package com.SkillExchange.skill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SkillRequest {

    @NotBlank(message = "Skill name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Skill name must be between 2 and 100 characters"
    )
    private String name;

    @Size(
            max = 100,
            message = "Category cannot exceed 100 characters"
    )
    private String category;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;
}