package com.selvam.alertservice.service;

import com.selvam.alertservice.entity.Alert;
import com.selvam.alertservice.repository.AlertRepository;
import com.selvam.kafka.event.AlertingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final AlertRepository alertRepository;

    public EmailService(JavaMailSender mailSender, AlertRepository alertRepository) {
        this.mailSender = mailSender;
        this.alertRepository = alertRepository;
    }

    public void sendMail(String to,
                         String subject,
                         String body,
                         Long userId) {
        log.info("Sending email to: {}, subject: {},", to, subject);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setFrom("noreply@selvam.com");
        message.setSubject(subject);
        message.setText(body);

        try {
            mailSender.send(message);

            final Alert alertSent = Alert.builder()
                    .sent(true)
                    .createdAt(LocalDateTime.now())
                    .userId(userId)
                    .build();

            //save it on SQL DB
            alertRepository.saveAndFlush(alertSent);
        } catch (MailException e) {
            log.error("Failed to send email: {}", to, e);
            //save it on SQL DB
            final Alert alertSent = Alert.builder()
                    .sent(false)
                    .createdAt(LocalDateTime.now())
                    .userId(userId)
                    .build();
            alertRepository.saveAndFlush(alertSent);
            return;
        }
        log.info("Email sent to: {}", to);
    }
}