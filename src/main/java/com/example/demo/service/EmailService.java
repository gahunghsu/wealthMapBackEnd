package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.demo.entity.AlertLog;
import com.example.demo.entity.AlertLog.AlertStatus;
import com.example.demo.repository.AlertLogRepository;

@Service
public class EmailService {

    private static final String MAILJET_API_URL = "https://api.mailjet.com/v3.1/send";

    @Value("${mailjet.api.key}")
    private String apiKey;

    @Value("${mailjet.secret.key}")
    private String secretKey;

    @Value("${mailjet.from.address}")
    private String fromEmail;

    @Autowired
    private AlertLogRepository alertLogRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendSimpleEmail(String to, String subject, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(apiKey, secretKey);

        Map<String, Object> payload = Map.of(
            "Messages", List.of(Map.of(
                "From", Map.of("Email", fromEmail),
                "To", List.of(Map.of("Email", to)),
                "Subject", subject,
                "TextPart", body
            ))
        );

        restTemplate.postForEntity(MAILJET_API_URL, new HttpEntity<>(payload, headers), String.class);
        System.out.println("郵件已發送至: " + to);
    }

    public void sendStrategyEmail(String toEmail, AlertLog logEntry) {
        try {
            sendSimpleEmail(toEmail, logEntry.getTitle(), logEntry.getContent());
            logEntry.setStatus(AlertStatus.SENT);
        } catch (Exception e) {
            logEntry.setStatus(AlertStatus.FAILED);
            logEntry.setErrorMessage(e.getMessage());
        } finally {
            alertLogRepository.save(logEntry);
        }
    }
}
