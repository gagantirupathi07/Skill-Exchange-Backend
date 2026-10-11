package com.SkillExchange.skill.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class SkillResponse {

    private Long id;

    private String name;

    private String category;

    private String description;

    private Boolean isActive;

    private java.time.LocalDateTime createdAt;

    private java.time.LocalDateTime updatedAt;
}