package com.SkillExchange.exchange.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "exchange_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExchangeSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exchange_id", nullable = false)
    private Exchange exchange;

    @Column(nullable = false)
    private LocalDate scheduledDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(nullable = false, length = 50)
    private String meetingPlatform;

    @Column(nullable = false, length = 1000)
    private String meetingLink;

    @Column(length = 1000)
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private SessionStatus status = SessionStatus.SCHEDULED;

    /*
     * Teacher confirmation.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean teacherConfirmed = false;

    private LocalDateTime teacherConfirmedAt;

    /*
     * Learner confirmation.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean learnerConfirmed = false;

    private LocalDateTime learnerConfirmedAt;

    /*
     * Prevent duplicate reminder notifications.
     */
    @Column(nullable = false)
    @Builder.Default
    private boolean reminderSent = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    @PrePersist
    protected void onCreate() {

        if (status == null) {
            status = SessionStatus.SCHEDULED;
        }

        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
    }
}