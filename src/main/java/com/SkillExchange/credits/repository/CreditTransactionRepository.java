package com.SkillExchange.credits.repository;

import com.SkillExchange.credits.model.CreditTransaction;
import com.SkillExchange.credits.model.CreditTransactionType;
import com.SkillExchange.exchange.model.Exchange;
import com.SkillExchange.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CreditTransactionRepository
        extends JpaRepository<CreditTransaction, Long> {

    List<CreditTransaction> findByUserOrderByCreatedAtDesc(
            User user
    );

    List<CreditTransaction> findByUser(
            User user
    );

    boolean existsByExchangeAndType(
            Exchange exchange,
            CreditTransactionType type
    );

    List<CreditTransaction> findByExchange(
            Exchange exchange
    );
}