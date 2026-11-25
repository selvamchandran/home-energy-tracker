package com.selvam.usageservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class DeviceEnergy {
    private Long deviceId;
    private double energyConsumed;
    private Long userId;
}
