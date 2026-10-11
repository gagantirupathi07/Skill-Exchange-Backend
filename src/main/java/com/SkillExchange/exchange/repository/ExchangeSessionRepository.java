package com.SkillExchange.exchange.repository;

import com.SkillExchange.exchange.model.Exchange;
import com.SkillExchange.exchange.model.ExchangeSession;
import com.SkillExchange.exchange.model.SessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExchangeSessionRepository
        extends JpaRepository<ExchangeSession, Long> {

    List<ExchangeSession> findByExchange(Exchange exchange);

    boolean existsByExchangeAndStatus(
            Exchange exchange,
            SessionStatus status
    );

    List<ExchangeSession> findByStatus(
            SessionStatus status
    );

    List<ExchangeSession> findByStatusAndScheduledDateLessThanEqual(
            SessionStatus status,
            LocalDate scheduledDate
    );
}