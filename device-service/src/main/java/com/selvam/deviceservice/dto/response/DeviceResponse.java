package com.selvam.deviceservice.dto.response;

import com.selvam.deviceservice.model.DeviceType;
import lombok.Builder;
import lombok.Data;

@Builder
public record DeviceResponse(
        Long id,
        String name,
        DeviceType type,
        String location,
        Long userId
) {
}
