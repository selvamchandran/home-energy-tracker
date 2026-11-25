package com.selvam.ingestionservice.controller;

import com.selvam.ingestionservice.dto.EnergyUsageRequest;
import com.selvam.ingestionservice.service.IngestionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.prefix}/ingestion")
public class IngestionController {
    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void ingestData(@RequestBody EnergyUsageRequest request)
    {
        ingestionService.ingestEnergyUsage(request);
    }

}
