package com.SkillExchange.user.dto;

import com.SkillExchange.user.skill.dto.UserSkillResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class PublicUserResponse {

    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    private String bio;

    private String profilePicture;

    private List<UserSkillResponse> teachingSkills;

    private List<UserSkillResponse> learningSkills;
}