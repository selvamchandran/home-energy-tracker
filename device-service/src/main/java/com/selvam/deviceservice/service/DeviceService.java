package com.selvam.deviceservice.service;

import com.selvam.deviceservice.dto.request.DeviceRequest;
import com.selvam.deviceservice.dto.response.DeviceResponse;

import java.util.List;

public interface DeviceService {
    DeviceResponse getDeviceById(Long id);

    DeviceResponse createDevice(DeviceRequest request);

    DeviceResponse updateDevice(Long id, DeviceRequest request);

    void deleteDevice(Long id);

    List<DeviceResponse> getAllDevicesByUserId(Long userId);
}
