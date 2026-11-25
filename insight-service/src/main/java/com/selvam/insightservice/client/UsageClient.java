package com.selvam.insightservice.client;

import com.selvam.insightservice.dto.ApiResponseDto;
import com.selvam.insightservice.dto.InsightDto;
import com.selvam.insightservice.dto.response.InsightResponse;

import com.selvam.insightservice.dto.response.UsageResponse;
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
public class UsageClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UsageClient(@Value("${usage.service.url}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
    }

    public UsageResponse getXDaysUsageForUser(Long userId, int days) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/{userId}")
                .queryParam("days", days)
                .buildAndExpand(userId)
                .toUriString();

        log.info("Usage Service url: {}", url);

        ResponseEntity<ApiResponseDto<UsageResponse>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ApiResponseDto<UsageResponse>>() {
                }
        );

        ApiResponseDto<UsageResponse> body = response.getBody();
        return (body != null) ? body.getData() : null;
    }
}
