package com.SkillExchange.exchange.dto;

import com.SkillExchange.exchange.model.ExchangeRequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRequestResponse {

    private Long id;

    private Long senderId;

    private String senderUsername;

    private String senderFirstName;

    private String senderLastName;

    private Long receiverId;

    private String receiverUsername;

    private String receiverFirstName;

    private String receiverLastName;

    private Long requestedSkillId;

    private String requestedSkillName;

    private Long offeredSkillId;

    private String offeredSkillName;

    private ExchangeRequestStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}