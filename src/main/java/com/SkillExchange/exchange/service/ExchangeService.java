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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final ExchangeRepository exchangeRepository;

    private final UserRepository userRepository;

    private final ExchangeSessionRepository exchangeSessionRepository;

    private final CreditService creditService;

    private final NotificationService notificationService;


    /*
     * Creates an Exchange from an accepted
     * ExchangeRequest.
     */
    @Transactional
    public ExchangeResponse createFromAcceptedRequest(
            ExchangeRequest request
    ) {

        if (request.getStatus()
                != ExchangeRequestStatus.ACCEPTED) {

            throw new BadRequestException(
                    "Exchange can only be created from an accepted request"
            );
        }

        if (exchangeRepository
                .existsByExchangeRequest(request)) {

            throw new BadRequestException(
                    "Exchange already exists for this request"
            );
        }

        User learner =
                request.getSender();

        User teacher =
                request.getReceiver();

        Exchange exchange =
                Exchange.builder()
                        .exchangeRequest(request)
                        .teacher(teacher)
                        .learner(learner)
                        .skill(request.getRequestedSkill())
                        .status(ExchangeStatus.ACTIVE)
                        .build();

        Exchange savedExchange =
                exchangeRepository.save(exchange);

        return mapToResponse(savedExchange);
    }


    /*
     * Complete the overall Exchange.
     *
     * ONLY THE TEACHER CAN COMPLETE IT.
     *
     * The session must already have been
     * confirmed by BOTH participants.
     */
    @Transactional
    public ExchangeCompletionResponse completeExchange(
            String username,
            Long exchangeId
    ) {

        User teacher =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                )
                        );

        Exchange exchange =
                exchangeRepository
                        .findById(exchangeId)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Exchange not found"
                                )
                        );

        /*
         * ONLY teacher can complete
         * the overall exchange.
         */
        if (!exchange.getTeacher()
                .getId()
                .equals(teacher.getId())) {

            throw new ForbiddenException(
                    "Only the teacher can complete this exchange"
            );
        }

        /*
         * Prevent duplicate completion.
         */
        if (exchange.getStatus()
                == ExchangeStatus.COMPLETED) {

            throw new BadRequestException(
                    "Exchange is already completed"
            );
        }

        /*
         * Exchange must be ACTIVE.
         */
        if (exchange.getStatus()
                != ExchangeStatus.ACTIVE) {

            throw new BadRequestException(
                    "Only an active exchange can be completed"
            );
        }

        /*
         * Find the scheduled session.
         */
        List<ExchangeSession> sessions =
                exchangeSessionRepository
                        .findByExchange(exchange);

        ExchangeSession session =
                sessions.stream()
                        .filter(s ->
                                s.getStatus()
                                        == SessionStatus.SCHEDULED
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "No scheduled session found for this exchange"
                                )
                        );

        /*
         * Calculate session end time.
         */
        LocalDateTime sessionEnd =
                LocalDateTime.of(
                        session.getScheduledDate(),
                        session.getEndTime()
                );

        /*
         * Session must already be finished.
         */
        if (LocalDateTime.now()
                .isBefore(sessionEnd)) {

            throw new BadRequestException(
                    "The session cannot be completed before it ends"
            );
        }

        /*
         * Mark session as completed.
         *
         * We keep both confirmation fields TRUE
         * for compatibility with the existing model.
         */
        LocalDateTime completedAt =
                LocalDateTime.now();

        session.setTeacherConfirmed(true);
        session.setTeacherConfirmedAt(completedAt);

        session.setLearnerConfirmed(true);
        session.setLearnerConfirmedAt(completedAt);

        session.setStatus(
                SessionStatus.COMPLETED
        );

        session.setCompletedAt(
                completedAt
        );

        exchangeSessionRepository.save(session);

        /*
         * Calculate credits from session duration.
         */
        BigDecimal credits =
                creditService.calculateCredits(
                        session.getDurationMinutes()
                );

        /*
         * Store credit amount in exchange.
         */
        exchange.setCreditAmount(credits);

        /*
         * Transfer:
         *
         * learner → teacher
         *
         * If this fails, the entire transaction
         * will roll back.
         */
        creditService.transferCredits(
                exchange,
                credits
        );

        /*
         * Mark exchange completed.
         */
        exchange.setStatus(
                ExchangeStatus.COMPLETED
        );

        exchange.setCompletedAt(
                completedAt
        );

        Exchange savedExchange =
                exchangeRepository.save(exchange);

        /*
         * Notify teacher.
         */
        notificationService.createNotification(
                savedExchange.getTeacher(),
                NotificationType.EXCHANGE_COMPLETED,
                "Exchange Completed",
                "Your "
                        + savedExchange.getSkill().getName()
                        + " skill exchange has been completed successfully.",
                savedExchange.getId()
        );

        /*
         * Notify learner.
         */
        notificationService.createNotification(
                savedExchange.getLearner(),
                NotificationType.EXCHANGE_COMPLETED,
                "Exchange Completed",
                "Your "
                        + savedExchange.getSkill().getName()
                        + " skill exchange has been completed successfully.",
                savedExchange.getId()
        );

        /*
         * Notify teacher about earned credits.
         */
        notificationService.createNotification(
                savedExchange.getTeacher(),
                NotificationType.CREDITS_EARNED,
                "Credits Earned",
                "You earned "
                        + credits
                        + " credits for teaching "
                        + savedExchange.getSkill().getName()
                        + ".",
                savedExchange.getId()
        );

        /*
         * Notify learner about spent credits.
         */
        notificationService.createNotification(
                savedExchange.getLearner(),
                NotificationType.CREDITS_SPENT,
                "Credits Spent",
                credits
                        + " credits were spent for learning "
                        + savedExchange.getSkill().getName()
                        + ".",
                savedExchange.getId()
        );

        /*
         * Return completion response.
         */
        return ExchangeCompletionResponse.builder()

                .exchangeId(
                        savedExchange.getId()
                )

                .sessionId(
                        session.getId()
                )

                .teacherUsername(
                        savedExchange
                                .getTeacher()
                                .getUsername()
                )

                .learnerUsername(
                        savedExchange
                                .getLearner()
                                .getUsername()
                )

                .skillName(
                        savedExchange
                                .getSkill()
                                .getName()
                )

                .durationMinutes(
                        session.getDurationMinutes()
                )

                .creditsTransferred(
                        credits
                )

                .exchangeStatus(
                        savedExchange.getStatus()
                )

                .completedAt(
                        completedAt
                )

                .message(
                        "Exchange completed and credits transferred successfully"
                )

                .build();
    }
    @Transactional
    public void completeExchangeAutomatically(Long sessionId) {

        ExchangeSession session = exchangeSessionRepository
                .findById(sessionId)
                .orElse(null);

        if (session == null
                || session.getStatus() != SessionStatus.SCHEDULED) {
            return;
        }

        Exchange exchange = session.getExchange();

        // Never transfer credits twice for an already completed exchange.
        if (exchange.getStatus() != ExchangeStatus.ACTIVE) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime sessionEnd = LocalDateTime.of(
                session.getScheduledDate(),
                session.getEndTime()
        );

        if (now.isBefore(sessionEnd)) {
            return;
        }

        // Complete the session.
        session.setTeacherConfirmed(true);
        session.setTeacherConfirmedAt(now);
        session.setLearnerConfirmed(true);
        session.setLearnerConfirmedAt(now);
        session.setStatus(SessionStatus.COMPLETED);
        session.setCompletedAt(now);

        exchangeSessionRepository.save(session);

        // Calculate and transfer credits using the existing service.
        BigDecimal credits = creditService.calculateCredits(
                session.getDurationMinutes()
        );

        exchange.setCreditAmount(credits);

        creditService.transferCredits(exchange, credits);

        // Complete the exchange.
        exchange.setStatus(ExchangeStatus.COMPLETED);
        exchange.setCompletedAt(now);

        Exchange savedExchange = exchangeRepository.save(exchange);

        // Notify both participants.
        notificationService.createNotification(
                savedExchange.getTeacher(),
                NotificationType.EXCHANGE_COMPLETED,
                "Exchange Completed",
                "Your " + savedExchange.getSkill().getName()
                        + " skill exchange has been completed successfully.",
                savedExchange.getId()
        );

        notificationService.createNotification(
                savedExchange.getLearner(),
                NotificationType.EXCHANGE_COMPLETED,
                "Exchange Completed",
                "Your " + savedExchange.getSkill().getName()
                        + " skill exchange has been completed successfully.",
                savedExchange.getId()
        );

        notificationService.createNotification(
                savedExchange.getTeacher(),
                NotificationType.CREDITS_EARNED,
                "Credits Earned",
                "You earned " + credits
                        + " credits for teaching "
                        + savedExchange.getSkill().getName() + ".",
                savedExchange.getId()
        );

        notificationService.createNotification(
                savedExchange.getLearner(),
                NotificationType.CREDITS_SPENT,
                "Credits Spent",
                credits + " credits were spent for learning "
                        + savedExchange.getSkill().getName() + ".",
                savedExchange.getId()
        );
    }
    @Transactional(readOnly = true)
    public ExchangeResponse getExchangeById(
            String username,
            Long exchangeId
    ) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                )
                        );

        Exchange exchange =
                exchangeRepository
                        .findById(exchangeId)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Exchange not found"
                                )
                        );

        boolean isTeacher =
                exchange.getTeacher()
                        .getId()
                        .equals(user.getId());

        boolean isLearner =
                exchange.getLearner()
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
    public List<ExchangeResponse> getMyExchanges(
            String username
    ) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                )
                        );

        return exchangeRepository
                .findByTeacherOrLearner(user, user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<ExchangeResponse> getTeachingExchanges(
            String username
    ) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                )
                        );

        return exchangeRepository
                .findByTeacher(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    @Transactional(readOnly = true)
    public List<ExchangeResponse> getLearningExchanges(
            String username
    ) {

        User user =
                userRepository
                        .findByUsername(username)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "User not found"
                                )
                        );

        return exchangeRepository
                .findByLearner(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    private ExchangeResponse mapToResponse(
            Exchange exchange
    ) {

        return ExchangeResponse.builder()

                .id(exchange.getId())

                .exchangeRequestId(
                        exchange
                                .getExchangeRequest()
                                .getId()
                )

                .teacherId(
                        exchange
                                .getTeacher()
                                .getId()
                )

                .teacherUsername(
                        exchange
                                .getTeacher()
                                .getUsername()
                )

                .teacherFirstName(
                        exchange
                                .getTeacher()
                                .getFirstName()
                )

                .teacherLastName(
                        exchange
                                .getTeacher()
                                .getLastName()
                )

                .learnerId(
                        exchange
                                .getLearner()
                                .getId()
                )

                .learnerUsername(
                        exchange
                                .getLearner()
                                .getUsername()
                )

                .learnerFirstName(
                        exchange
                                .getLearner()
                                .getFirstName()
                )

                .learnerLastName(
                        exchange
                                .getLearner()
                                .getLastName()
                )

                .skillId(
                        exchange
                                .getSkill()
                                .getId()
                )

                .skillName(
                        exchange
                                .getSkill()
                                .getName()
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
    @Transactional(readOnly = true)
    public List<ExchangeResponse> getAllExchangesForAdmin() {

        return exchangeRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
}