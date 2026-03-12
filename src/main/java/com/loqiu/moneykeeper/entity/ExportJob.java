package com.loqiu.moneykeeper.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("export_job")
public class ExportJob {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("ledger_id")
    private Long ledgerId;

    @TableField("requested_by_user_id")
    private Long requestedByUserId;

    @TableField("target_user_id")
    private Long targetUserId;

    @TableField("record_type")
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

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
