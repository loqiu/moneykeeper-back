package com.loqiu.moneykeeper.controller;

import com.loqiu.moneykeeper.dto.IntegrationModuleStatusDTO;
import com.loqiu.moneykeeper.exception.ForbiddenException;
import com.loqiu.moneykeeper.exception.ResourceNotFoundException;
import com.loqiu.moneykeeper.service.IntegrationStatusService;
import com.loqiu.moneykeeper.util.RequestAuthUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/integrations")
public class IntegrationController {

    @Autowired
    private IntegrationStatusService integrationStatusService;

    @GetMapping("/status")
    public ResponseEntity<List<IntegrationModuleStatusDTO>> getIntegrationStatuses(HttpServletRequest request) {
        requireAdmin(request);
        return ResponseEntity.ok(integrationStatusService.getAllStatuses());
    }

    @GetMapping("/status/{module}")
    public ResponseEntity<IntegrationModuleStatusDTO> getIntegrationStatus(@PathVariable String module,
                                                                           HttpServletRequest request) {
        requireAdmin(request);
        IntegrationModuleStatusDTO status = integrationStatusService.getStatus(module);
        if (status == null) {
            throw new ResourceNotFoundException("Integration module not found");
        }
        return ResponseEntity.ok(status);
    }

    private void requireAdmin(HttpServletRequest request) {
        if (!RequestAuthUtil.isAdmin(request)) {
            throw new ForbiddenException("Admin role is required");
        }
    }
}