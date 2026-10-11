package com.SkillExchange.auth.service;

import com.SkillExchange.auth.dto.AuthResponse;
import com.SkillExchange.auth.dto.LoginRequest;
import com.SkillExchange.auth.dto.MessageResponse;
import com.SkillExchange.auth.dto.RegisterRequest;
import com.SkillExchange.auth.dto.VerifyOtpRequest;
import com.SkillExchange.auth.model.OtpPurpose;
import com.SkillExchange.credits.service.CreditService;
import com.SkillExchange.exception.BadRequestException;
import com.SkillExchange.security.JwtService;
import com.SkillExchange.user.dto.UserProfileResponse;
import com.SkillExchange.user.model.Role;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.model.UserStatus;
import com.SkillExchange.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final CreditService creditService;

    @Transactional
    public MessageResponse register(RegisterRequest request) {

        // Normalize username before checking and saving.
        String username = request.getUsername() == null
                ? ""
                : request.getUsername().trim();

        if (username.isBlank()) {
            throw new BadRequestException("Username is required");
        }

        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already exists");
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .phone(request.getPhone())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .bio(request.getBio())
                .role(Role.USER)
                .status(UserStatus.PENDING)
                .emailVerified(false)
                .build();

        userRepository.save(user);

        otpService.generateAndSendOtp(
                user,
                OtpPurpose.REGISTRATION
        );

        return new MessageResponse(
                "Registration successful. OTP sent to your email."
        );
    }

    @Transactional
    public MessageResponse resendRegistrationOtp(String email) {

        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new BadRequestException("User not found")
                );

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BadRequestException(
                    "Email is already verified"
            );
        }

        if (user.getStatus() != UserStatus.PENDING) {
            throw new BadRequestException(
                    "Account is not pending verification"
            );
        }

        otpService.generateAndSendOtp(
                user,
                OtpPurpose.REGISTRATION
        );

        return new MessageResponse(
                "New OTP sent to your email."
        );
    }

    @Transactional
    public MessageResponse verifyRegistrationOtp(
            VerifyOtpRequest request
    ) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new BadRequestException("User not found")
                );

        if (Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BadRequestException(
                    "Email is already verified"
            );
        }

        // Verify registration OTP.
        otpService.verifyOtp(
                user,
                request.getOtp(),
                OtpPurpose.REGISTRATION
        );

        // Activate account.
        user.setEmailVerified(true);
        user.setStatus(UserStatus.ACTIVE);

        userRepository.save(user);

        // Initialize credit wallet for the newly activated user.
        creditService.initializeCredits(user);

        return new MessageResponse(
                "Email verified successfully. Account activated."
        );
    }

    public AuthResponse login(LoginRequest request) {

        // Normalize username before searching.
        String username = request.getUsername() == null
                ? ""
                : request.getUsername().trim();

        if (username.isBlank()) {
            throw new BadCredentialsException(
                    "Invalid username or password"
            );
        }

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new BadCredentialsException(
                                "Invalid username or password"
                        )
                );

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new BadRequestException(
                    "Please verify your email before logging in"
            );
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException(
                    "Account is not active"
            );
        }

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new BadCredentialsException(
                    "Invalid username or password"
            );
        }

        String token = jwtService.generateToken(
                user.getUsername()
        );

        return new AuthResponse(
                token,
                "Bearer",
                UserProfileResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .bio(user.getBio())
                        .role(user.getRole())
                        .build()
        );
    }
}