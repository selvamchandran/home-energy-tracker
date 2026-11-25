package com.selvam.usageservice.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UsageResponse(
        Long userId,
        List<DeviceResponse> devices
) {
}
