package com.SkillExchange.exchange.dto;

import com.SkillExchange.exchange.model.ExchangeStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeCompletionResponse {

    private Long exchangeId;

    private Long sessionId;

    private String teacherUsername;

    private String learnerUsername;

    private String skillName;

    private Integer durationMinutes;

    private BigDecimal creditsTransferred;

    private ExchangeStatus exchangeStatus;

    private LocalDateTime completedAt;

    private String message;
}