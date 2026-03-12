package com.loqiu.moneykeeper.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobDTO {
    private Long id;
    private Long ledgerId;
    private Long requestedByUserId;
    private Long targetUserId;
    private String recordType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String fileName;
    private String fileFormat;
    @JsonIgnore
    private String storagePath;
    private String status;
    private String errorMessage;
    private Integer recordCount;
    private Integer downloadCount;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime lastDownloadedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String downloadUrl;
}
