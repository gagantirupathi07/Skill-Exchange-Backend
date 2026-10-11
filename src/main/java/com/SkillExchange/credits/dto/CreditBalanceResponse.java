package com.SkillExchange.credits.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditBalanceResponse {

    private Long walletId;

    private Long userId;

    private String username;

    private BigDecimal balance;
}