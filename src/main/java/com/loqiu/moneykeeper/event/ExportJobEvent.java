package com.loqiu.moneykeeper.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportJobEvent {
    private String eventId;
    private ExportJobEventType eventType;
    private Long jobId;
    private Long ledgerId;
    private Long requestedByUserId;
    private LocalDateTime occurredAt;

    public static ExportJobEvent created(Long jobId, Long ledgerId, Long requestedByUserId) {
        return ExportJobEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(ExportJobEventType.CREATED)
                .jobId(jobId)
                .ledgerId(ledgerId)
                .requestedByUserId(requestedByUserId)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
