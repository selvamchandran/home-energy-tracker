package com.selvam.deviceservice.dto.request;

import com.selvam.deviceservice.model.DeviceType;
import lombok.Builder;

@Builder
public record DeviceRequest( String name,
                             DeviceType type,
                             String location,
                             Long userId) {
}
