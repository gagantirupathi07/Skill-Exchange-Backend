package com.SkillExchange.exchange.service;

import com.SkillExchange.exception.ConflictException;
import com.SkillExchange.exchange.dto.ExchangeRequestRequest;
import com.SkillExchange.exchange.dto.ExchangeRequestResponse;
import com.SkillExchange.exchange.model.ExchangeRequest;
import com.SkillExchange.exchange.model.ExchangeRequestStatus;
import com.SkillExchange.exchange.repository.ExchangeRequestRepository;
import com.SkillExchange.skill.model.Skill;
import com.SkillExchange.skill.repository.SkillRepository;
import com.SkillExchange.user.model.User;
import com.SkillExchange.user.model.UserStatus;
import com.SkillExchange.user.repository.UserRepository;
import com.SkillExchange.user.skill.repository.UserTeachingSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExchangeRequestService {

    private final UserRepository userRepository;

    private final SkillRepository skillRepository;

    private final UserTeachingSkillRepository teachingSkillRepository;

    private final ExchangeRequestRepository exchangeRequestRepository;

    private final ExchangeService exchangeService;

    @Transactional
    public ExchangeRequestResponse sendRequest(
            String username,
            ExchangeRequestRequest request
    ) {

        /*
         * Find sender.
         */
        User sender = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Sender not found"
                        )
                );


        /*
         * Find receiver.
         */
        User receiver = userRepository
                .findById(request.getReceiverId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Receiver not found"
                        )
                );


        /*
         * Prevent sending request to yourself.
         */
        if (sender.getId().equals(receiver.getId())) {

            throw new RuntimeException(
                    "You cannot send an exchange request to yourself"
            );
        }


        /*
         * Sender must be active.
         */
        if (sender.getStatus() != UserStatus.ACTIVE) {

            throw new RuntimeException(
                    "Your account is not active"
            );
        }


        /*
         * Receiver must be active.
         */
        if (receiver.getStatus() != UserStatus.ACTIVE) {

            throw new RuntimeException(
                    "Receiver account is not active"
            );
        }


        /*
         * Find requested skill.
         *
         * This is the skill that the sender
         * wants to learn from the receiver.
         */
        Skill requestedSkill = skillRepository
                .findById(request.getRequestedSkillId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Requested skill not found"
                        )
                );


        /*
         * Receiver MUST teach the requested skill.
         *
         * Example:
         *
         * Sender wants Python.
         * Receiver must teach Python.
         */
        boolean receiverTeachesSkill =
                teachingSkillRepository
                        .existsByUserAndSkill(
                                receiver,
                                requestedSkill
                        );

        if (!receiverTeachesSkill) {

            throw new RuntimeException(
                    "Receiver does not teach the requested skill"
            );
        }


        /*
         * Offered skill is OPTIONAL.
         *
         * If no skill is selected:
         *
         * offeredSkillId = null
         *
         * and no validation is required.
         */
        Skill offeredSkill = null;

        if (request.getOfferedSkillId() != null) {

            /*
             * Find offered skill.
             */
            offeredSkill = skillRepository
                    .findById(request.getOfferedSkillId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Offered skill not found"
                            )
                    );


            /*
             * If sender selected an offered skill,
             * sender must actually teach that skill.
             */
            boolean senderTeachesSkill =
                    teachingSkillRepository
                            .existsByUserAndSkill(
                                    sender,
                                    offeredSkill
                            );

            if (!senderTeachesSkill) {

                throw new RuntimeException(
                        "You do not teach the offered skill"
                );
            }
        }


        /*
         * Check whether a PENDING request already exists.
         *
         * IMPORTANT:
         *
         * offeredSkill is intentionally NOT included
         * in this duplicate check.
         *
         * Example:
         *
         * Request 1:
         * A -> B
         * wants Python
         * offers Java
         *
         * Request 2:
         * A -> B
         * wants Python
         * offers Spring Boot
         *
         * These are still duplicate pending requests.
         */
        boolean duplicate =
                exchangeRequestRepository
                        .existsBySenderAndReceiverAndRequestedSkill_IdAndStatus(
                                sender,
                                receiver,
                                requestedSkill.getId(),
                                ExchangeRequestStatus.PENDING
                        );


        /*
         * If duplicate exists, return HTTP 409 CONFLICT.
         */
        if (duplicate) {

            throw new ConflictException(
                    "A pending exchange request already exists"
            );
        }


        /*
         * Create exchange request.
         */
        ExchangeRequest exchangeRequest =
                ExchangeRequest.builder()
                        .sender(sender)
                        .receiver(receiver)
                        .requestedSkill(requestedSkill)
                        .offeredSkill(offeredSkill)
                        .status(
                                ExchangeRequestStatus.PENDING
                        )
                        .build();


        /*
         * Save request.
         */
        ExchangeRequest savedRequest =
                exchangeRequestRepository.save(
                        exchangeRequest
                );


        /*
         * Convert entity to response.
         */
        return mapToResponse(savedRequest);
    }


    /*
     * Get requests sent by logged-in user.
     */
    @Transactional(readOnly = true)
    public List<ExchangeRequestResponse> getSentRequests(
            String username
    ) {

        User user = getUser(username);

        return exchangeRequestRepository
                .findBySender(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    /*
     * Get requests received by logged-in user.
     */
    @Transactional(readOnly = true)
    public List<ExchangeRequestResponse> getReceivedRequests(
            String username
    ) {

        User user = getUser(username);

        return exchangeRequestRepository
                .findByReceiver(user)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }


    /*
     * Accept exchange request.
     *
     * Only receiver can accept.
     */
    @Transactional
    public ExchangeRequestResponse acceptRequest(
            String username,
            Long requestId
    ) {

        User receiver = getUser(username);

        ExchangeRequest request =
                getRequest(requestId);


        /*
         * Make sure logged-in user is receiver.
         */
        if (!request.getReceiver()
                .getId()
                .equals(receiver.getId())) {

            throw new RuntimeException(
                    "Only the receiver can accept this request"
            );
        }


        /*
         * Only PENDING requests can be accepted.
         */
        if (request.getStatus()
                != ExchangeRequestStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending requests can be accepted"
            );
        }


        /*
         * Change request status.
         */
        request.setStatus(
                ExchangeRequestStatus.ACCEPTED
        );


        /*
         * Save updated request.
         */
        ExchangeRequest savedRequest =
                exchangeRequestRepository.save(
                        request
                );


        /*
         * Automatically create Exchange.
         *
         * sender   = learner
         * receiver = teacher
         *
         * requestedSkill = skill being taught
         */
        exchangeService.createFromAcceptedRequest(
                savedRequest
        );


        return mapToResponse(savedRequest);
    }


    /*
     * Reject exchange request.
     *
     * Only receiver can reject.
     */
    @Transactional
    public ExchangeRequestResponse rejectRequest(
            String username,
            Long requestId
    ) {

        User receiver = getUser(username);

        ExchangeRequest request =
                getRequest(requestId);


        /*
         * Make sure logged-in user is receiver.
         */
        if (!request.getReceiver()
                .getId()
                .equals(receiver.getId())) {

            throw new RuntimeException(
                    "Only the receiver can reject this request"
            );
        }


        /*
         * Only PENDING requests can be rejected.
         */
        if (request.getStatus()
                != ExchangeRequestStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending requests can be rejected"
            );
        }


        /*
         * Change status.
         */
        request.setStatus(
                ExchangeRequestStatus.REJECTED
        );


        /*
         * Save request.
         */
        ExchangeRequest savedRequest =
                exchangeRequestRepository.save(
                        request
                );


        return mapToResponse(savedRequest);
    }


    /*
     * Cancel exchange request.
     *
     * Only sender can cancel.
     */
    @Transactional
    public ExchangeRequestResponse cancelRequest(
            String username,
            Long requestId
    ) {

        User sender = getUser(username);

        ExchangeRequest request =
                getRequest(requestId);


        /*
         * Make sure logged-in user is sender.
         */
        if (!request.getSender()
                .getId()
                .equals(sender.getId())) {

            throw new RuntimeException(
                    "Only the sender can cancel this request"
            );
        }


        /*
         * Only PENDING requests can be cancelled.
         */
        if (request.getStatus()
                != ExchangeRequestStatus.PENDING) {

            throw new RuntimeException(
                    "Only pending requests can be cancelled"
            );
        }


        /*
         * Change status.
         */
        request.setStatus(
                ExchangeRequestStatus.CANCELLED
        );


        /*
         * Save request.
         */
        ExchangeRequest savedRequest =
                exchangeRequestRepository.save(
                        request
                );


        return mapToResponse(savedRequest);
    }


    /*
     * Find user by username.
     */
    private User getUser(
            String username
    ) {

        return userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found"
                        )
                );
    }


    /*
     * Find exchange request.
     */
    private ExchangeRequest getRequest(
            Long requestId
    ) {

        return exchangeRequestRepository
                .findById(requestId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Exchange request not found"
                        )
                );
    }


    /*
     * Convert ExchangeRequest entity
     * to ExchangeRequestResponse DTO.
     *
     * offeredSkill can be NULL.
     */
    private ExchangeRequestResponse mapToResponse(
            ExchangeRequest request
    ) {

        return ExchangeRequestResponse.builder()

                .id(
                        request.getId()
                )

                .senderId(
                        request.getSender().getId()
                )

                .senderUsername(
                        request.getSender().getUsername()
                )

                .senderFirstName(
                        request.getSender().getFirstName()
                )

                .senderLastName(
                        request.getSender().getLastName()
                )

                .receiverId(
                        request.getReceiver().getId()
                )

                .receiverUsername(
                        request.getReceiver().getUsername()
                )

                .receiverFirstName(
                        request.getReceiver().getFirstName()
                )

                .receiverLastName(
                        request.getReceiver().getLastName()
                )

                .requestedSkillId(
                        request.getRequestedSkill().getId()
                )

                .requestedSkillName(
                        request.getRequestedSkill().getName()
                )

                /*
                 * Offered skill is optional.
                 */
                .offeredSkillId(
                        request.getOfferedSkill() != null
                                ? request.getOfferedSkill().getId()
                                : null
                )

                .offeredSkillName(
                        request.getOfferedSkill() != null
                                ? request.getOfferedSkill().getName()
                                : null
                )

                .status(
                        request.getStatus()
                )

                .createdAt(
                        request.getCreatedAt()
                )

                .updatedAt(
                        request.getUpdatedAt()
                )

                .build();
    }
}