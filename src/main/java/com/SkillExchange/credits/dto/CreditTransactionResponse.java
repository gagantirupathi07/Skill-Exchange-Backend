package com.SkillExchange.credits.dto;

import com.SkillExchange.credits.model.CreditTransactionType;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditTransactionResponse {

    private Long id;

    private Long userId;

    private String username;

    private Long exchangeId;

    private CreditTransactionType type;

    private BigDecimal amount;

    private BigDecimal balanceAfter;

    private String description;

    private LocalDateTime createdAt;
}