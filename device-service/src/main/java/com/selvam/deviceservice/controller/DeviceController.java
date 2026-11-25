package com.selvam.deviceservice.controller;

import com.selvam.deviceservice.dto.ApiResponseDto;
import com.selvam.deviceservice.dto.request.DeviceRequest;
import com.selvam.deviceservice.dto.response.DeviceResponse;
import com.selvam.deviceservice.service.DeviceService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/device")
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }


    @PostMapping
    public ResponseEntity<ApiResponseDto<DeviceResponse>> createDevice(DeviceRequest request) {
        DeviceResponse deviceResponse = deviceService.createDevice(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponseDto<>("Device created successfully", deviceResponse, HttpStatus.CREATED));

    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<DeviceResponse>> getDeviceById(@PathVariable Long id) {
        DeviceResponse deviceResponse = deviceService.getDeviceById(id);
        return ResponseEntity.ok(new ApiResponseDto<>("Device retrieved successfully", deviceResponse));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<DeviceResponse>> updateDevice(@PathVariable Long id,
                                                                       DeviceRequest request) {
        DeviceResponse deviceResponse = deviceService.updateDevice(id, request);
        return ResponseEntity.ok(new ApiResponseDto<>("Success!", deviceResponse, HttpStatus.OK));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteDevice(@PathVariable Long id) {
        {

            deviceService.deleteDevice(id);
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .body(new ApiResponseDto<>("Device deleted successfully", null, HttpStatus.NO_CONTENT));

        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDto<List<DeviceResponse>>> getAllDevicesByUserID(
            @PathVariable Long userId
    ) {
        List<DeviceResponse> devices = deviceService.getAllDevicesByUserId(userId);
        return ResponseEntity.ok(
                new ApiResponseDto<>("Device retrieved successfully", devices)
        );
    }
}
