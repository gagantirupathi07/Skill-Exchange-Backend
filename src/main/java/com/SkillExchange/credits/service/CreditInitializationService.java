package com.SkillExchange.credits.service;

import com.SkillExchange.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreditInitializationService {

    private final CreditService creditService;


    /*
     * Initialize the user's wallet
     * when the account becomes active.
     */
    @Transactional
    public void initializeForUser(
            User user
    ) {

        creditService.initializeCredits(user);
    }
}