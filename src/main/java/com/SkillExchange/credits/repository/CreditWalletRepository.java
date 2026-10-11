package com.SkillExchange.credits.repository;

import com.SkillExchange.credits.model.CreditWallet;
import com.SkillExchange.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CreditWalletRepository
        extends JpaRepository<CreditWallet, Long> {

    Optional<CreditWallet> findByUser(User user);

    boolean existsByUser(User user);
}