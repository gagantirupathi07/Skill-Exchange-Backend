package com.SkillExchange.exchange.repository;

import com.SkillExchange.exchange.model.ExchangeRequest;
import com.SkillExchange.exchange.model.ExchangeRequestStatus;
import com.SkillExchange.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExchangeRequestRepository
        extends JpaRepository<ExchangeRequest, Long> {

    List<ExchangeRequest> findBySender(User sender);

    List<ExchangeRequest> findByReceiver(User receiver);

    /*
     * Only one pending request is allowed for:
     *
     * sender
     * receiver
     * requested skill
     *
     * Offered skill is intentionally NOT included
     * because it is optional.
     */
    boolean existsBySenderAndReceiverAndRequestedSkill_IdAndStatus(
            User sender,
            User receiver,
            Long requestedSkillId,
            ExchangeRequestStatus status
    );
}