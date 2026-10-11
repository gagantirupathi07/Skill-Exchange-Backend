package com.SkillExchange.exchange.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRequestRequest {

    @NotNull(message = "Receiver ID is required")
    private Long receiverId;

    @NotNull(message = "Requested skill ID is required")
    private Long requestedSkillId;

    /*
     * Optional.
     * The sender does not have to offer a skill in return.
     */
    private Long offeredSkillId;
}