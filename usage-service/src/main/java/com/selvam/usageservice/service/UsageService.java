package com.selvam.usageservice.service;

import com.influxdb.v3.client.InfluxDBClient;
import com.influxdb.v3.client.Point;
import com.influxdb.v3.client.write.WriteOptions;
import com.influxdb.v3.client.write.WritePrecision;
import com.selvam.kafka.event.AlertingEvent;
import com.selvam.kafka.event.EnergyUsageEvent;
import com.selvam.usageservice.client.DeviceClient;
import com.selvam.usageservice.client.UserClient;
import com.selvam.usageservice.dto.response.DeviceResponse;
import com.selvam.usageservice.dto.response.UsageResponse;
import com.selvam.usageservice.dto.response.UserResponse;
import com.selvam.usageservice.model.Device;
import com.selvam.usageservice.model.DeviceEnergy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class UsageService {

    private final InfluxDBClient influxDBClient;
    private final DeviceClient deviceClient;
    private final UserClient userClient;

    /*@Value("${influx.bucket}")
    private String influxBucket;

    @Value("${influx.org}")
    private String influxOrg;*/

    private final KafkaTemplate<String, AlertingEvent> kafkaTemplate;

    @Value("${influx.database}")
    private String database;

    public UsageService(InfluxDBClient influxDBClient, DeviceClient deviceClient, UserClient userClient, KafkaTemplate<String, AlertingEvent> kafkaTemplate) {
        this.influxDBClient = influxDBClient;
        this.deviceClient = deviceClient;
        this.userClient = userClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    @KafkaListener(topics = "energy-usage", groupId = "usage-service")
    public void energyUsageEvent(EnergyUsageEvent energyUsageEvent) {
//        log.info("Received energy usage event: {}", energyUsageEvent); //Hide it,because it might load huge load of data later


        Point point = Point.measurement("energy_usage")
                .setTag("deviceId", String.valueOf(energyUsageEvent.deviceId()))
                .setField("energyConsumed", energyUsageEvent.energyConsumed())
                .setTimestamp(energyUsageEvent.timestamp());

        WriteOptions params = new WriteOptions.Builder()
                .database(database)
                .precision(WritePrecision.MS)
                .build();

        influxDBClient.writePoint(point, params);

    }

    @Scheduled(cron = "*/10 * * * * *")
    public void aggregateDeviceEnergyUsage() {
        final Instant now = Instant.now();
        final Instant oneHourAgo = now.minusSeconds(3600);

        // Format instants to ISO strings for SQL compatibility
        String startTime = DateTimeFormatter.ISO_INSTANT.format(oneHourAgo);
        String endTime = DateTimeFormatter.ISO_INSTANT.format(now);

        // Modern InfluxDB 3.x SQL Query targeting the iox schema and double-quoted table name
      /*  String sqlQuery = String.format("""
                SELECT deviceId, SUM(energyConsumed) AS total_energy
                FROM iox."energy-usage"
                WHERE time >= '%s' AND time < '%s'
                GROUP BY deviceId
                """, startTime, endTime);*/

        String sqlQuery = "SELECT iox.\"energy_usage\".\"deviceId\", SUM(iox.\"energy_usage\".\"energyConsumed\") AS total_energy FROM iox.\"energy_usage\" WHERE iox.\"energy_usage\".time >= now() - INTERVAL '24 hours' GROUP BY iox.\"energy_usage\".\"deviceId\"";
        log.info("Executing InfluxDB SQL Query: {}", sqlQuery);

        List<DeviceEnergy> deviceEnergies = new ArrayList<>();

        try {
            // Execute query using the InfluxDB 3 client (adjust method call if your v3 client uses query(sql) or executeQuery)
            influxDBClient.query(sqlQuery).forEach(row -> {
                try {
                    // Extract values safely based on InfluxDB 3 query record/row structure
                    /*String deviceIdStr = String.valueOf(row.getValue("deviceId"));
                    Object energyObj = row.getValue("total_energy");
                    */
                    String deviceIdStr = String.valueOf(row[0]);
                    Object energyObj = row[1];
                    Double energyConsumed = (energyObj instanceof Number) ? ((Number) energyObj).doubleValue() : 0.0;

                    if (deviceIdStr != null && !deviceIdStr.equals("null")) {
                        deviceEnergies.add(
                                DeviceEnergy.builder()
                                        .deviceId(Long.valueOf(deviceIdStr))
                                        .energyConsumed(energyConsumed)
                                        .build()
                        );
                    }
                } catch (Exception ex) {
                    log.warn("Failed to parse row from InfluxDB result: {}", ex.getMessage());
                }
            });
        } catch (Exception e) {
            log.error("Failed to execute InfluxDB 3 query: {}", e.getMessage(), e);
            return; // Abort cycle if DB query fails
        }

        log.info("Aggregated device energies over the past hour: {}", deviceEnergies);

        // --- (The rest of your business logic remains completely unchanged) ---

        for (DeviceEnergy deviceEnergy : deviceEnergies) {
            try {
                final DeviceResponse deviceResponse = deviceClient.getDeviceById(deviceEnergy.getDeviceId());
                log.info("Device Response {}", deviceResponse);
                if (deviceResponse == null || deviceResponse.id() == null) {
                    log.warn("Device not found for ID: {}", deviceEnergy.getDeviceId());
                    continue;
                }
                deviceEnergy.setUserId(deviceResponse.userId());
            } catch (Exception e) {
                log.warn("Failed to fetch device for ID: {}", deviceEnergy.getDeviceId());
            }
        }

        // Remove devices with null userId
        deviceEnergies.removeIf(de -> de.getUserId() == null);

        // Get user-device mapping and aggregate per user
        Map<Long, List<DeviceEnergy>> userDeviceEnergyMap =
                deviceEnergies.stream()
                        .collect(Collectors.groupingBy(DeviceEnergy::getUserId));

        log.info("User-Device Energy Map: {}", userDeviceEnergyMap);

        // Get users energy consumption thresholds
        List<Long> userIds = new ArrayList<>(userDeviceEnergyMap.keySet());
        final Map<Long, Double> userThresholdMap = new HashMap<>();
        final Map<Long, String> userEmailMap = new HashMap<>();

        for (final Long userId : userIds) {
            try {
                UserResponse user = userClient.getUserById(userId);
                if (user == null || user.id() == null || !user.alerting()) {
                    log.warn("User not found or alerting disabled for ID: {}", userId);
                    continue;
                }
                userThresholdMap.put(userId, user.energyAlertingThreshold());
                userEmailMap.put(userId, user.email());
            } catch (Exception e) {
                log.warn("Failed to fetch user for ID: {}", userId);
            }
        }
        log.info("User Threshold Map: {}", userThresholdMap);

        // Check thresholds against aggregated usage
        final List<Long> alertedUsers = new ArrayList<>(userThresholdMap.keySet());
        for (final Long userId : alertedUsers) {
            final Double threshold = userThresholdMap.get(userId);
            final List<DeviceEnergy> devices = userDeviceEnergyMap.get(userId);

            final Double totalConsumption = devices.stream()
                    .mapToDouble(DeviceEnergy::getEnergyConsumed)
                    .sum();

            if (totalConsumption > threshold) {
                log.info("ALERT: User ID {} has exceeded the energy threshold! " +
                                "Total Consumption: {}, Threshold: {}",
                        userId, totalConsumption, threshold);

                final AlertingEvent alertingEvent = AlertingEvent.builder()
                        .userId(userId)
                        .message("Energy consumption threshold exceeded")
                        .threshold(threshold)
                        .energyConsumed(totalConsumption)
                        .email(userEmailMap.get(userId))
                        .build();
                kafkaTemplate.send("energy-alerts", alertingEvent);
            } else {
                log.info("User ID {} is within the energy threshold. " +
                                "Total Consumption: {}, Threshold: {}",
                        userId, totalConsumption, threshold);
            }
        }
    }

    public UsageResponse getXDaysUsageForUser(Long userId, int days) {
        log.info("Getting usage for userId {} over past {} days", userId, days);
        final List<DeviceResponse> deviceResponses = deviceClient.getAllDevicesForUser(userId);

        if (deviceResponses == null || deviceResponses.isEmpty()) {
            return UsageResponse.builder()
                    .userId(userId)
                    .devices(List.of())
                    .build();
        }

        final List<Device> devices = deviceResponses.stream()
                .filter(Objects::nonNull)
                .map(dto -> Device.builder()
                        .id(dto.id())
                        .name(dto.name())
                        .type(dto.type())
                        .location(dto.location())
                        .userId(dto.userId())
                        .build())
                .toList();

        final List<String> deviceIdStrings = devices.stream()
                .map(Device::getId)
                .filter(Objects::nonNull)
                .map(String::valueOf)
                .distinct()
                .toList();

        if (deviceIdStrings.isEmpty()) {
            List<DeviceResponse> zeroed = devices.stream()
                    .map(d -> toDeviceResponse(d, 0.0))
                    .toList();
            return UsageResponse.builder()
                    .userId(userId)
                    .devices(zeroed)
                    .build();
        }

        final Instant now = Instant.now();
        final Instant start = now.minusSeconds((long) days * 24 * 3600);

        final String deviceIdsSql = deviceIdStrings.stream()
                .map(id -> "'" + id.replace("'", "''") + "'")
                .collect(Collectors.joining(", "));

        final String sqlQuery = String.format("""
            SELECT "deviceId", SUM("energyConsumed") AS total_energy
            FROM "energy-usage"
            WHERE time >= '%s' AND time < '%s'
              AND "deviceId" IN (%s)
            GROUP BY "deviceId"
            """,
                DateTimeFormatter.ISO_INSTANT.format(start),
                DateTimeFormatter.ISO_INSTANT.format(now),
                deviceIdsSql
        );

        log.debug("Executing InfluxDB SQL Query: {}", sqlQuery);

        final Map<Long, Double> aggregatedMap = new HashMap<>();

        try (Stream<Object[]> rows = influxDBClient.query(sqlQuery)) {
            rows.forEach(row -> {

                if (row == null || row.length < 2 || row[0] == null) {
                    return;
                }
                String deviceIdStr = row[0].toString();
                double energyConsumed = (row[1] instanceof Number num) ? num.doubleValue() : 0.0;

                try {
                    Long deviceId = Long.valueOf(deviceIdStr);
                    aggregatedMap.merge(deviceId, energyConsumed, Double::sum);

                } catch (NumberFormatException nfe) {
                    log.warn("Failed to parse deviceId from query result: {}", deviceIdStr);
                }
            });
        } catch (Exception e) {
            log.error("Failed to query InfluxDB for user {} usage over {} days: {}", userId, days, e.getMessage(), e);
            return UsageResponse.builder()
                    .userId(userId)
                    .devices(List.of())
                    .build();
        }

        log.info("Aggregated energy consumption for userId {}: {}", userId, aggregatedMap);

        final List<DeviceResponse> resultDevices = devices.stream()
                .filter(d -> d.getId() != null)
                .map(d -> toDeviceResponse(d, aggregatedMap.getOrDefault(d.getId(), 0.0)))
                .toList();

        return UsageResponse.builder()
                .userId(userId)
                .devices(resultDevices)
                .build();
    }

    private DeviceResponse toDeviceResponse(Device d, double energyConsumed) {
        return DeviceResponse.builder()
                .id(d.getId())
                .name(d.getName())
                .type(d.getType())
                .location(d.getLocation())
                .userId(d.getUserId())
                .energyConsumed(energyConsumed)
                .build();
    }
}