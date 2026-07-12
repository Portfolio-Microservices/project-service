package com.portfolio.project.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.portfolio.project.entity.Analytics;

@Repository
public interface AnalyticsRepository extends JpaRepository<Analytics, Long> {

    Page<Analytics> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<Analytics> findByEventTypeOrderByTimestampDesc(Analytics.EventType eventType, Pageable pageable);

    Page<Analytics> findByPageOrderByTimestampDesc(String page, Pageable pageable);

    Page<Analytics> findByEventTypeAndPageOrderByTimestampDesc(Analytics.EventType eventType, String page,
            Pageable pageable);

    long countByEventType(Analytics.EventType eventType);

    long countByEventTypeAndProjectId(Analytics.EventType eventType, Long projectId);

    long countByEventTypeAndPage(Analytics.EventType eventType, String page);

    long countByUserId(Long userId);

    long countByUserIdAndEventType(Long userId, Analytics.EventType eventType);

    long countByUserIdAndEventTypeAndPage(Long userId, Analytics.EventType eventType, String page);

    @Query("SELECT COUNT(DISTINCT a.userId) FROM Analytics a WHERE a.userId IS NOT NULL")
    long countDistinctUsers();

    @Query("SELECT COUNT(DISTINCT a.projectId) FROM Analytics a WHERE a.projectId IS NOT NULL")
    long countDistinctProjects();

    @Query("SELECT COUNT(DISTINCT a.userId) FROM Analytics a WHERE a.eventType = :eventType AND a.page = :page AND a.userId IS NOT NULL")
    long countDistinctByEventTypeAndPage(Analytics.EventType eventType, String page);

    @Query("SELECT COUNT(DISTINCT a.userId) FROM Analytics a WHERE a.eventType = :eventType AND a.projectId = :projectId AND a.userId IS NOT NULL")
    long countDistinctByEventTypeAndProjectId(Analytics.EventType eventType, Long projectId);

    Analytics findFirstByOrderByTimestampAsc();

    Analytics findFirstByOrderByTimestampDesc();

    Analytics findFirstByEventTypeAndPageOrderByTimestampAsc(Analytics.EventType eventType, String page);

    Analytics findFirstByEventTypeAndPageOrderByTimestampDesc(Analytics.EventType eventType, String page);

    Analytics findFirstByUserIdOrderByTimestampAsc(Long userId);

    Analytics findFirstByUserIdOrderByTimestampDesc(Long userId);

    List<Analytics> findByProjectId(Long projectId);

    List<Analytics> findByPage(String page);

    List<Analytics> findByUserId(Long userId);
}
