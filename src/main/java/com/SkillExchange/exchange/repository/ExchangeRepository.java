package com.SkillExchange.exchange.repository;

import com.SkillExchange.exchange.model.Exchange;
import com.SkillExchange.exchange.model.ExchangeRequest;
import com.SkillExchange.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExchangeRepository
        extends JpaRepository<Exchange, Long> {

    /*
     * Find the Exchange created from a specific
     * ExchangeRequest.
     */
    Optional<Exchange> findByExchangeRequest(
            ExchangeRequest exchangeRequest
    );

    /*
     * Check whether an Exchange already exists
     * for a specific ExchangeRequest.
     *
     * This prevents the same accepted request
     * from creating multiple Exchanges.
     */
    boolean existsByExchangeRequest(
            ExchangeRequest exchangeRequest
    );

    /*
     * Get all Exchanges where the user is the teacher.
     */
    List<Exchange> findByTeacher(
            User teacher
    );

    /*
     * Get all Exchanges where the user is the learner.
     */
    List<Exchange> findByLearner(
            User learner
    );

    /*
     * Get all Exchanges where the user is either
     * the teacher OR the learner.
     */
    List<Exchange> findByTeacherOrLearner(
            User teacher,
            User learner
    );
}