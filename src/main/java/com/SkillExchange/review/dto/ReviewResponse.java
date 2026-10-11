package com.SkillExchange.review.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;

    private Long exchangeId;

    private Long reviewerId;
    private String reviewerUsername;
    private String reviewerFirstName;
    private String reviewerLastName;

    private Long revieweeId;
    private String revieweeUsername;
    private String revieweeFirstName;
    private String revieweeLastName;

    private Integer rating;

    private String comment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}