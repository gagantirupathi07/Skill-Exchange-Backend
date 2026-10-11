package com.SkillExchange.review.controller;

import com.SkillExchange.review.dto.ReviewRequest;
import com.SkillExchange.review.dto.ReviewResponse;
import com.SkillExchange.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/exchange/{exchangeId}")
    public ResponseEntity<ReviewResponse> createReview(
            Authentication authentication,
            @PathVariable Long exchangeId,
            @Valid @RequestBody ReviewRequest request
    ) {

        String username = authentication.getName();

        ReviewResponse response =
                reviewService.createReview(
                        username,
                        exchangeId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getReviewsForUser(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                reviewService.getReviewsForUser(userId)
        );
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                reviewService.getMyReviews(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/exchange/{exchangeId}")
    public ResponseEntity<List<ReviewResponse>> getExchangeReviews(
            @PathVariable Long exchangeId
    ) {

        return ResponseEntity.ok(
                reviewService.getExchangeReviews(exchangeId)
        );
    }
}