package com.SkillExchange.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String email, String otp) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject("Skill Exchange - Email Verification OTP");

        message.setText(
                "Your Skill Exchange verification OTP is: " + otp +
                        "\n\nThis OTP is valid for 5 minutes." +
                        "\n\nIf you did not create an account, please ignore this email."
        );

        mailSender.send(message);
    }

    public void sendEmail(
            String email,
            String subject,
            String body
    ) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }
}