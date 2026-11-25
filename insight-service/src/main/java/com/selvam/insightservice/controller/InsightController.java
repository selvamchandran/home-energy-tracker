package com.selvam.insightservice.controller;

import com.selvam.insightservice.dto.InsightDto;
import com.selvam.insightservice.service.InsightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/insight")
public class InsightController {

    private final InsightService insightService;

    public InsightController(InsightService insightService) {
        this.insightService = insightService;
    }

    @GetMapping("/saving-tips/{userId}")
    public ResponseEntity<InsightDto> getSavingTips(@PathVariable Long userId)
    {
        final InsightDto insight = insightService.getSavingTips(userId);
        return ResponseEntity.ok(insight);
    }

    @GetMapping("/overview/{userId}")
    public ResponseEntity<InsightDto> getOverview(@PathVariable Long userId)
    {
        final InsightDto insight = insightService.getOverview(userId);
        return ResponseEntity.ok(insight);
    }
}
