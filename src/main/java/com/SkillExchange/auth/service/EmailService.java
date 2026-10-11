package com.SkillExchange.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    private static final String BREVO_API_URL =
            "https://api.brevo.com/v3/smtp/email";

    private final RestClient restClient;
    private final String apiKey;
    private final String senderEmail;

    public EmailService(
            @Value("${BREVO_API_KEY}") String apiKey,
            @Value("${BREVO_SENDER_EMAIL}") String senderEmail
    ) {
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;

        this.restClient = RestClient.builder()
                .baseUrl(BREVO_API_URL)
                .defaultHeader("api-key", apiKey)
                .defaultHeader(
                        HttpHeaders.CONTENT_TYPE,
                        MediaType.APPLICATION_JSON_VALUE
                )
                .build();
    }

    public void sendOtpEmail(String email, String otp) {

        String subject =
                "Skill Exchange - Email Verification OTP";

        String body =
                "Your Skill Exchange verification OTP is: " + otp +
                "\n\nThis OTP is valid for 5 minutes." +
                "\n\nIf you did not create an account, please ignore this email.";

        sendEmail(email, subject, body);
    }

    public void sendEmail(
            String email,
            String subject,
            String body
    ) {

        Map<String, Object> requestBody = Map.of(
                "sender", Map.of(
                        "name", "Skill Exchange",
                        "email", senderEmail
                ),
                "to", List.of(
                        Map.of("email", email)
                ),
                "subject", subject,
                "textContent", body
        );

        try {
            restClient.post()
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException exception) {
            throw new IllegalStateException(
                    "Failed to send email through Brevo. " +
                    "Check your Brevo API key, sender verification, " +
                    "and Railway logs.",
                    exception
            );
        }
    }
}
