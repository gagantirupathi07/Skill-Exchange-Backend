package com.SkillExchange.auth.service;

import com.SkillExchange.auth.model.Otp;
import com.SkillExchange.auth.model.OtpPurpose;
import com.SkillExchange.auth.repository.OtpRepository;
import com.SkillExchange.exception.BadRequestException;
import com.SkillExchange.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final EmailService emailService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public void generateAndSendOtp(User user, OtpPurpose purpose) {

        otpRepository.deleteByUserAndPurpose(user, purpose);

        String otpCode = generateOtp();

        Otp otp = Otp.builder()
                .user(user)
                .otp(otpCode)
                .purpose(purpose)
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build();

        otpRepository.save(otp);

        emailService.sendOtpEmail(
                user.getEmail(),
                otpCode
        );
    }

    public void verifyOtp(
            User user,
            String enteredOtp,
            OtpPurpose purpose
    ) {

        Otp otp = otpRepository
                .findTopByUserAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
                        user,
                        purpose
                )
                .orElseThrow(() ->
                        new BadRequestException("OTP not found")
                );

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired");
        }

        if (!otp.getOtp().equals(enteredOtp)) {
            throw new BadRequestException("Invalid OTP");
        }

        otp.setVerified(true);

        otpRepository.save(otp);
    }

    private String generateOtp() {

        int number = secureRandom.nextInt(1_000_000);

        return String.format("%06d", number);
    }

    public boolean isOtpVerified(
            User user,
            OtpPurpose purpose
    ) {

        return otpRepository
                .findTopByUserAndPurposeAndVerifiedTrueOrderByCreatedAtDesc(
                        user,
                        purpose
                )
                .isPresent();
    }

    @Transactional
    public void deleteVerifiedOtp(
            User user,
            OtpPurpose purpose
    ) {

        otpRepository.deleteByUserAndPurposeAndVerifiedTrue(
                user,
                purpose
        );
    }
}