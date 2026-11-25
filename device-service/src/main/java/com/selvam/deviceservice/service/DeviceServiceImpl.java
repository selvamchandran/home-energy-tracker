package com.selvam.deviceservice.service;

import com.selvam.deviceservice.dto.request.DeviceRequest;
import com.selvam.deviceservice.dto.response.DeviceResponse;
import com.selvam.deviceservice.entity.Device;
import com.selvam.deviceservice.exceptions.DeviceNotFoundException;
import com.selvam.deviceservice.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    public DeviceServiceImpl(DeviceRepository deviceRepository) {
        this.deviceRepository = deviceRepository;
    }

    @Override
    public DeviceResponse getDeviceById(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("No Device Found with id " + id));

        return mapToDto(device);
    }

    @Override
    public DeviceResponse createDevice(DeviceRequest request) {
        Device device = new Device();
        device.setName(request.name());
        device.setType(request.type());
        device.setLocation(request.location());
        device.setUserId(request.userId());
        final Device savedDevice = deviceRepository.save(device);

        return mapToDto(savedDevice);
    }

    @Override
    public DeviceResponse updateDevice(Long id, DeviceRequest request) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id " + id));
        device.setName(request.name());
        device.setType(request.type());
        device.setLocation(request.location());
        device.setUserId(request.userId());

        final Device updatedDevice = deviceRepository.save(device);
        return mapToDto(updatedDevice);
    }

    @Override
    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found with id " + id));
        deviceRepository.delete(device);
    }

    @Override
    public List<DeviceResponse> getAllDevicesByUserId(Long userId) {
        List<Device> devices = deviceRepository.findAllByUserId(userId);
        return devices.stream()
                .map(this::mapToDto)
                .toList();
    }

    private DeviceResponse mapToDto(Device device) {
        return DeviceResponse.builder()
                .id(device.getId())
                .name(device.getName())
                .type(device.getType())
                .location(device.getLocation())
                .userId(device.getUserId())
                .build();
    }
}
