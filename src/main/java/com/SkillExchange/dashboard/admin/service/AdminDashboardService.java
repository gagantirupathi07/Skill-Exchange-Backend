package com.SkillExchange.dashboard.admin.service;

import com.SkillExchange.credits.model.CreditTransaction;
import com.SkillExchange.credits.model.CreditTransactionType;
import com.SkillExchange.credits.model.CreditWallet;
import com.SkillExchange.credits.repository.CreditTransactionRepository;
import com.SkillExchange.credits.repository.CreditWalletRepository;
import com.SkillExchange.dashboard.admin.dto.AdminDashboardResponse;
import com.SkillExchange.exchange.model.Exchange;
import com.SkillExchange.exchange.model.ExchangeRequest;
import com.SkillExchange.exchange.model.ExchangeRequestStatus;
import com.SkillExchange.exchange.model.ExchangeSession;
import com.SkillExchange.exchange.model.ExchangeStatus;
import com.SkillExchange.exchange.model.SessionStatus;
import com.SkillExchange.exchange.repository.ExchangeRepository;
import com.SkillExchange.exchange.repository.ExchangeRequestRepository;
import com.SkillExchange.exchange.repository.ExchangeSessionRepository;
import com.SkillExchange.review.model.Review;
import com.SkillExchange.review.repository.ReviewRepository;
import com.SkillExchange.skill.repository.SkillRepository;
import com.SkillExchange.user.model.Role;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.model.UserStatus;
import com.SkillExchange.user.repository.UserRepository;
import com.SkillExchange.user.skill.repository.UserLearningSkillRepository;
import com.SkillExchange.user.skill.repository.UserTeachingSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;

    private final SkillRepository skillRepository;

    private final UserTeachingSkillRepository teachingSkillRepository;
    private final UserLearningSkillRepository learningSkillRepository;

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final ExchangeRepository exchangeRepository;
    private final ExchangeSessionRepository exchangeSessionRepository;

    private final ReviewRepository reviewRepository;

    private final CreditWalletRepository creditWalletRepository;
    private final CreditTransactionRepository creditTransactionRepository;


    // =========================================================
    // GET ADMIN DASHBOARD
    // =========================================================

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {


        // =====================================================
        // USERS
        // =====================================================

        List<User> users =
                userRepository.findAll();

        long totalUsers =
                users.size();

        long activeUsers =
                users.stream()
                        .filter(user ->
                                user.getStatus()
                                        == UserStatus.ACTIVE)
                        .count();

        long pendingUsers =
                users.stream()
                        .filter(user ->
                                user.getStatus()
                                        == UserStatus.PENDING)
                        .count();

        long inactiveUsers =
                users.stream()
                        .filter(user ->
                                user.getStatus()
                                        != UserStatus.ACTIVE
                                        &&
                                        user.getStatus()
                                                != UserStatus.PENDING)
                        .count();

        long adminUsers =
                users.stream()
                        .filter(user ->
                                user.getRole()
                                        == Role.ADMIN)
                        .count();


        // =====================================================
        // SKILLS
        // =====================================================

        long totalSkills =
                skillRepository.count();

        long activeSkills =
                skillRepository.countByIsActive(true);

        long inactiveSkills =
                skillRepository.countByIsActive(false);


        // =====================================================
        // USER SKILL ASSOCIATIONS
        // =====================================================

        long totalTeachingSkillEntries =
                teachingSkillRepository.count();

        long totalLearningSkillEntries =
                learningSkillRepository.count();


        // =====================================================
        // EXCHANGE REQUESTS
        // =====================================================

        List<ExchangeRequest> exchangeRequests =
                exchangeRequestRepository.findAll();

        long totalExchangeRequests =
                exchangeRequests.size();

        long pendingExchangeRequests =
                countRequestsByStatus(
                        exchangeRequests,
                        ExchangeRequestStatus.PENDING
                );

        long acceptedExchangeRequests =
                countRequestsByStatus(
                        exchangeRequests,
                        ExchangeRequestStatus.ACCEPTED
                );

        long rejectedExchangeRequests =
                countRequestsByStatus(
                        exchangeRequests,
                        ExchangeRequestStatus.REJECTED
                );

        long cancelledExchangeRequests =
                countRequestsByStatus(
                        exchangeRequests,
                        ExchangeRequestStatus.CANCELLED
                );


        // =====================================================
        // EXCHANGES
        // =====================================================

        List<Exchange> exchanges =
                exchangeRepository.findAll();

        long totalExchanges =
                exchanges.size();

        long activeExchanges =
                countExchangesByStatus(
                        exchanges,
                        ExchangeStatus.ACTIVE
                );

        long completedExchanges =
                countExchangesByStatus(
                        exchanges,
                        ExchangeStatus.COMPLETED
                );

        long cancelledExchanges =
                countExchangesByStatus(
                        exchanges,
                        ExchangeStatus.CANCELLED
                );


        // =====================================================
        // EXCHANGE SESSIONS
        // =====================================================

        List<ExchangeSession> sessions =
                exchangeSessionRepository.findAll();

        long totalSessions =
                sessions.size();

        long scheduledSessions =
                countSessionsByStatus(
                        sessions,
                        SessionStatus.SCHEDULED
                );

        long completedSessions =
                countSessionsByStatus(
                        sessions,
                        SessionStatus.COMPLETED
                );

        long cancelledSessions =
                countSessionsByStatus(
                        sessions,
                        SessionStatus.CANCELLED
                );


        // =====================================================
        // REVIEWS
        // =====================================================

        List<Review> reviews =
                reviewRepository.findAll();

        long totalReviews =
                reviews.size();

        double averageRating =
                reviews.isEmpty()
                        ? 0.0
                        : reviews.stream()
                        .mapToInt(Review::getRating)
                        .average()
                        .orElse(0.0);

        averageRating =
                BigDecimal.valueOf(averageRating)
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        )
                        .doubleValue();


        // =====================================================
        // CREDIT WALLETS
        // =====================================================

        List<CreditWallet> wallets =
                creditWalletRepository.findAll();

        BigDecimal totalCreditsInWallets =
                wallets.stream()
                        .map(CreditWallet::getBalance)
                        .filter(balance ->
                                balance != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // =====================================================
        // CREDIT TRANSACTIONS
        // =====================================================

        List<CreditTransaction> transactions =
                creditTransactionRepository.findAll();

        BigDecimal totalCreditsEarned =
                transactions.stream()
                        .filter(transaction ->
                                transaction.getType()
                                        == CreditTransactionType.EARN)
                        .map(CreditTransaction::getAmount)
                        .filter(amount ->
                                amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );

        BigDecimal totalCreditsSpent =
                transactions.stream()
                        .filter(transaction ->
                                transaction.getType()
                                        == CreditTransactionType.SPEND)
                        .map(CreditTransaction::getAmount)
                        .filter(amount ->
                                amount != null)
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        )
                        .setScale(
                                2,
                                RoundingMode.HALF_UP
                        );


        // =====================================================
        // BUILD RESPONSE
        // =====================================================

        return AdminDashboardResponse.builder()

                // USERS
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .pendingUsers(pendingUsers)
                .inactiveUsers(inactiveUsers)
                .adminUsers(adminUsers)

                // SKILLS
                .totalSkills(totalSkills)
                .activeSkills(activeSkills)
                .inactiveSkills(inactiveSkills)

                // USER SKILL ASSOCIATIONS
                .totalTeachingSkillEntries(
                        totalTeachingSkillEntries
                )
                .totalLearningSkillEntries(
                        totalLearningSkillEntries
                )

                // EXCHANGE REQUESTS
                .totalExchangeRequests(
                        totalExchangeRequests
                )
                .pendingExchangeRequests(
                        pendingExchangeRequests
                )
                .acceptedExchangeRequests(
                        acceptedExchangeRequests
                )
                .rejectedExchangeRequests(
                        rejectedExchangeRequests
                )
                .cancelledExchangeRequests(
                        cancelledExchangeRequests
                )

                // EXCHANGES
                .totalExchanges(totalExchanges)
                .activeExchanges(activeExchanges)
                .completedExchanges(completedExchanges)
                .cancelledExchanges(cancelledExchanges)

                // SESSIONS
                .totalSessions(totalSessions)
                .scheduledSessions(scheduledSessions)
                .completedSessions(completedSessions)
                .cancelledSessions(cancelledSessions)

                // REVIEWS
                .totalReviews(totalReviews)
                .averageRating(averageRating)

                // CREDITS
                .totalCreditsInWallets(
                        totalCreditsInWallets
                )
                .totalCreditsEarned(
                        totalCreditsEarned
                )
                .totalCreditsSpent(
                        totalCreditsSpent
                )

                .build();
    }


    // =========================================================
    // COUNT EXCHANGE REQUESTS BY STATUS
    // =========================================================

    private long countRequestsByStatus(
            List<ExchangeRequest> requests,
            ExchangeRequestStatus status
    ) {

        return requests.stream()
                .filter(request ->
                        request.getStatus() == status)
                .count();
    }


    // =========================================================
    // COUNT EXCHANGES BY STATUS
    // =========================================================

    private long countExchangesByStatus(
            List<Exchange> exchanges,
            ExchangeStatus status
    ) {

        return exchanges.stream()
                .filter(exchange ->
                        exchange.getStatus() == status)
                .count();
    }


    // =========================================================
    // COUNT SESSIONS BY STATUS
    // =========================================================

    private long countSessionsByStatus(
            List<ExchangeSession> sessions,
            SessionStatus status
    ) {

        return sessions.stream()
                .filter(session ->
                        session.getStatus() == status)
                .count();
    }
}