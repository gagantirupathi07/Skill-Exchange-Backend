package com.SkillExchange.exchange.model;

import com.SkillExchange.skill.model.Skill;
import com.SkillExchange.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "exchanges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Exchange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    /*
     * One accepted ExchangeRequest creates
     * exactly one Exchange.
     */
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "exchange_request_id",
            nullable = false,
            unique = true
    )
    private ExchangeRequest exchangeRequest;


    /*
     * User who teaches the requested skill.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;


    /*
     * User who wants to learn the skill.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "learner_id", nullable = false)
    private User learner;


    /*
     * Skill being taught during this exchange.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;


    /*
     * Final credit amount will be calculated
     * when the session duration is known.
     *
     * Example:
     * 1 hour   = 5 credits
     * 2 hours  = 10 credits
     * 30 mins  = 2.50 credits
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal creditAmount;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ExchangeStatus status = ExchangeStatus.ACTIVE;


    /*
     * Time when the actual exchange became active.
     */
    @Column(nullable = false, updatable = false)
    private LocalDateTime startedAt;


    /*
     * Set only when the teacher completes
     * the exchange.
     */
    private LocalDateTime completedAt;


    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;


    @Column(nullable = false)
    private LocalDateTime updatedAt;


    @PrePersist
    protected void onCreate() {

        if (status == null) {
            status = ExchangeStatus.ACTIVE;
        }

        LocalDateTime now = LocalDateTime.now();

        startedAt = now;
        createdAt = now;
        updatedAt = now;
    }


    @PreUpdate
    protected void onUpdate() {

        updatedAt = LocalDateTime.now();
    }
}