package com.SkillExchange.notification.dto;

import com.SkillExchange.notification.model.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponse {

    private Long id;

    private Long userId;

    private NotificationType type;

    private String title;

    private String message;

    private boolean read;

    private Long referenceId;

    private LocalDateTime createdAt;
}