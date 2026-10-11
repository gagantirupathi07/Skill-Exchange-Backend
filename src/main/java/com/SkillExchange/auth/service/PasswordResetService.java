package com.SkillExchange.auth.service;

import com.SkillExchange.auth.dto.MessageResponse;
import com.SkillExchange.auth.dto.ResetPasswordRequest;
import com.SkillExchange.auth.dto.VerifyResetOtpRequest;
import com.SkillExchange.auth.model.OtpPurpose;
import com.SkillExchange.exception.BadRequestException;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public MessageResponse sendResetOtp(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new BadRequestException(
                                "No account found with this email"
                        )
                );

        otpService.generateAndSendOtp(
                user,
                OtpPurpose.PASSWORD_RESET
        );

        return new MessageResponse(
                "Password reset OTP sent to your email."
        );
    }

    @Transactional
    public MessageResponse verifyResetOtp(
            VerifyResetOtpRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "User not found"
                        )
                );

        otpService.verifyOtp(
                user,
                request.getOtp(),
                OtpPurpose.PASSWORD_RESET
        );

        return new MessageResponse(
                "OTP verified successfully."
        );
    }

    @Transactional
    public MessageResponse resetPassword(
            ResetPasswordRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException(
                                "User not found"
                        )
                );

        boolean otpVerified = otpService
                .isOtpVerified(
                        user,
                        OtpPurpose.PASSWORD_RESET
                );

        if (!otpVerified) {
            throw new BadRequestException(
                    "Please verify the reset OTP first"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        userRepository.save(user);

        otpService.deleteVerifiedOtp(
                user,
                OtpPurpose.PASSWORD_RESET
        );

        return new MessageResponse(
                "Password reset successfully."
        );
    }
}