package com.SkillExchange.exchange.model;

import com.SkillExchange.skill.model.Skill;
import com.SkillExchange.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "exchange_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    /*
     * Skill that the sender wants to learn.
     * This is mandatory.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requested_skill_id", nullable = false)
    private Skill requestedSkill;

    /*
     * Skill that the sender optionally offers
     * in return.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offered_skill_id", nullable = true)
    private Skill offeredSkill;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ExchangeRequestStatus status = ExchangeRequestStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {

        if (status == null) {
            status = ExchangeRequestStatus.PENDING;
        }

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}