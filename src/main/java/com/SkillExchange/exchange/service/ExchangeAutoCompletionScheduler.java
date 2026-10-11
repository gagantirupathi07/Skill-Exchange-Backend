package com.SkillExchange.exchange.service;

import com.SkillExchange.exchange.model.ExchangeSession;
import com.SkillExchange.exchange.model.SessionStatus;
import com.SkillExchange.exchange.repository.ExchangeSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeAutoCompletionScheduler {

    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Kolkata");

    private final ExchangeSessionRepository exchangeSessionRepository;
    private final ExchangeService exchangeService;

    /**
     * Checks for ended sessions every 60 seconds.
     */
    @Scheduled(fixedDelay = 60000)
    public void completeEndedSessions() {

        LocalDate today = LocalDate.now(ZONE_ID);
        LocalDateTime now = LocalDateTime.now(ZONE_ID);

        log.info(
                "AUTO-COMPLETION SCHEDULER RUNNING: IST time={}",
                now
        );

        List<ExchangeSession> sessions =
                exchangeSessionRepository
                        .findByStatusAndScheduledDateLessThanEqual(
                                SessionStatus.SCHEDULED,
                                today
                        );

        log.info(
                "AUTO-COMPLETION: Found {} scheduled sessions to inspect",
                sessions.size()
        );

        log.info(
                "AUTO-COMPLETION DEBUG: Current IST date={}, session count={}",
                today,
                sessions.size()
        );

        // Diagnostic logging for all scheduled sessions.
        List<ExchangeSession> allScheduledSessions =
                exchangeSessionRepository.findByStatus(
                        SessionStatus.SCHEDULED
                );

        log.info(
                "AUTO-COMPLETION DEBUG: All SCHEDULED sessions={}",
                allScheduledSessions.stream()
                        .map(session -> String.format(
                                "id=%s, date=%s, endTime=%s, exchangeId=%s",
                                session.getId(),
                                session.getScheduledDate(),
                                session.getEndTime(),
                                session.getExchange() != null
                                        ? session.getExchange().getId()
                                        : null
                        ))
                        .toList()
        );

        for (ExchangeSession session : sessions) {

            if (session.getScheduledDate() == null
                    || session.getEndTime() == null) {

                log.warn(
                        "AUTO-COMPLETION: Session {} has no date or end time",
                        session.getId()
                );

                continue;
            }

            LocalDateTime sessionEnd = LocalDateTime.of(
                    session.getScheduledDate(),
                    session.getEndTime()
            );

            log.info(
                    "AUTO-COMPLETION: Checking session {}: current IST time={}, session end IST={}",
                    session.getId(),
                    now,
                    sessionEnd
            );

            if (now.isBefore(sessionEnd)) {

                log.info(
                        "AUTO-COMPLETION: Session {} ends at {}; not due yet",
                        session.getId(),
                        sessionEnd
                );

                continue;
            }

            try {

                log.info(
                        "AUTO-COMPLETION: Session {} has ended. Starting automatic completion.",
                        session.getId()
                );

                exchangeService.completeExchangeAutomatically(
                        session.getId()
                );

                log.info(
                        "AUTO-COMPLETION: Automatic completion processing finished for session {}",
                        session.getId()
                );

            } catch (Exception exception) {

                log.error(
                        "AUTO-COMPLETION: Failed to automatically complete session {}",
                        session.getId(),
                        exception
                );
            }
        }
    }
}
