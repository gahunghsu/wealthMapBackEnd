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

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.from.address}")
    private String fromEmail;

    @Autowired
    private AlertLogRepository alertLogRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendSimpleEmail(String to, String subject, String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);

        Map<String, Object> payload = Map.of(
            "sender", Map.of("email", fromEmail),
            "to", List.of(Map.of("email", to)),
            "subject", subject,
            "textContent", body
        );

        restTemplate.postForEntity(BREVO_API_URL, new HttpEntity<>(payload, headers), String.class);
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
