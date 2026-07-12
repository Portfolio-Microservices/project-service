package com.portfolio.project.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Analytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EventType eventType;

    @Column(length = 255)
    private String page;

    private Long userId;

    @Column(length = 100)
    private String username;

    private Long projectId;

    @Column(length = 255)
    private String projectTitle;

    @Column(columnDefinition = "TEXT")
    private String eventData;

    @Column(length = 1000)
    private String userAgent;

    @Column(length = 255)
    private String ipAddress;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    public enum EventType {
        PAGE_VIEW, PROJECT_VIEW, PROJECT_LIKE, PROJECT_UNLIKE, USER_LOGIN, USER_SIGNUP, SEARCH, ERROR
    }
}
