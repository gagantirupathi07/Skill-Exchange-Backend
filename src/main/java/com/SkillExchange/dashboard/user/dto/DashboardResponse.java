package com.SkillExchange.dashboard.user.dto;

import com.SkillExchange.exchange.dto.ExchangeResponse;
import com.SkillExchange.exchange.dto.ExchangeSessionResponse;
import com.SkillExchange.review.dto.ReviewResponse;
import com.SkillExchange.user.dto.MatchResponse;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardResponse {

    // =========================
    // USER
    // =========================

    private Long userId;
    private String username;
    private String firstName;
    private String lastName;
    private String email;

    // =========================
    // CREDITS
    // =========================

    private BigDecimal creditBalance;

    // =========================
    // SKILLS
    // =========================

    private List<String> teachingSkills;
    private List<String> learningSkills;

    // =========================
    // MATCHES
    // =========================

    private List<MatchResponse> matches;

    // =========================
    // EXCHANGE REQUESTS
    // =========================

    private long sentRequestCount;
    private long receivedRequestCount;
    private long pendingReceivedRequestCount;

    // =========================
    // EXCHANGES
    // =========================

    private long activeExchangeCount;
    private long completedExchangeCount;
    private long cancelledExchangeCount;

    private List<ExchangeResponse> activeExchanges;

    // =========================
    // SESSIONS
    // =========================

    private List<ExchangeSessionResponse> upcomingSessions;

    // =========================
    // REVIEWS
    // =========================

    private List<ReviewResponse> receivedReviews;
    private List<ReviewResponse> myReviews;

    // =========================
    // NOTIFICATIONS
    // =========================

    private long unreadNotificationCount;
}
