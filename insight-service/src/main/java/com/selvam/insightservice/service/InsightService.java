package com.selvam.insightservice.service;


import com.selvam.insightservice.dto.InsightDto;

public interface InsightService {
    InsightDto getOverview(Long userId);

    InsightDto getSavingTips(Long userId);
}
