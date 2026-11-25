package com.selvam.usageservice.client;

import com.selvam.usageservice.dto.ApiResponseDto;
import com.selvam.usageservice.dto.response.DeviceResponse;
import com.selvam.usageservice.dto.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
public class UserClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserClient(@Value("${user.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public UserResponse getUserById(Long userId) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/{id}")
                .buildAndExpand(userId)
                .toUriString();

        log.info("User Service url: {}", url);

        /*ResponseEntity<UserResponse> response = restTemplate.getForEntity(url, UserResponse.class);
        return response.getBody();*/
        ResponseEntity<ApiResponseDto<UserResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseDto<UserResponse>>() {
                }
        );

        ApiResponseDto<UserResponse> body = response.getBody();
        return (body != null) ? body.getData() : null;
    }
}
