package com.SkillExchange.auth.repository;

import com.SkillExchange.auth.model.Otp;
import com.SkillExchange.auth.model.OtpPurpose;
import com.SkillExchange.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    void deleteByUserAndPurpose(
            User user,
            OtpPurpose purpose
    );

    Optional<Otp> findTopByUserAndPurposeAndVerifiedFalseOrderByCreatedAtDesc(
            User user,
            OtpPurpose purpose
    );

    Optional<Otp> findTopByUserAndPurposeAndVerifiedTrueOrderByCreatedAtDesc(
            User user,
            OtpPurpose purpose
    );

    void deleteByUserAndPurposeAndVerifiedTrue(
            User user,
            OtpPurpose purpose
    );
}