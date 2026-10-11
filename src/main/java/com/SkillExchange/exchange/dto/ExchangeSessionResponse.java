package com.SkillExchange.exchange.dto;

import com.SkillExchange.exchange.model.SessionStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeSessionResponse {

    private Long id;

    private Long exchangeId;

    private Long teacherId;
    private String teacherUsername;

    private Long learnerId;
    private String learnerUsername;

    private Long skillId;
    private String skillName;

    private LocalDate scheduledDate;

    private LocalTime startTime;
    private LocalTime endTime;

    private Integer durationMinutes;

    private String meetingPlatform;
    private String meetingLink;

    private String notes;

    private SessionStatus status;

    private boolean teacherConfirmed;
    private LocalDateTime teacherConfirmedAt;

    private boolean learnerConfirmed;
    private LocalDateTime learnerConfirmedAt;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}