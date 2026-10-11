package com.SkillExchange.review.service;

import com.SkillExchange.exception.BadRequestException;
import com.SkillExchange.exception.ForbiddenException;
import com.SkillExchange.exchange.model.Exchange;
import com.SkillExchange.exchange.model.ExchangeStatus;
import com.SkillExchange.exchange.repository.ExchangeRepository;
import com.SkillExchange.review.dto.ReviewRequest;
import com.SkillExchange.review.dto.ReviewResponse;
import com.SkillExchange.review.model.Review;
import com.SkillExchange.review.repository.ReviewRepository;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ExchangeRepository exchangeRepository;

    @Transactional
    public ReviewResponse createReview(
            String username,
            Long exchangeId,
            ReviewRequest request
    ) {

        User reviewer =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                ));

        Exchange exchange =
                exchangeRepository.findById(exchangeId)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Exchange not found"
                                ));

        // Exchange must be completed
        if (exchange.getStatus() != ExchangeStatus.COMPLETED) {
            throw new BadRequestException(
                    "Review can only be submitted after the exchange is completed"
            );
        }

        // Reviewer must be part of the exchange
        boolean isTeacher =
                exchange.getTeacher()
                        .getId()
                        .equals(reviewer.getId());

        boolean isLearner =
                exchange.getLearner()
                        .getId()
                        .equals(reviewer.getId());

        if (!isTeacher && !isLearner) {
            throw new ForbiddenException(
                    "Only participants of the exchange can submit a review"
            );
        }

        // Cannot review yourself
        User reviewee;

        if (isTeacher) {
            reviewee = exchange.getLearner();
        } else {
            reviewee = exchange.getTeacher();
        }

        if (reviewee.getId().equals(reviewer.getId())) {
            throw new BadRequestException(
                    "You cannot review yourself"
            );
        }

        // Prevent duplicate review
        if (reviewRepository.existsByExchangeAndReviewer(
                exchange,
                reviewer
        )) {
            throw new BadRequestException(
                    "You have already reviewed this exchange"
            );
        }

        // Extra validation
        if (request.getRating() == null) {
            throw new BadRequestException(
                    "Rating is required"
            );
        }

        if (request.getRating() < 1 ||
                request.getRating() > 5) {

            throw new BadRequestException(
                    "Rating must be between 1 and 5"
            );
        }

        Review review =
                Review.builder()
                        .exchange(exchange)
                        .reviewer(reviewer)
                        .reviewee(reviewee)
                        .rating(request.getRating())
                        .comment(request.getComment())
                        .build();

        Review savedReview =
                reviewRepository.save(review);

        return mapToResponse(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsForUser(
            Long userId
    ) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                ));

        return reviewRepository
                .findByRevieweeOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getMyReviews(
            String username
    ) {

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                ));

        return reviewRepository
                .findByReviewerOrderByCreatedAtDesc(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getExchangeReviews(
            Long exchangeId
    ) {

        Exchange exchange =
                exchangeRepository.findById(exchangeId)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Exchange not found"
                                ));

        return reviewRepository
                .findByExchange(exchange)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ReviewResponse mapToResponse(
            Review review
    ) {

        User reviewer = review.getReviewer();
        User reviewee = review.getReviewee();
        Exchange exchange = review.getExchange();

        return ReviewResponse.builder()
                .id(review.getId())

                .exchangeId(exchange.getId())

                .reviewerId(reviewer.getId())
                .reviewerUsername(reviewer.getUsername())
                .reviewerFirstName(reviewer.getFirstName())
                .reviewerLastName(reviewer.getLastName())

                .revieweeId(reviewee.getId())
                .revieweeUsername(reviewee.getUsername())
                .revieweeFirstName(reviewee.getFirstName())
                .revieweeLastName(reviewee.getLastName())

                .rating(review.getRating())
                .comment(review.getComment())

                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())

                .build();
    }
}