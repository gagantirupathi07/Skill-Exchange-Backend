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
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExchangeAutoCompletionScheduler {

    private final ExchangeSessionRepository exchangeSessionRepository;
    private final ExchangeService exchangeService;

    /**
     * Checks for ended sessions every 60 seconds.
     */
    @Scheduled(fixedDelay = 60000)
    public void completeEndedSessions() {

        LocalDateTime now = LocalDateTime.now();

        log.info("AUTO-COMPLETION SCHEDULER RUNNING at {}", now);

        List<ExchangeSession> sessions =
                exchangeSessionRepository
                        .findByStatusAndScheduledDateLessThanEqual(
                                SessionStatus.SCHEDULED,
                                LocalDate.now()
                        );

        log.info(
                "AUTO-COMPLETION: Found {} scheduled sessions to inspect",
                sessions.size()
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
                        "AUTO-COMPLETION: Completing session {}",
                        session.getId()
                );

                exchangeService.completeExchangeAutomatically(
                        session.getId()
                );

                log.info(
                        "AUTO-COMPLETION: Processing finished for session {}",
                        session.getId()
                );

            } catch (Exception exception) {
                log.error(
                        "AUTO-COMPLETION: Failed for session {}",
                        session.getId(),
                        exception
                );
            }
        }
    }
}