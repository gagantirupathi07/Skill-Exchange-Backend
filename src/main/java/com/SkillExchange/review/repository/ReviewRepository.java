package com.SkillExchange.review.repository;

import com.SkillExchange.exchange.model.Exchange;
import com.SkillExchange.user.model.User;
import com.SkillExchange.review.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    boolean existsByExchangeAndReviewer(
            Exchange exchange,
            User reviewer
    );

    List<Review> findByRevieweeOrderByCreatedAtDesc(
            User reviewee
    );

    List<Review> findByReviewerOrderByCreatedAtDesc(
            User reviewer
    );

    List<Review> findByExchange(
            Exchange exchange
    );
}