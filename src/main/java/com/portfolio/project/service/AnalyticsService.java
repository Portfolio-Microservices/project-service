package com.portfolio.project.service;

import org.springframework.stereotype.Service;

import com.portfolio.project.entity.Analytics;
import com.portfolio.project.repository.AnalyticsRepository;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public void recordEvent(Analytics.EventType eventType, String page, Long userId, String username, Long projectId,
            String projectTitle, String eventData, HttpServletRequest servletRequest) {
        if (eventType == null) {
            log.warn("Analytics event type is null");
            return;
        }

        Analytics analytics = Analytics.builder().eventType(eventType).page(page).userId(userId).username(username)
                .projectId(projectId).projectTitle(projectTitle).eventData(eventData)
                .userAgent(getUserAgent(servletRequest)).ipAddress(getIpAddress(servletRequest)).build();

        analyticsRepository.save(analytics);
        log.debug("Saved analytics event: {}", eventType);
    }

    private String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        return request.getHeader("User-Agent");
    }

    private String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
