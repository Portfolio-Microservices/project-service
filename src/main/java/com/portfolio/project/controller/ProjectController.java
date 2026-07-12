package com.portfolio.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.project.dto.CreateProjectRequest;
import com.portfolio.project.dto.PaginationResponse;
import com.portfolio.project.dto.ProjectResponse;
import com.portfolio.project.dto.UpdateProjectRequest;
import com.portfolio.project.entity.Analytics;
import com.portfolio.project.security.UserPrincipal;
import com.portfolio.project.service.AnalyticsService;
import com.portfolio.project.service.ProjectService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/projects")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ProjectController {

    private final ProjectService projectService;
    private final AnalyticsService analyticsService;

    @GetMapping
    public ResponseEntity<PaginationResponse<ProjectResponse>> getAllProjects(
            @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request, Authentication authentication) {

        log.info("Fetching all projects - page: {}, size: {}", page, size);
        Long userId = extractUserId(authentication);
        String username = extractUsername(authentication);
        analyticsService.recordEvent(Analytics.EventType.PAGE_VIEW, "projects.list", userId, username, null, null, null,
                request);

        PaginationResponse<ProjectResponse> response = projectService.getAllProjects(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<PaginationResponse<ProjectResponse>> searchProjects(@RequestParam String q,
            @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request, Authentication authentication) {

        log.info("Searching projects - query: {}, page: {}, size: {}", q, page, size);
        Long userId = extractUserId(authentication);
        String username = extractUsername(authentication);
        analyticsService.recordEvent(Analytics.EventType.SEARCH, "projects.search", userId, username, null, null, q,
                request);

        PaginationResponse<ProjectResponse> response = projectService.searchByTitle(q, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trending/views")
    public ResponseEntity<PaginationResponse<ProjectResponse>> getMostViewedProjects(
            @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request, Authentication authentication) {

        log.info("Fetching most viewed projects - page: {}, size: {}", page, size);
        Long userId = extractUserId(authentication);
        String username = extractUsername(authentication);
        analyticsService.recordEvent(Analytics.EventType.PAGE_VIEW, "projects.trending.views", userId, username, null,
                null, null, request);

        PaginationResponse<ProjectResponse> response = projectService.getMostViewedProjects(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trending/likes")
    public ResponseEntity<PaginationResponse<ProjectResponse>> getMostLikedProjects(
            @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "20") Integer size,
            HttpServletRequest request, Authentication authentication) {

        log.info("Fetching most liked projects - page: {}, size: {}", page, size);
        Long userId = extractUserId(authentication);
        String username = extractUsername(authentication);
        analyticsService.recordEvent(Analytics.EventType.PAGE_VIEW, "projects.trending.likes", userId, username, null,
                null, null, request);

        PaginationResponse<ProjectResponse> response = projectService.getMostLikedProjects(page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProjectById(@PathVariable Long id, Authentication authentication) {

        log.info("Fetching project with ID: {}", id);

        Long userId = null;
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            userId = userPrincipal.getId();
        }

        ProjectResponse response = projectService.getProjectById(id, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {

        log.info("Creating new project - title: {}", request.getTitle());
        ProjectResponse response = projectService.createProject(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request) {

        log.info("Updating project with ID: {}", id);
        ProjectResponse response = projectService.updateProject(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.info("Deleting project with ID: {}", id);
        projectService.deleteProject(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/views")
    public ResponseEntity<ProjectResponse> recordView(@PathVariable Long id, HttpServletRequest request,
            Authentication authentication) {
        log.debug("Recording view for project ID: {}", id);

        Long userId = extractUserId(authentication);
        String username = extractUsername(authentication);
        ProjectResponse response = projectService.incrementViewCounter(id);
        analyticsService.recordEvent(Analytics.EventType.PROJECT_VIEW, "project.view", userId, username,
                response.getId(), response.getTitle(), "view", request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ProjectResponse> likeProject(@PathVariable Long id, Authentication authentication,
            HttpServletRequest request) {

        log.info("User liking project with ID: {}", id);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ProjectResponse response = projectService.likeProject(id, userPrincipal.getId());
        analyticsService.recordEvent(Analytics.EventType.PROJECT_LIKE, "project.like", userPrincipal.getId(),
                userPrincipal.getUsername(), response.getId(), response.getTitle(), "like", request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/like")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ProjectResponse> unlikeProject(@PathVariable Long id, Authentication authentication,
            HttpServletRequest request) {

        log.info("User unliking project with ID: {}", id);

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        ProjectResponse response = projectService.unlikeProject(id, userPrincipal.getId());
        analyticsService.recordEvent(Analytics.EventType.PROJECT_UNLIKE, "project.unlike", userPrincipal.getId(),
                userPrincipal.getUsername(), response.getId(), response.getTitle(), "unlike", request);
        return ResponseEntity.ok(response);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return null;
        }
        return ((UserPrincipal) authentication.getPrincipal()).getId();
    }

    private String extractUsername(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return null;
        }
        return ((UserPrincipal) authentication.getPrincipal()).getUsername();
    }
}
