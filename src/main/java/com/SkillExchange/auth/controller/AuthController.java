package com.SkillExchange.auth.controller;

import com.SkillExchange.auth.dto.*;
import com.SkillExchange.auth.service.AuthService;
import com.SkillExchange.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private final PasswordResetService passwordResetService;


    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<MessageResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {

        return ResponseEntity.ok(
                authService.verifyRegistrationOtp(request)
        );
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<MessageResponse> resendOtp(
            @Valid @RequestBody ResendOtpRequest request
    ) {

        return ResponseEntity.ok(
                authService.resendRegistrationOtp(
                        request.getEmail()
                )
        );
    }


    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request
    ) {

        return ResponseEntity.ok(
                passwordResetService.sendResetOtp(
                        request.getEmail()
                )
        );
    }


    @PostMapping("/verify-reset-otp")
    public ResponseEntity<MessageResponse> verifyResetOtp(
            @Valid @RequestBody VerifyResetOtpRequest request
    ) {

        return ResponseEntity.ok(
                passwordResetService.verifyResetOtp(request)
        );
    }


    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {

        return ResponseEntity.ok(
                passwordResetService.resetPassword(request)
        );
    }

}
