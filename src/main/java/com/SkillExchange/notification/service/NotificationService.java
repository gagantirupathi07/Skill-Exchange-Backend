package com.SkillExchange.notification.service;

import com.SkillExchange.auth.service.EmailService;
import com.SkillExchange.exception.BadRequestException;
import com.SkillExchange.notification.dto.NotificationResponse;
import com.SkillExchange.notification.model.Notification;
import com.SkillExchange.notification.model.NotificationType;
import com.SkillExchange.notification.repository.NotificationRepository;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public NotificationResponse createNotification(
            User user,
            NotificationType type,
            String title,
            String message,
            Long referenceId
    ) {

        Notification notification =
                Notification.builder()
                        .user(user)
                        .type(type)
                        .title(title)
                        .message(message)
                        .referenceId(referenceId)
                        .read(false)
                        .build();

        Notification savedNotification =
                notificationRepository.save(notification);

        sendEmail(user, title, message);

        return mapToResponse(savedNotification);
    }

    @Transactional
    public NotificationResponse createNotification(
            String username,
            NotificationType type,
            String title,
            String message,
            Long referenceId
    ) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                ));

        return createNotification(
                user,
                type,
                title,
                message,
                referenceId
        );
    }

    private void sendEmail(
            User user,
            String subject,
            String message
    ) {

        if (user.getEmail() == null ||
                user.getEmail().isBlank()) {
            return;
        }

        emailService.sendEmail(
                user.getEmail(),
                subject,
                message
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(
            String username
    ) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                ));

        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(
            String username
    ) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                ));

        return notificationRepository
                .findByUserAndReadFalseOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getUnreadCount(
            String username
    ) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                ));

        return notificationRepository
                .countByUserAndReadFalse(user);
    }

    @Transactional
    public NotificationResponse markAsRead(
            String username,
            Long notificationId
    ) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                ));

        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Notification not found"
                                ));

        if (!notification.getUser().getId()
                .equals(user.getId())) {

            throw new BadRequestException(
                    "You cannot modify this notification"
            );
        }

        notification.setRead(true);

        Notification savedNotification =
                notificationRepository.save(notification);

        return mapToResponse(savedNotification);
    }

    @Transactional
    public void markAllAsRead(
            String username
    ) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                ));

        List<Notification> notifications =
                notificationRepository
                        .findByUserAndReadFalseOrderByCreatedAtDesc(
                                user
                        );

        notifications.forEach(notification ->
                notification.setRead(true)
        );

        notificationRepository.saveAll(notifications);
    }

    private NotificationResponse mapToResponse(
            Notification notification
    ) {

        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .read(notification.isRead())
                .referenceId(notification.getReferenceId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}