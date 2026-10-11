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
public class ExchangeResponse {

    private Long id;

    private Long exchangeRequestId;


    private Long teacherId;

    private String teacherUsername;

    private String teacherFirstName;

    private String teacherLastName;


    private Long learnerId;

    private String learnerUsername;

    private String learnerFirstName;

    private String learnerLastName;


    private Long skillId;

    private String skillName;


    private BigDecimal creditAmount;


    private ExchangeStatus status;


    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}