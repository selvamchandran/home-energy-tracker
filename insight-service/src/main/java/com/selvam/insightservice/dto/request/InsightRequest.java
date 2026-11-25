package com.selvam.insightservice.dto.request;

import lombok.Builder;

@Builder
public record InsightRequest(
        Long userId,
        String tips,
        double energyUsage
) {
}
