package com.selvam.usageservice.dto.response;

import lombok.Builder;

@Builder
public record DeviceResponse(
        Long id,
        String name,
        String type,
        String location,
        Long userId,
        Double energyConsumed
) {
}
