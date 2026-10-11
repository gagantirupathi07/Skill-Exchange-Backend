package com.SkillExchange.user.dto;

import com.SkillExchange.user.model.Role;
import com.SkillExchange.user.skill.dto.UserSkillResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class UserProfileResponse {

    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private String bio;

    private String profilePicture;

    private Role role;

    private List<UserSkillResponse> teachingSkills;

    private List<UserSkillResponse> learningSkills;
}