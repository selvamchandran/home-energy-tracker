package com.selvam.insightservice.dto.response;

import lombok.Builder;

import java.util.List;

@Builder
public record UsageResponse(
        Long userId,
        List<DeviceResponse> devices
) {
}
