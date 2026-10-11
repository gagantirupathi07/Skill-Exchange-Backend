package com.SkillExchange.dashboard.admin.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardResponse {

    private long totalUsers;
    private long activeUsers;
    private long pendingUsers;
    private long inactiveUsers;
    private long adminUsers;

    private long totalSkills;
    private long activeSkills;
    private long inactiveSkills;


    private long totalTeachingSkillEntries;
    private long totalLearningSkillEntries;


    private long totalExchangeRequests;
    private long pendingExchangeRequests;
    private long acceptedExchangeRequests;
    private long rejectedExchangeRequests;
    private long cancelledExchangeRequests;

    private long totalExchanges;
    private long activeExchanges;
    private long completedExchanges;
    private long cancelledExchanges;
    private long totalSessions;
    private long scheduledSessions;
    private long completedSessions;
    private long cancelledSessions;

    private long totalReviews;
    private double averageRating;
    private BigDecimal totalCreditsInWallets;
    private BigDecimal totalCreditsEarned;
    private BigDecimal totalCreditsSpent;
}