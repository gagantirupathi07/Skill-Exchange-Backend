package com.SkillExchange.exchange.service;

import com.SkillExchange.credits.service.CreditService;
import com.SkillExchange.exception.BadRequestException;
import com.SkillExchange.exception.ForbiddenException;
import com.SkillExchange.exchange.dto.ExchangeCompletionResponse;
import com.SkillExchange.exchange.dto.ExchangeResponse;
import com.SkillExchange.exchange.model.Exchange;
import com.SkillExchange.exchange.model.ExchangeRequest;
import com.SkillExchange.exchange.model.ExchangeRequestStatus;
import com.SkillExchange.exchange.model.ExchangeSession;
import com.SkillExchange.exchange.model.ExchangeStatus;
import com.SkillExchange.exchange.model.SessionStatus;
import com.SkillExchange.exchange.repository.ExchangeRepository;
import com.SkillExchange.exchange.repository.ExchangeSessionRepository;
import com.SkillExchange.notification.model.NotificationType;
import com.SkillExchange.notification.service.NotificationService;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExchangeService {

    private static final ZoneId ZONE_ID =
            ZoneId.of("Asia/Kolkata");

    private final ExchangeRepository exchangeRepository;
    private final UserRepository userRepository;
    private final ExchangeSessionRepository exchangeSessionRepository;
    private final CreditService creditService;
    private final NotificationService notificationService;

    /*
     * Creates an Exchange from an accepted ExchangeRequest.
     */
    @Transactional
    public ExchangeResponse createFromAcceptedRequest(
            ExchangeRequest request
    ) {

        if (request.getStatus() != ExchangeRequestStatus.ACCEPTED) {
            throw new BadRequestException(
                    "Exchange can only be created from an accepted request"
            );
        }

        if (exchangeRepository.existsByExchangeRequest(request)) {
            throw new BadRequestException(
                    "Exchange already exists for this request"
            );
        }

        User learner = request.getSender();
        User teacher = request.getReceiver();

        Exchange exchange = Exchange.builder()
                .exchangeRequest(request)
                .teacher(teacher)
                .learner(learner)
                .skill(request.getRequestedSkill())
                .status(ExchangeStatus.ACTIVE)
                .build();

        Exchange savedExchange = exchangeRepository.save(exchange);

        return mapToResponse(savedExchange);
    }

    /*
     * Completes an exchange manually.
     * Only the teacher can complete the exchange.
     */
    @Transactional
    public ExchangeCompletionResponse completeExchange(
            String username,
            Long exchangeId
    ) {

        User teacher = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BadRequestException("User not found")
                );

        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() ->
                        new BadRequestException("Exchange not found")
                );

        if (!exchange.getTeacher().getId().equals(teacher.getId())) {
            throw new ForbiddenException(
                    "Only the teacher can complete this exchange"
            );
        }

        if (exchange.getStatus() == ExchangeStatus.COMPLETED) {
            throw new BadRequestException(
                    "Exchange is already completed"
            );
        }

        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            throw new BadRequestException(
                    "Only an active exchange can be completed"
            );
        }

        List<ExchangeSession> sessions =
                exchangeSessionRepository.findByExchange(exchange);

        ExchangeSession session = sessions.stream()
                .filter(s -> s.getStatus() == SessionStatus.SCHEDULED)
                .findFirst()
                .orElseThrow(() ->
                        new BadRequestException(
                                "No scheduled session found for this exchange"
                        )
                );

        if (session.getScheduledDate() == null
                || session.getEndTime() == null) {
            throw new BadRequestException(
                    "The session's scheduled date or end time is missing"
            );
        }

        LocalDateTime now = LocalDateTime.now(ZONE_ID);

        LocalDateTime sessionEnd = LocalDateTime.of(
                session.getScheduledDate(),
                session.getEndTime()
        );

        if (now.isBefore(sessionEnd)) {
            throw new BadRequestException(
                    "The session cannot be completed before it ends"
            );
        }

        completeSessionAndTransferCredits(
                exchange,
                session,
                now
        );

        Exchange savedExchange = exchangeRepository.save(exchange);

        return buildCompletionResponse(
                savedExchange,
                session,
                exchange.getCreditAmount(),
                now
        );
    }

    /*
     * Automatically completes an ended session.
     *
     * This method must remain transactional so that a failure
     * during credit transfer rolls back the session and exchange
     * completion changes.
     */
    @Transactional
    public void completeExchangeAutomatically(Long sessionId) {

        log.info(
                "AUTO-COMPLETION SERVICE: Started for session {}",
                sessionId
        );

        ExchangeSession session = exchangeSessionRepository
                .findById(sessionId)
                .orElse(null);

        if (session == null) {
            log.warn(
                    "AUTO-COMPLETION SERVICE: Session {} does not exist",
                    sessionId
            );
            return;
        }

        log.info(
                "AUTO-COMPLETION SERVICE: Session {}, status={}, date={}, endTime={}",
                session.getId(),
                session.getStatus(),
                session.getScheduledDate(),
                session.getEndTime()
        );

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            log.info(
                    "AUTO-COMPLETION SERVICE: Skipping session {} because its status is {}",
                    sessionId,
                    session.getStatus()
            );
            return;
        }

        Exchange exchange = session.getExchange();

        if (exchange == null) {
            log.error(
                    "AUTO-COMPLETION SERVICE: Session {} has no associated exchange",
                    sessionId
            );
            throw new IllegalStateException(
                    "Session has no associated exchange"
            );
        }

        log.info(
                "AUTO-COMPLETION SERVICE: Exchange {}, status={}",
                exchange.getId(),
                exchange.getStatus()
        );

        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            log.info(
                    "AUTO-COMPLETION SERVICE: Skipping session {} because exchange {} is {}",
                    sessionId,
                    exchange.getId(),
                    exchange.getStatus()
            );
            return;
        }

        if (session.getScheduledDate() == null
                || session.getEndTime() == null) {
            throw new IllegalStateException(
                    "Session " + sessionId
                            + " is missing its scheduled date or end time"
            );
        }

        LocalDateTime now = LocalDateTime.now(ZONE_ID);

        LocalDateTime sessionEnd = LocalDateTime.of(
                session.getScheduledDate(),
                session.getEndTime()
        );

        log.info(
                "AUTO-COMPLETION SERVICE: Session {}, current IST={}, session end IST={}",
                sessionId,
                now,
                sessionEnd
        );

        if (now.isBefore(sessionEnd)) {
            log.info(
                    "AUTO-COMPLETION SERVICE: Skipping session {} because it has not ended",
                    sessionId
            );
            return;
        }

        log.info(
                "AUTO-COMPLETION SERVICE: Completing session {} and transferring credits",
                sessionId
        );

        completeSessionAndTransferCredits(
                exchange,
                session,
                now
        );

        exchangeRepository.save(exchange);

        log.info(
                "AUTO-COMPLETION SERVICE: Session {} and exchange {} completed successfully; credits transferred={}",
                sessionId,
                exchange.getId(),
                exchange.getCreditAmount()
        );
    }

    /*
     * Shared transactional completion logic.
     *
     * The session and exchange are updated in the same transaction
     * as the credit transfer. A transfer failure must roll back
     * these database changes.
     */
    private void completeSessionAndTransferCredits(
            Exchange exchange,
            ExchangeSession session,
            LocalDateTime completedAt
    ) {

        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            throw new BadRequestException(
                    "Only an active exchange can be completed"
            );
        }

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new BadRequestException(
                    "Only a scheduled session can be completed"
            );
        }

        if (session.getDurationMinutes() == null
                || session.getDurationMinutes() <= 0) {
            throw new BadRequestException(
                    "Session duration must be greater than zero"
            );
        }

        session.setTeacherConfirmed(true);
        session.setTeacherConfirmedAt(completedAt);

        session.setLearnerConfirmed(true);
        session.setLearnerConfirmedAt(completedAt);

        session.setStatus(SessionStatus.COMPLETED);
        session.setCompletedAt(completedAt);

        exchangeSessionRepository.save(session);

        BigDecimal credits = creditService.calculateCredits(
                session.getDurationMinutes()
        );

        if (credits == null || credits.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException(
                    "Calculated session credits must be greater than zero"
            );
        }

        exchange.setCreditAmount(credits);

        /*
         * Transfer credits before finalizing the exchange.
         * If this fails, the surrounding transaction should roll back.
         */
        creditService.transferCredits(exchange, credits);

        exchange.setStatus(ExchangeStatus.COMPLETED);
        exchange.setCompletedAt(completedAt);

        notifyParticipants(exchange, credits);

        log.info(
                "EXCHANGE COMPLETION: Exchange {}, session {}, credits={}",
                exchange.getId(),
                session.getId(),
                credits
        );
    }

    /*
     * Sends completion and credit notifications to both participants.
     */
    private void notifyParticipants(
            Exchange exchange,
            BigDecimal credits
    ) {

        notificationService.createNotification(
                exchange.getTeacher(),
                NotificationType.EXCHANGE_COMPLETED,
                "Exchange Completed",
                "Your " + exchange.getSkill().getName()
                        + " skill exchange has been completed successfully.",
                exchange.getId()
        );

        notificationService.createNotification(
                exchange.getLearner(),
                NotificationType.EXCHANGE_COMPLETED,
                "Exchange Completed",
                "Your " + exchange.getSkill().getName()
                        + " skill exchange has been completed successfully.",
                exchange.getId()
        );

        notificationService.createNotification(
                exchange.getTeacher(),
                NotificationType.CREDITS_EARNED,
                "Credits Earned",
                "You earned " + credits
                        + " credits for teaching "
                        + exchange.getSkill().getName() + ".",
                exchange.getId()
        );

        notificationService.createNotification(
                exchange.getLearner(),
                NotificationType.CREDITS_SPENT,
                "Credits Spent",
                credits + " credits were spent for learning "
                        + exchange.getSkill().getName() + ".",
                exchange.getId()
        );
    }

    /*
     * Builds the response for manual completion.
     */
    private ExchangeCompletionResponse buildCompletionResponse(
            Exchange exchange,
            ExchangeSession session,
            BigDecimal credits,
            LocalDateTime completedAt
    ) {

        return ExchangeCompletionResponse.builder()
                .exchangeId(exchange.getId())
                .sessionId(session.getId())
                .teacherUsername(exchange.getTeacher().getUsername())
                .learnerUsername(exchange.getLearner().getUsername())
                .skillName(exchange.getSkill().getName())
                .durationMinutes(session.getDurationMinutes())
                .creditsTransferred(credits)
                .exchangeStatus(exchange.getStatus())
                .completedAt(completedAt)
                .message(
                        "Exchange completed and credits transferred successfully"
                )
                .build();
    }

    @Transactional(readOnly = true)
    public ExchangeResponse getExchangeById(
            String username,
            Long exchangeId
    ) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BadRequestException("User not found")
                );

        Exchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() ->
                        new BadRequestException("Exchange not found")
                );

        boolean isTeacher = exchange.getTeacher()
                .getId()
                .equals(user.getId());

        boolean isLearner = exchange.getLearner()
                .getId()
                .equals(user.getId());

        if (!isTeacher && !isLearner) {
            throw new ForbiddenException(
                    "You are not a participant in this exchange"
            );
        }

        return mapToResponse(exchange);
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> getMyExchanges(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BadRequestException("User not found")
                );

        return exchangeRepository.findByTeacherOrLearner(user, user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> getTeachingExchanges(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BadRequestException("User not found")
                );

        return exchangeRepository.findByTeacher(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> getLearningExchanges(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() ->
                        new BadRequestException("User not found")
                );

        return exchangeRepository.findByLearner(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ExchangeResponse mapToResponse(Exchange exchange) {

        return ExchangeResponse.builder()
                .id(exchange.getId())
                .exchangeRequestId(exchange.getExchangeRequest().getId())
                .teacherId(exchange.getTeacher().getId())
                .teacherUsername(exchange.getTeacher().getUsername())
                .teacherFirstName(exchange.getTeacher().getFirstName())
                .teacherLastName(exchange.getTeacher().getLastName())
                .learnerId(exchange.getLearner().getId())
                .learnerUsername(exchange.getLearner().getUsername())
                .learnerFirstName(exchange.getLearner().getFirstName())
                .learnerLastName(exchange.getLearner().getLastName())
                .skillId(exchange.getSkill().getId())
                .skillName(exchange.getSkill().getName())
                .creditAmount(exchange.getCreditAmount())
                .status(exchange.getStatus())
                .startedAt(exchange.getStartedAt())
                .completedAt(exchange.getCompletedAt())
                .createdAt(exchange.getCreatedAt())
                .updatedAt(exchange.getUpdatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ExchangeResponse> getAllExchangesForAdmin() {

        return exchangeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}
