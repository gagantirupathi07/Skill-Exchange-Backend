package com.SkillExchange.exchange.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeSessionRequest {

    @NotNull(message = "Scheduled date is required")
    @FutureOrPresent(message = "Scheduled date cannot be in the past")
    private LocalDate scheduledDate;

    @NotNull(message = "Start time is required")
    private LocalTime startTime;

    @NotNull(message = "End time is required")
    private LocalTime endTime;

    @NotBlank(message = "Meeting platform is required")
    @Size(max = 50, message = "Meeting platform cannot exceed 50 characters")
    private String meetingPlatform;

    @NotBlank(message = "Meeting link is required")
    @Size(max = 1000, message = "Meeting link cannot exceed 1000 characters")
    private String meetingLink;

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    private String notes;
}