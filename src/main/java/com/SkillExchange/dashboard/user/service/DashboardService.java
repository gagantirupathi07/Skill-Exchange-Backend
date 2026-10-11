package com.SkillExchange.dashboard.user.service;

import com.SkillExchange.credits.model.CreditWallet;
import com.SkillExchange.credits.repository.CreditWalletRepository;
import com.SkillExchange.dashboard.user.dto.DashboardResponse;
import com.SkillExchange.exchange.dto.ExchangeResponse;
import com.SkillExchange.exchange.dto.ExchangeSessionResponse;
import com.SkillExchange.exchange.model.Exchange;
import com.SkillExchange.exchange.model.ExchangeStatus;
import com.SkillExchange.exchange.model.ExchangeSession;
import com.SkillExchange.exchange.model.SessionStatus;
import com.SkillExchange.exchange.repository.ExchangeRepository;
import com.SkillExchange.exchange.repository.ExchangeRequestRepository;
import com.SkillExchange.exchange.repository.ExchangeSessionRepository;
import com.SkillExchange.exchange.model.ExchangeRequest;
import com.SkillExchange.exchange.model.ExchangeRequestStatus;
import com.SkillExchange.notification.repository.NotificationRepository;
import com.SkillExchange.review.dto.ReviewResponse;
import com.SkillExchange.review.repository.ReviewRepository;
import com.SkillExchange.review.model.Review;
import com.SkillExchange.user.dto.MatchResponse;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.repository.UserRepository;
import com.SkillExchange.user.service.MatchService;
import com.SkillExchange.user.skill.repository.UserLearningSkillRepository;
import com.SkillExchange.user.skill.repository.UserTeachingSkillRepository;
import com.SkillExchange.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;

    private final CreditWalletRepository creditWalletRepository;

    private final UserTeachingSkillRepository teachingSkillRepository;
    private final UserLearningSkillRepository learningSkillRepository;

    private final ExchangeRepository exchangeRepository;
    private final ExchangeRequestRepository exchangeRequestRepository;
    private final ExchangeSessionRepository exchangeSessionRepository;

    private final ReviewRepository reviewRepository;

    private final NotificationRepository notificationRepository;

    private final MatchService matchService;


    // =====================================================
    // GET USER DASHBOARD
    // =====================================================

    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(String username) {

        // -------------------------------------------------
        // FIND USER
        // -------------------------------------------------

        User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                ));


        // -------------------------------------------------
        // CREDIT BALANCE
        // -------------------------------------------------

        CreditWallet wallet =
                creditWalletRepository.findByUser(user)
                        .orElse(null);


        // -------------------------------------------------
        // TEACHING SKILLS
        // -------------------------------------------------

        List<String> teachingSkills =
                teachingSkillRepository.findByUser(user)
                        .stream()
                        .map(skill ->
                                skill.getSkill().getName()
                        )
                        .toList();


        // -------------------------------------------------
        // LEARNING SKILLS
        // -------------------------------------------------

        List<String> learningSkills =
                learningSkillRepository.findByUser(user)
                        .stream()
                        .map(skill ->
                                skill.getSkill().getName()
                        )
                        .toList();


        // -------------------------------------------------
        // MATCHES
        // -------------------------------------------------

        List<MatchResponse> matches =
                matchService.findMatches(username);


        // -------------------------------------------------
        // EXCHANGE REQUESTS
        // -------------------------------------------------

        List<ExchangeRequest> sentRequests =
                exchangeRequestRepository.findBySender(user);

        List<ExchangeRequest> receivedRequests =
                exchangeRequestRepository.findByReceiver(user);


        long pendingReceivedRequestCount =
                receivedRequests.stream()
                        .filter(request ->
                                request.getStatus()
                                        == ExchangeRequestStatus.PENDING
                        )
                        .count();


        // -------------------------------------------------
        // EXCHANGES
        // -------------------------------------------------

        List<Exchange> exchanges =
                exchangeRepository.findByTeacherOrLearner(
                        user,
                        user
                );


        long activeExchangeCount =
                exchanges.stream()
                        .filter(exchange ->
                                exchange.getStatus()
                                        == ExchangeStatus.ACTIVE
                        )
                        .count();


        long completedExchangeCount =
                exchanges.stream()
                        .filter(exchange ->
                                exchange.getStatus()
                                        == ExchangeStatus.COMPLETED
                        )
                        .count();


        long cancelledExchangeCount =
                exchanges.stream()
                        .filter(exchange ->
                                exchange.getStatus()
                                        == ExchangeStatus.CANCELLED
                        )
                        .count();


        List<ExchangeResponse> activeExchanges =
                exchanges.stream()
                        .filter(exchange ->
                                exchange.getStatus()
                                        == ExchangeStatus.ACTIVE
                        )
                        .map(this::mapExchangeToResponse)
                        .toList();


        // -------------------------------------------------
        // UPCOMING SESSIONS
        // -------------------------------------------------

        List<ExchangeSessionResponse> upcomingSessions =
                exchanges.stream()
                        .flatMap(exchange ->
                                exchangeSessionRepository
                                        .findByExchange(exchange)
                                        .stream()
                        )
                        .filter(session ->
                                session.getStatus()
                                        == SessionStatus.SCHEDULED
                        )
                        .filter(this::isUpcoming)
                        .map(this::mapSessionToResponse)
                        .toList();


        // -------------------------------------------------
        // REVIEWS
        // -------------------------------------------------

        List<ReviewResponse> receivedReviews =
                reviewRepository
                        .findByRevieweeOrderByCreatedAtDesc(user)
                        .stream()
                        .map(this::mapReviewToResponse)
                        .toList();


        List<ReviewResponse> myReviews =
                reviewRepository
                        .findByReviewerOrderByCreatedAtDesc(user)
                        .stream()
                        .map(this::mapReviewToResponse)
                        .toList();


        // -------------------------------------------------
        // UNREAD NOTIFICATIONS
        // -------------------------------------------------

        long unreadNotificationCount =
                notificationRepository
                        .countByUserAndReadFalse(user);


        // -------------------------------------------------
        // BUILD DASHBOARD
        // -------------------------------------------------

        return DashboardResponse.builder()

                // USER
                .userId(user.getId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())

                // CREDITS
                .creditBalance(
                        wallet != null
                                ? wallet.getBalance()
                                : null
                )

                // SKILLS
                .teachingSkills(teachingSkills)
                .learningSkills(learningSkills)

                // MATCHES
                .matches(matches)

                // REQUESTS
                .sentRequestCount(sentRequests.size())
                .receivedRequestCount(receivedRequests.size())
                .pendingReceivedRequestCount(
                        pendingReceivedRequestCount
                )

                // EXCHANGES
                .activeExchangeCount(activeExchangeCount)
                .completedExchangeCount(completedExchangeCount)
                .cancelledExchangeCount(cancelledExchangeCount)
                .activeExchanges(activeExchanges)

                // SESSIONS
                .upcomingSessions(upcomingSessions)

                // REVIEWS
                .receivedReviews(receivedReviews)
                .myReviews(myReviews)

                // NOTIFICATIONS
                .unreadNotificationCount(
                        unreadNotificationCount
                )

                .build();
    }


    // =====================================================
    // CHECK UPCOMING SESSION
    // =====================================================

    private boolean isUpcoming(
            ExchangeSession session
    ) {

        LocalDate today = LocalDate.now();
        LocalTime now = LocalTime.now();

        if (session.getScheduledDate().isAfter(today)) {
            return true;
        }

        if (session.getScheduledDate().isEqual(today)
                && session.getStartTime().isAfter(now)) {
            return true;
        }

        return false;
    }


    // =====================================================
    // MAP EXCHANGE
    // =====================================================

    private ExchangeResponse mapExchangeToResponse(
            Exchange exchange
    ) {

        return ExchangeResponse.builder()

                .id(exchange.getId())

                .exchangeRequestId(
                        exchange.getExchangeRequest().getId()
                )

                .teacherId(
                        exchange.getTeacher().getId()
                )

                .teacherUsername(
                        exchange.getTeacher().getUsername()
                )

                .teacherFirstName(
                        exchange.getTeacher().getFirstName()
                )

                .teacherLastName(
                        exchange.getTeacher().getLastName()
                )

                .learnerId(
                        exchange.getLearner().getId()
                )

                .learnerUsername(
                        exchange.getLearner().getUsername()
                )

                .learnerFirstName(
                        exchange.getLearner().getFirstName()
                )

                .learnerLastName(
                        exchange.getLearner().getLastName()
                )

                .skillId(
                        exchange.getSkill().getId()
                )

                .skillName(
                        exchange.getSkill().getName()
                )

                .creditAmount(
                        exchange.getCreditAmount()
                )

                .status(
                        exchange.getStatus()
                )

                .startedAt(
                        exchange.getStartedAt()
                )

                .completedAt(
                        exchange.getCompletedAt()
                )

                .createdAt(
                        exchange.getCreatedAt()
                )

                .updatedAt(
                        exchange.getUpdatedAt()
                )

                .build();
    }


    // =====================================================
    // MAP SESSION
    // =====================================================

    private ExchangeSessionResponse mapSessionToResponse(
            ExchangeSession session
    ) {

        Exchange exchange =
                session.getExchange();

        return ExchangeSessionResponse.builder()

                .id(session.getId())

                .exchangeId(
                        exchange.getId()
                )

                .teacherId(
                        exchange.getTeacher().getId()
                )

                .teacherUsername(
                        exchange.getTeacher().getUsername()
                )

                .learnerId(
                        exchange.getLearner().getId()
                )

                .learnerUsername(
                        exchange.getLearner().getUsername()
                )

                .skillId(
                        exchange.getSkill().getId()
                )

                .skillName(
                        exchange.getSkill().getName()
                )

                .scheduledDate(
                        session.getScheduledDate()
                )

                .startTime(
                        session.getStartTime()
                )

                .endTime(
                        session.getEndTime()
                )

                .durationMinutes(
                        session.getDurationMinutes()
                )

                .meetingPlatform(
                        session.getMeetingPlatform()
                )

                .meetingLink(
                        session.getMeetingLink()
                )

                .notes(
                        session.getNotes()
                )

                .status(
                        session.getStatus()
                )

                .createdAt(
                        session.getCreatedAt()
                )

                .completedAt(
                        session.getCompletedAt()
                )

                .build();
    }


    // =====================================================
    // MAP REVIEW
    // =====================================================

    private ReviewResponse mapReviewToResponse(
            Review review
    ) {

        User reviewer =
                review.getReviewer();

        User reviewee =
                review.getReviewee();

        Exchange exchange =
                review.getExchange();

        return ReviewResponse.builder()

                .id(review.getId())

                .exchangeId(
                        exchange.getId()
                )

                .reviewerId(
                        reviewer.getId()
                )

                .reviewerUsername(
                        reviewer.getUsername()
                )

                .reviewerFirstName(
                        reviewer.getFirstName()
                )

                .reviewerLastName(
                        reviewer.getLastName()
                )

                .revieweeId(
                        reviewee.getId()
                )

                .revieweeUsername(
                        reviewee.getUsername()
                )

                .revieweeFirstName(
                        reviewee.getFirstName()
                )

                .revieweeLastName(
                        reviewee.getLastName()
                )

                .rating(
                        review.getRating()
                )

                .comment(
                        review.getComment()
                )

                .createdAt(
                        review.getCreatedAt()
                )

                .updatedAt(
                        review.getUpdatedAt()
                )

                .build();
    }
}
