package com.SkillExchange.notification.controller;

import com.SkillExchange.notification.dto.NotificationResponse;
import com.SkillExchange.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                notificationService.getMyNotifications(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationResponse>> getUnreadNotifications(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                notificationService.getUnreadNotifications(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                notificationService.getUnreadCount(
                        authentication.getName()
                )
        );
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            Authentication authentication,
            @PathVariable Long notificationId
    ) {

        return ResponseEntity.ok(
                notificationService.markAsRead(
                        authentication.getName(),
                        notificationId
                )
        );
    }

    @PutMapping("/read-all")
    public ResponseEntity<String> markAllAsRead(
            Authentication authentication
    ) {

        notificationService.markAllAsRead(
                authentication.getName()
        );

        return ResponseEntity.ok(
                "All notifications marked as read"
        );
    }
}