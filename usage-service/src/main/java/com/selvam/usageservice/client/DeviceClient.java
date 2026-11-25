package com.selvam.usageservice.client;

import com.selvam.usageservice.dto.ApiResponseDto;
import com.selvam.usageservice.dto.response.DeviceResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Slf4j
@Component
public class DeviceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public DeviceClient(@Value("${device.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public DeviceResponse getDeviceById(Long deviceId) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/{id}")
                .buildAndExpand(deviceId)
                .toUriString();

        log.info("Device Service url: {}", url);
//        ResponseEntity<DeviceResponse> response = restTemplate.getForEntity(url, DeviceResponse.class);
//        ResponseEntity<ApiResponseDto> response = restTemplate.getForEntity(url, ApiResponseDto.class);
//        return response.getBody();

        ResponseEntity<ApiResponseDto<DeviceResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseDto<DeviceResponse>>() {
                }
        );

        ApiResponseDto<DeviceResponse> body = response.getBody();
        return (body != null) ? body.getData() : null;
    }

    public List<DeviceResponse> getAllDevicesForUser(Long userId) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/user/{userId}")
                .buildAndExpand(userId)
                .toUriString();

        log.info("Device Service url: {}", url);
//        ResponseEntity<DeviceResponse> response = restTemplate.getForEntity(url, DeviceResponse.class);
//        ResponseEntity<ApiResponseDto> response = restTemplate.getForEntity(url, ApiResponseDto.class);
//        return response.getBody();

        ResponseEntity<ApiResponseDto<DeviceResponse[]>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseDto<DeviceResponse[]>>() {
                }
        );

        ApiResponseDto<DeviceResponse[]> body = response.getBody();
        return (body != null) ? List.of(body.getData()) : List.of();
    }
}
