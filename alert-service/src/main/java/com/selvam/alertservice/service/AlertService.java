package com.selvam.alertservice.service;

import com.selvam.kafka.event.AlertingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AlertService {


    private final EmailService emailService;

    public AlertService(EmailService emailService) {
        this.emailService = emailService;
    }


    @KafkaListener(topics = "energy-alerts", groupId = "alert-service")
    public void energyUsageAlertEvent(AlertingEvent alertingEvent) {
        log.info("Received alert event: {}", alertingEvent);

        //Here you can add additional logic to process the alerting event

        //Send email alert
        final String subject = "Energy Usage alert for User "
                + alertingEvent.userId();

        final String message = "Alert: " + alertingEvent.message() +
                "\nThreshold: " + alertingEvent.threshold() +
                "\nEnergy Consumed: " + alertingEvent.energyConsumed();

        emailService.sendMail(alertingEvent.email(), subject, message, alertingEvent.userId());
    }


}