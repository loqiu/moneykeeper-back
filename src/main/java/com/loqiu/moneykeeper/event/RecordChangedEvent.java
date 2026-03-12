package com.loqiu.moneykeeper.event;

import com.loqiu.moneykeeper.entity.MoneyKeeper;
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
public class RecordChangedEvent {
    private String eventId;
    private RecordChangeType changeType;
    private Long ledgerId;
    private Long recordId;
    private MoneyKeeper previousRecord;
    private MoneyKeeper currentRecord;
    private LocalDateTime occurredAt;

    public static RecordChangedEvent created(Long ledgerId, MoneyKeeper currentRecord) {
        return RecordChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .changeType(RecordChangeType.CREATED)
                .ledgerId(ledgerId)
                .recordId(currentRecord == null ? null : currentRecord.getId())
                .currentRecord(currentRecord)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    public static RecordChangedEvent updated(Long ledgerId, MoneyKeeper previousRecord, MoneyKeeper currentRecord) {
        return RecordChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .changeType(RecordChangeType.UPDATED)
                .ledgerId(ledgerId)
                .recordId(currentRecord == null ? null : currentRecord.getId())
                .previousRecord(previousRecord)
                .currentRecord(currentRecord)
                .occurredAt(LocalDateTime.now())
                .build();
    }

    public static RecordChangedEvent deleted(Long ledgerId, MoneyKeeper previousRecord) {
        return RecordChangedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .changeType(RecordChangeType.DELETED)
                .ledgerId(ledgerId)
                .recordId(previousRecord == null ? null : previousRecord.getId())
                .previousRecord(previousRecord)
                .occurredAt(LocalDateTime.now())
                .build();
    }
}
