package com.loqiu.moneykeeper.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.loqiu.moneykeeper.config.ElasticsearchProperties;
import com.loqiu.moneykeeper.dto.MoneyKeeperDTO;
import com.loqiu.moneykeeper.dto.RecordSearchDocument;
import com.loqiu.moneykeeper.dto.RecordSearchIndexStatsDTO;
import com.loqiu.moneykeeper.dto.RecordSearchReindexResultDTO;
import com.loqiu.moneykeeper.dto.RecordSearchResultDTO;
import com.loqiu.moneykeeper.entity.MoneyKeeper;
import com.loqiu.moneykeeper.exception.ServiceUnavailableException;
import com.loqiu.moneykeeper.mapper.MoneyKeeperMapper;
import com.loqiu.moneykeeper.service.RecordSearchService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class RecordSearchServiceImpl implements RecordSearchService {

    private static final Logger logger = LogManager.getLogger(RecordSearchServiceImpl.class);

    @Autowired
    private MoneyKeeperMapper moneyKeeperMapper;

    @Autowired
    private ElasticsearchProperties elasticsearchProperties;

    @Autowired
    private ObjectProvider<ElasticsearchClient> elasticsearchClientProvider;

    @Value("${app.elasticsearch.enabled:false}")
    private boolean elasticsearchEnabled;

    @Override
    public boolean isEnabled() {
        return elasticsearchEnabled;
    }

    @Override
    public boolean isReady() {
        return elasticsearchEnabled && elasticsearchClientProvider.getIfAvailable() != null;
    }

    @Override
    public String getIndexName() {
        return elasticsearchProperties.getIndexName();
    }

    @Override
    public List<RecordSearchResultDTO> searchRecords(Long userId,
                                                     String query,
                                                     String type,
                                                     Long categoryId,
                                                     String categoryName,
                                                     LocalDate startDate,
                                                     LocalDate endDate,
                                                     int limit) {
        ElasticsearchClient client = requireClient();
        try {
            ensureIndex(client);
            String queryText = normalizeText(query);
            SearchResponse<RecordSearchDocument> response = client.search(search -> {
                search.index(getIndexName())
                        .size(limit)
                        .query(q -> q.bool(buildQuery(userId, queryText, type, categoryId, categoryName, startDate, endDate)));
                if (StringUtils.hasText(queryText)) {
                    search.sort(sort -> sort.score(score -> score.order(SortOrder.Desc)));
                }
                search.sort(sort -> sort.field(field -> field.field("transactionDate").order(SortOrder.Desc)));
                return search.sort(sort -> sort.field(field -> field.field("updatedAt").order(SortOrder.Desc)));
            }, RecordSearchDocument.class);

            return response.hits().hits().stream()
                    .map(this::toSearchResult)
                    .filter(Objects::nonNull)
                    .toList();
        } catch (IOException ex) {
            logger.error("Failed to search records in Elasticsearch - userId: {}", userId, ex);
            throw new ServiceUnavailableException("Elasticsearch search is temporarily unavailable");
        }
    }

    @Override
    public RecordSearchReindexResultDTO reindexRecords(Long userId) {
        ElasticsearchClient client = requireClient();
        try {
            if (userId == null) {
                resetIndex(client);
            } else {
                ensureIndex(client);
                deleteUserDocuments(client, userId);
            }

            List<MoneyKeeperDTO> records = moneyKeeperMapper.getAllRecordsWithCategoryName(userId, null, null);
            int indexedCount = indexRecords(client, records);
            logger.info("Record search reindex completed - scope: {}, userId: {}, count: {}",
                    userId == null ? "all" : "user", userId, indexedCount);

            return RecordSearchReindexResultDTO.builder()
                    .scope(userId == null ? "all" : "user")
                    .userId(userId)
                    .indexedCount(indexedCount)
                    .indexName(getIndexName())
                    .reindexedAt(LocalDateTime.now())
                    .build();
        } catch (IOException ex) {
            logger.error("Failed to reindex records in Elasticsearch - userId: {}", userId, ex);
            throw new ServiceUnavailableException("Elasticsearch reindex is temporarily unavailable");
        }
    }

    @Override
    public RecordSearchIndexStatsDTO getIndexStats(Long userId) {
        ElasticsearchClient client = requireClient();
        try {
            boolean indexExists = client.indices().exists(request -> request.index(getIndexName())).value();
            long indexedDocumentCount = indexExists ? countIndexedDocuments(client, userId) : 0L;
            long databaseRecordCount = countDatabaseRecords(userId);
            return RecordSearchIndexStatsDTO.builder()
                    .scope(userId == null ? "all" : "user")
                    .userId(userId)
                    .enabled(isEnabled())
                    .ready(isReady())
                    .indexName(getIndexName())
                    .indexExists(indexExists)
                    .indexedDocumentCount(indexedDocumentCount)
                    .databaseRecordCount(databaseRecordCount)
                    .statsCollectedAt(LocalDateTime.now())
                    .build();
        } catch (IOException ex) {
            logger.error("Failed to load record search index stats - userId: {}", userId, ex);
            throw new ServiceUnavailableException("Elasticsearch index stats are temporarily unavailable");
        }
    }

    @Override
    public void syncRecordIfEnabled(Long recordId) {
        if (recordId == null || !isReady()) {
            return;
        }
        try {
            ElasticsearchClient client = requireClient();
            ensureIndex(client);
            upsertRecord(client, recordId);
        } catch (IOException | RuntimeException ex) {
            logger.warn("Best-effort record sync failed - recordId: {}, message: {}", recordId, ex.getMessage());
        }
    }

    @Override
    public void removeRecordIfEnabled(Long recordId) {
        if (recordId == null || !isReady()) {
            return;
        }
        try {
            ElasticsearchClient client = requireClient();
            if (!indexExists(client)) {
                return;
            }
            deleteRecordDocument(client, recordId);
        } catch (IOException | RuntimeException ex) {
            logger.warn("Best-effort record removal failed - recordId: {}, message: {}", recordId, ex.getMessage());
        }
    }

    @Override
    public void refreshCategoryRecordsIfEnabled(Long categoryId) {
        if (categoryId == null || !isReady()) {
            return;
        }
        try {
            ElasticsearchClient client = requireClient();
            ensureIndex(client);
            replaceCategoryDocuments(client, categoryId);
        } catch (IOException | RuntimeException ex) {
            logger.warn("Best-effort category refresh failed - categoryId: {}, message: {}", categoryId, ex.getMessage());
        }
    }

    @Override
    public void reindexUserRecordsIfEnabled(Long userId) {
        if (userId == null || !isReady()) {
            return;
        }
        try {
            reindexRecords(userId);
        } catch (RuntimeException ex) {
            logger.warn("Best-effort user reindex failed - userId: {}, message: {}", userId, ex.getMessage());
        }
    }

    private ElasticsearchClient requireClient() {
        if (!elasticsearchEnabled) {
            throw new ServiceUnavailableException("Elasticsearch search module is disabled");
        }
        ElasticsearchClient client = elasticsearchClientProvider.getIfAvailable();
        if (client == null) {
            throw new ServiceUnavailableException("Elasticsearch client is not ready");
        }
        return client;
    }

    private boolean indexExists(ElasticsearchClient client) throws IOException {
        return client.indices().exists(request -> request.index(getIndexName())).value();
    }

    private void ensureIndex(ElasticsearchClient client) throws IOException {
        if (!indexExists(client)) {
            client.indices().create(request -> request.index(getIndexName()));
        }
    }

    private void resetIndex(ElasticsearchClient client) throws IOException {
        if (indexExists(client)) {
            client.indices().delete(request -> request.index(getIndexName()));
        }
        client.indices().create(request -> request.index(getIndexName()));
    }

    private void deleteUserDocuments(ElasticsearchClient client, Long userId) throws IOException {
        client.deleteByQuery(request -> request
                .index(getIndexName())
                .query(q -> q.term(t -> t.field("userId").value(userId))));
    }

    private void deleteCategoryDocuments(ElasticsearchClient client, Long categoryId) throws IOException {
        client.deleteByQuery(request -> request
                .index(getIndexName())
                .query(q -> q.term(t -> t.field("categoryId").value(categoryId))));
    }

    private void deleteRecordDocument(ElasticsearchClient client, Long recordId) throws IOException {
        client.deleteByQuery(request -> request
                .index(getIndexName())
                .query(q -> q.term(t -> t.field("recordId").value(recordId))));
    }

    private void replaceCategoryDocuments(ElasticsearchClient client, Long categoryId) throws IOException {
        deleteCategoryDocuments(client, categoryId);
        List<MoneyKeeperDTO> records = moneyKeeperMapper.getRecordsWithCategoryNameByCategoryId(categoryId);
        indexRecords(client, records);
    }

    private void upsertRecord(ElasticsearchClient client, Long recordId) throws IOException {
        MoneyKeeperDTO record = moneyKeeperMapper.getRecordWithCategoryName(recordId);
        if (record == null) {
            deleteRecordDocument(client, recordId);
            return;
        }
        indexRecord(client, record);
    }

    private int indexRecords(ElasticsearchClient client, List<MoneyKeeperDTO> records) throws IOException {
        int indexedCount = 0;
        for (MoneyKeeperDTO record : records) {
            if (record == null || record.getId() == null) {
                continue;
            }
            indexRecord(client, record);
            indexedCount++;
        }
        return indexedCount;
    }

    private void indexRecord(ElasticsearchClient client, MoneyKeeperDTO record) throws IOException {
        client.index(request -> request
                .index(getIndexName())
                .id(String.valueOf(record.getId()))
                .document(toDocument(record)));
    }

    private long countIndexedDocuments(ElasticsearchClient client, Long userId) throws IOException {
        CountResponse response;
        if (userId == null) {
            response = client.count(request -> request.index(getIndexName()));
        } else {
            response = client.count(request -> request
                    .index(getIndexName())
                    .query(q -> q.term(t -> t.field("userId").value(userId))));
        }
        return response.count();
    }

    private long countDatabaseRecords(Long userId) {
        QueryWrapper<MoneyKeeper> queryWrapper = new QueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        Long count = moneyKeeperMapper.selectCount(queryWrapper);
        return count == null ? 0L : count;
    }

    private BoolQuery buildQuery(Long userId,
                                 String query,
                                 String type,
                                 Long categoryId,
                                 String categoryName,
                                 LocalDate startDate,
                                 LocalDate endDate) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        boolQuery.filter(filter -> filter.term(term -> term.field("userId").value(userId)));

        if (categoryId != null) {
            boolQuery.filter(filter -> filter.term(term -> term.field("categoryId").value(categoryId)));
        }
        if (StringUtils.hasText(type)) {
            boolQuery.filter(filter -> filter.term(term -> term.field("type.keyword").value(type.trim())));
        }
        if (StringUtils.hasText(categoryName)) {
            boolQuery.filter(filter -> filter.term(term -> term.field("categoryName.keyword").value(categoryName.trim())));
        }
        if (startDate != null || endDate != null) {
            boolQuery.filter(filter -> filter.range(range -> {
                range.field("transactionDate");
                if (startDate != null) {
                    range.gte(JsonData.of(startDate.toString()));
                }
                if (endDate != null) {
                    range.lte(JsonData.of(endDate.toString()));
                }
                return range;
            }));
        }
        if (StringUtils.hasText(query)) {
            boolQuery.must(must -> must.multiMatch(match -> match
                    .query(query)
                    .fields("notes", "categoryName", "type")));
        }
        return boolQuery.build();
    }

    private RecordSearchDocument toDocument(MoneyKeeperDTO record) {
        return RecordSearchDocument.builder()
                .recordId(record.getId())
                .userId(record.getUserId())
                .categoryId(record.getCategoryId())
                .categoryName(record.getCategoryName())
                .type(record.getType())
                .amount(record.getAmount())
                .transactionDate(record.getTransactionDate() == null ? null : record.getTransactionDate().toString())
                .updatedAt(record.getUpdatedAt() == null ? null : record.getUpdatedAt().toString())
                .notes(record.getNotes())
                .build();
    }

    private RecordSearchResultDTO toSearchResult(Hit<RecordSearchDocument> hit) {
        RecordSearchDocument document = hit.source();
        if (document == null) {
            return null;
        }
        Long recordId = document.getRecordId();
        if (recordId == null && StringUtils.hasText(hit.id())) {
            try {
                recordId = Long.parseLong(hit.id());
            } catch (NumberFormatException ex) {
                logger.warn("Unable to parse Elasticsearch hit id as record id: {}", hit.id());
            }
        }
        return RecordSearchResultDTO.builder()
                .id(recordId)
                .userId(document.getUserId())
                .categoryId(document.getCategoryId())
                .categoryName(document.getCategoryName())
                .type(document.getType())
                .amount(document.getAmount())
                .transactionDate(parseLocalDate(document.getTransactionDate()))
                .updatedAt(parseLocalDateTime(document.getUpdatedAt()))
                .notes(document.getNotes())
                .score(hit.score())
                .build();
    }

    private LocalDate parseLocalDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDate.parse(value);
    }

    private LocalDateTime parseLocalDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDateTime.parse(value);
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}