package com.selvam.insightservice.dto.response;

import lombok.Builder;

@Builder
public record DeviceResponse(
        Long id,
        String name,
        String type,
        String location,
        double energyConsumed
) {
}
