package com.SkillExchange.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequest {

    @Min(
            value = 1,
            message = "Rating must be at least 1"
    )
    @Max(
            value = 5,
            message = "Rating cannot exceed 5"
    )
    private Integer rating;

    @Size(
            max = 1000,
            message = "Comment cannot exceed 1000 characters"
    )
    private String comment;
}