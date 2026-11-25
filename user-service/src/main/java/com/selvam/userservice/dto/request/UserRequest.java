package com.selvam.userservice.dto.request;

import lombok.Builder;

@Builder
public record UserRequest(
        String name,
        String surname,
        String email,
        String address,
        boolean alerting,
        double energyAlertingThreshold
) {
}
