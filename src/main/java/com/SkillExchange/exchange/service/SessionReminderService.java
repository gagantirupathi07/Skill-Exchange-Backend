package com.SkillExchange.exchange.service;

import com.SkillExchange.exchange.model.ExchangeSession;
import com.SkillExchange.exchange.model.SessionStatus;
import com.SkillExchange.exchange.repository.ExchangeSessionRepository;
import com.SkillExchange.notification.model.NotificationType;
import com.SkillExchange.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionReminderService {

    private final ExchangeSessionRepository exchangeSessionRepository;

    private final NotificationService notificationService;


    /*
     * Runs every minute.
     */
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendSessionReminders() {

        LocalDateTime now =
                LocalDateTime.now();

        LocalDateTime reminderLimit =
                now.plusMinutes(10);

        List<ExchangeSession> sessions =
                exchangeSessionRepository
                        .findByStatus(
                                SessionStatus.SCHEDULED
                        );

        for (ExchangeSession session : sessions) {

            if (session.isReminderSent()) {
                continue;
            }

            LocalDateTime sessionStart =
                    LocalDateTime.of(
                            session.getScheduledDate(),
                            session.getStartTime()
                    );

            /*
             * Send reminder if the session starts
             * within the next 10 minutes.
             */
            if (!sessionStart.isBefore(now)
                    && !sessionStart.isAfter(reminderLimit)) {

                var exchange =
                        session.getExchange();

                notificationService.createNotification(
                        exchange.getTeacher(),
                        NotificationType.SESSION_REMINDER,
                        "Session Starting Soon",
                        "Your "
                                + exchange.getSkill().getName()
                                + " session starts in about 10 minutes.",
                        session.getId()
                );

                notificationService.createNotification(
                        exchange.getLearner(),
                        NotificationType.SESSION_REMINDER,
                        "Session Starting Soon",
                        "Your "
                                + exchange.getSkill().getName()
                                + " session starts in about 10 minutes.",
                        session.getId()
                );

                session.setReminderSent(true);

                exchangeSessionRepository.save(session);
            }
        }
    }
}