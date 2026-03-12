package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.dto.ExportJobDTO;
import com.loqiu.moneykeeper.vo.ExportJobRequest;

import java.util.List;

public interface ExportJobService {
    List<ExportJobDTO> listJobs(Long ledgerId, Long currentUserId, boolean admin, int limit);

    ExportJobDTO getJob(Long ledgerId, Long jobId, Long currentUserId, boolean admin);

    ExportJobDTO createJob(Long ledgerId, Long requestedByUserId, ExportJobRequest request);

    void markJobDownloaded(Long jobId);
}
