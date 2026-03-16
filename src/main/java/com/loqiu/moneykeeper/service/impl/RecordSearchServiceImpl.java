package com.loqiu.moneykeeper.service.impl;

import com.loqiu.moneykeeper.constant.ErrorKeyConstants;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.CountResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
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
import com.loqiu.moneykeeper.util.RecordTypeNormalizer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Override
    public boolean isEnabled() {
        return elasticsearchProperties.isEnabled();
    }

    @Override
    public boolean isReady() {
        return elasticsearchProperties.isEnabled() && elasticsearchClientProvider.getIfAvailable() != null;
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
        return searchRecordsByScope(null, userId, query, type, categoryId, categoryName, startDate, endDate, limit);
    }

    @Override
    public List<RecordSearchResultDTO> searchLedgerRecords(Long ledgerId,
                                                           Long userId,
                                                           String query,
                                                           String type,
                                                           Long categoryId,
                                                           String categoryName,
                                                           LocalDate startDate,
                                                           LocalDate endDate,
                                                           int limit) {
        return searchRecordsByScope(ledgerId, userId, query, type, categoryId, categoryName, startDate, endDate, limit);
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
                    .ledgerId(null)
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
    public RecordSearchReindexResultDTO reindexLedgerRecords(Long ledgerId) {
        ElasticsearchClient client = requireClient();
        try {
            ensureIndex(client);
            deleteLedgerDocuments(client, ledgerId);

            List<MoneyKeeperDTO> records = moneyKeeperMapper.getLedgerRecordsWithCategoryName(ledgerId, null, null, null);
            int indexedCount = indexRecords(client, records);
            logger.info("Record search ledger reindex completed - ledgerId: {}, count: {}", ledgerId, indexedCount);

            return RecordSearchReindexResultDTO.builder()
                    .scope("ledger")
                    .userId(null)
                    .ledgerId(ledgerId)
                    .indexedCount(indexedCount)
                    .indexName(getIndexName())
                    .reindexedAt(LocalDateTime.now())
                    .build();
        } catch (IOException ex) {
            logger.error("Failed to reindex ledger records in Elasticsearch - ledgerId: {}", ledgerId, ex);
            throw new ServiceUnavailableException("Elasticsearch reindex is temporarily unavailable");
        }
    }

    @Override
    public RecordSearchIndexStatsDTO getIndexStats(Long userId) {
        ElasticsearchClient client = requireClient();
        try {
            boolean indexExists = client.indices().exists(request -> request.index(getIndexName())).value();
            long indexedDocumentCount = indexExists ? countIndexedDocuments(client, userId, null) : 0L;
            long databaseRecordCount = countDatabaseRecords(userId, null);
            return RecordSearchIndexStatsDTO.builder()
                    .scope(userId == null ? "all" : "user")
                    .userId(userId)
                    .ledgerId(null)
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
    public RecordSearchIndexStatsDTO getLedgerIndexStats(Long ledgerId) {
        ElasticsearchClient client = requireClient();
        try {
            boolean indexExists = client.indices().exists(request -> request.index(getIndexName())).value();
            long indexedDocumentCount = indexExists ? countIndexedDocuments(client, null, ledgerId) : 0L;
            long databaseRecordCount = countDatabaseRecords(null, ledgerId);
            return RecordSearchIndexStatsDTO.builder()
                    .scope("ledger")
                    .userId(null)
                    .ledgerId(ledgerId)
                    .enabled(isEnabled())
                    .ready(isReady())
                    .indexName(getIndexName())
                    .indexExists(indexExists)
                    .indexedDocumentCount(indexedDocumentCount)
                    .databaseRecordCount(databaseRecordCount)
                    .statsCollectedAt(LocalDateTime.now())
                    .build();
        } catch (IOException ex) {
            logger.error("Failed to load record search index stats - ledgerId: {}", ledgerId, ex);
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

    private List<RecordSearchResultDTO> searchRecordsByScope(Long ledgerId,
                                                             Long userId,
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
                        .query(q -> q.bool(buildQuery(ledgerId, userId, queryText, type, categoryId, categoryName, startDate, endDate)));
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
            logger.error("Failed to search records in Elasticsearch - ledgerId: {}, userId: {}", ledgerId, userId, ex);
            throw new ServiceUnavailableException("Elasticsearch search is temporarily unavailable");
        }
    }

    private ElasticsearchClient requireClient() {
        if (!elasticsearchProperties.isEnabled()) {
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

    private void deleteLedgerDocuments(ElasticsearchClient client, Long ledgerId) throws IOException {
        client.deleteByQuery(request -> request
                .index(getIndexName())
                .query(q -> q.term(t -> t.field("ledgerId").value(ledgerId))));
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

    private long countIndexedDocuments(ElasticsearchClient client, Long userId, Long ledgerId) throws IOException {
        CountResponse response;
        if (userId == null && ledgerId == null) {
            response = client.count(request -> request.index(getIndexName()));
        } else if (userId != null) {
            response = client.count(request -> request
                    .index(getIndexName())
                    .query(q -> q.term(t -> t.field("userId").value(userId))));
        } else {
            response = client.count(request -> request
                    .index(getIndexName())
                    .query(q -> q.term(t -> t.field("ledgerId").value(ledgerId))));
        }
        return response.count();
    }

    private long countDatabaseRecords(Long userId, Long ledgerId) {
        QueryWrapper<MoneyKeeper> queryWrapper = new QueryWrapper<>();
        if (userId != null) {
            queryWrapper.eq("user_id", userId);
        }
        if (ledgerId != null) {
            queryWrapper.eq("ledger_id", ledgerId);
        }
        Long count = moneyKeeperMapper.selectCount(queryWrapper);
        return count == null ? 0L : count;
    }

    private BoolQuery buildQuery(Long ledgerId,
                                 Long userId,
                                 String query,
                                 String type,
                                 Long categoryId,
                                 String categoryName,
                                 LocalDate startDate,
                                 LocalDate endDate) {
        BoolQuery.Builder boolQuery = new BoolQuery.Builder();
        if (ledgerId != null) {
            boolQuery.filter(filter -> filter.term(term -> term.field("ledgerId").value(ledgerId)));
        }
        if (userId != null) {
            boolQuery.filter(filter -> filter.term(term -> term.field("userId").value(userId)));
        }
        if (categoryId != null) {
            boolQuery.filter(filter -> filter.term(term -> term.field("categoryId").value(categoryId)));
        }
        if (StringUtils.hasText(type)) {
            boolQuery.filter(filter -> filter.term(term -> term.field("type.keyword")
                    .value(RecordTypeNormalizer.normalizeRequired(type, "Record type is required", "Record type must be income or expense", ErrorKeyConstants.RECORD_INVALID_TYPE))));
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
                .ledgerId(record.getLedgerId())
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
                .ledgerId(document.getLedgerId())
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
