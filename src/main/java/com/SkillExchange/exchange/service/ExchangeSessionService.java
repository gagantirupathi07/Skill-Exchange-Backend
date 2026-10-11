package com.SkillExchange.exchange.service;

import com.SkillExchange.exception.BadRequestException;
import com.SkillExchange.exception.ForbiddenException;
import com.SkillExchange.exchange.dto.ExchangeSessionRequest;
import com.SkillExchange.exchange.dto.ExchangeSessionResponse;
import com.SkillExchange.exchange.model.Exchange;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeSessionService {

    private final UserRepository userRepository;

    private final ExchangeRepository exchangeRepository;

    private final ExchangeSessionRepository exchangeSessionRepository;

    private final NotificationService notificationService;


    /*
     * Teacher schedules a session.
     */
    @Transactional
    public ExchangeSessionResponse createSession(
            String username,
            Long exchangeId,
            ExchangeSessionRequest request
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

        if (exchange.getStatus()
                != ExchangeStatus.ACTIVE) {

            throw new BadRequestException(
                    "Session can only be scheduled for an active exchange"
            );
        }

        if (!exchange.getTeacher()
                .getId()
                .equals(teacher.getId())) {

            throw new ForbiddenException(
                    "Only the teacher can schedule a session"
            );
        }

        if (!request.getStartTime()
                .isBefore(request.getEndTime())) {

            throw new BadRequestException(
                    "Start time must be before end time"
            );
        }

        LocalDateTime scheduledStart =
                LocalDateTime.of(
                        request.getScheduledDate(),
                        request.getStartTime()
                );

        if (scheduledStart.isBefore(
                LocalDateTime.now()
        )) {

            throw new BadRequestException(
                    "Session cannot be scheduled in the past"
            );
        }

        boolean alreadyScheduled =
                exchangeSessionRepository
                        .existsByExchangeAndStatus(
                                exchange,
                                SessionStatus.SCHEDULED
                        );

        if (alreadyScheduled) {

            throw new BadRequestException(
                    "A session is already scheduled for this exchange"
            );
        }

        long duration =
                Duration.between(
                        request.getStartTime(),
                        request.getEndTime()
                ).toMinutes();

        if (duration <= 0) {

            throw new BadRequestException(
                    "Session duration must be greater than zero"
            );
        }

        ExchangeSession session =
                ExchangeSession.builder()
                        .exchange(exchange)
                        .scheduledDate(
                                request.getScheduledDate()
                        )
                        .startTime(
                                request.getStartTime()
                        )
                        .endTime(
                                request.getEndTime()
                        )
                        .durationMinutes(
                                (int) duration
                        )
                        .meetingPlatform(
                                request.getMeetingPlatform()
                        )
                        .meetingLink(
                                request.getMeetingLink()
                        )
                        .notes(
                                request.getNotes()
                        )
                        .status(
                                SessionStatus.SCHEDULED
                        )
                        .teacherConfirmed(false)
                        .learnerConfirmed(false)
                        .reminderSent(false)
                        .build();

        ExchangeSession savedSession =
                exchangeSessionRepository.save(session);


        /*
         * Notify learner.
         */
        notificationService.createNotification(
                exchange.getLearner(),
                NotificationType.SESSION_SCHEDULED,
                "New Session Scheduled",
                "Your teacher has scheduled a "
                        + exchange.getSkill().getName()
                        + " session on "
                        + request.getScheduledDate()
                        + " from "
                        + request.getStartTime()
                        + " to "
                        + request.getEndTime()
                        + ".",
                savedSession.getId()
        );

        return mapToResponse(savedSession);
    }


    /*
     * Get all sessions for an exchange.
     */
    @Transactional(readOnly = true)
    public List<ExchangeSessionResponse> getSessions(
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
                    "You are not part of this exchange"
            );
        }

        return exchangeSessionRepository
                .findByExchange(exchange)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    /*
     * Teacher or learner confirms
     * that they completed the session.
     */
    @Transactional
    public ExchangeSessionResponse completeSession(
            String username,
            Long exchangeId,
            Long sessionId
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

        ExchangeSession session =
                exchangeSessionRepository
                        .findById(sessionId)
                        .orElseThrow(() ->
                                new BadRequestException(
                                        "Session not found"
                                )
                        );

        if (!session.getExchange()
                .getId()
                .equals(exchange.getId())) {

            throw new BadRequestException(
                    "Session does not belong to this exchange"
            );
        }

        if (!exchange.getTeacher()
                .getId()
                .equals(teacher.getId())) {

            throw new ForbiddenException(
                    "Only the teacher can complete the session"
            );
        }

        if (session.getStatus()
                == SessionStatus.COMPLETED) {

            throw new BadRequestException(
                    "Session is already completed"
            );
        }

        if (session.getStatus()
                != SessionStatus.SCHEDULED) {

            throw new BadRequestException(
                    "Only a scheduled session can be completed"
            );
        }

        LocalDateTime sessionEnd =
                LocalDateTime.of(
                        session.getScheduledDate(),
                        session.getEndTime()
                );

        if (LocalDateTime.now()
                .isBefore(sessionEnd)) {

            throw new BadRequestException(
                    "Session cannot be completed before it ends"
            );
        }

        LocalDateTime now =
                LocalDateTime.now();

        session.setTeacherConfirmed(true);
        session.setTeacherConfirmedAt(now);

        session.setLearnerConfirmed(true);
        session.setLearnerConfirmedAt(now);

        session.setStatus(
                SessionStatus.COMPLETED
        );

        session.setCompletedAt(now);

        notificationService.createNotification(
                exchange.getLearner(),
                NotificationType.SESSION_COMPLETED,
                "Session Completed",
                "The "
                        + exchange.getSkill().getName()
                        + " learning session has been completed.",
                session.getId()
        );

        ExchangeSession savedSession =
                exchangeSessionRepository.save(session);

        return mapToResponse(savedSession);
    }


    private ExchangeSessionResponse mapToResponse(
            ExchangeSession session
    ) {

        Exchange exchange =
                session.getExchange();

        return ExchangeSessionResponse.builder()

                .id(session.getId())

                .exchangeId(exchange.getId())

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

                .teacherConfirmed(
                        session.isTeacherConfirmed()
                )

                .teacherConfirmedAt(
                        session.getTeacherConfirmedAt()
                )

                .learnerConfirmed(
                        session.isLearnerConfirmed()
                )

                .learnerConfirmedAt(
                        session.getLearnerConfirmedAt()
                )

                .createdAt(
                        session.getCreatedAt()
                )

                .completedAt(
                        session.getCompletedAt()
                )

                .build();
    }
}