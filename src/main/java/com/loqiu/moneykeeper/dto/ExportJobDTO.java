package com.loqiu.moneykeeper.dto;

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
    private String status;
    private Integer recordCount;
    private Integer downloadCount;
    private LocalDateTime completedAt;
    private LocalDateTime lastDownloadedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String downloadUrl;
}
