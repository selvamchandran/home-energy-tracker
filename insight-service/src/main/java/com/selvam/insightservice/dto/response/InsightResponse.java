package com.selvam.insightservice.dto.response;

import lombok.Builder;

@Builder
public record InsightResponse(
        Long userId,
        String tips,
        double energyUsage
) {
}
