package com.SkillExchange.auth.dto;

import com.SkillExchange.user.dto.UserProfileResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private String token;
    private String tokenType;
    private UserProfileResponse user;
}