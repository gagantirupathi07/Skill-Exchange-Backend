package com.SkillExchange.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserSearchResponse {

    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    private String bio;

    private Long skillId;

    private String skillName;

    private String category;
}