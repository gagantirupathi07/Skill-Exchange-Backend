package com.SkillExchange.user.skill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UserSkillResponse {

    private Long id;
    private Long skillId;
    private String skillName;
    private String category;
}