package com.selvam.usageservice.controller;

import com.selvam.usageservice.dto.ApiResponseDto;
import com.selvam.usageservice.dto.response.UsageResponse;
import com.selvam.usageservice.service.UsageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/usage")
public class UsageController {
    private final UsageService usageService;

    public UsageController(UsageService usageService) {
        this.usageService = usageService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponseDto<UsageResponse>> getUserDeviceUsage(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "3") int days
    ) {
        final UsageResponse usage = usageService.getXDaysUsageForUser(userId, days);
        return ResponseEntity.ok(new ApiResponseDto<>("Usage Details retrieved successfully", usage));
    }
}
