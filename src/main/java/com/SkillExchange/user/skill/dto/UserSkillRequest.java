package com.SkillExchange.user.skill.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSkillRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;
}