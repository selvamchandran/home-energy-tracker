package com.selvam.insightservice.service;

import com.selvam.insightservice.client.UsageClient;
import com.selvam.insightservice.dto.InsightDto;
import com.selvam.insightservice.dto.response.DeviceResponse;
import com.selvam.insightservice.dto.response.UsageResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class InsightServiceImpl implements InsightService {

    private final UsageClient usageClient;
    private final OllamaChatModel ollamaChatModel;

    public InsightServiceImpl(UsageClient usageClient, OllamaChatModel ollamaChatModel) {
        this.usageClient = usageClient;
        this.ollamaChatModel = ollamaChatModel;
    }

    @Override
    public InsightDto getOverview(Long userId) {
        // Fetch data from Usage Service
        final UsageResponse usageData = usageClient.getXDaysUsageForUser(userId, 3);

        double totalUsage = usageData.devices().stream()
                .mapToDouble(DeviceResponse::energyConsumed)
                .sum();

        log.info("Calling Ollama for userId {} with total usage {}",
                userId, totalUsage);

        String prompt = new StringBuilder()
                .append("Analyse the following energy usage data and provide a " +
                        "concise overview with actionable insights.")
                .append("This data is the aggregate data for the past 3 days.")
                .append("Usage Data: \n")
                .append(usageData.devices())
                .toString();

        ChatResponse response = ollamaChatModel.call(
                Prompt.builder()
                        .content(prompt)
                        .build()
        );

        return InsightDto.builder()
                .userId(userId)
                .tips(response.getResult().getOutput().getText())
                .energyUsage(totalUsage)
                .build();
    }


    @Override
    public InsightDto getSavingTips(Long userId) {
        // Fetch data from Usage Service
        final UsageResponse usageData = usageClient.getXDaysUsageForUser(userId, 3);

        double totalUsage = usageData.devices().stream()
                .mapToDouble(DeviceResponse::energyConsumed)
                .sum();

        log.info("Calling Ollama for userId {} with total usage {}",
                userId, totalUsage);

        String prompt = new StringBuilder()
                .append("This is my total consumption over the past 3 days.")
                .append("How can I reduce my energy consumption? How does it compare to average households?")
                .append("Total energy used: \n")
                .append(totalUsage)
                .toString();

        ChatResponse response = ollamaChatModel.call(
                Prompt.builder()
                        .content(prompt)
                        .build()
        );

        return InsightDto.builder()
                .userId(userId)
                .tips(response.getResult().getOutput().getText())
                .energyUsage(totalUsage)
                .build();
    }
}
