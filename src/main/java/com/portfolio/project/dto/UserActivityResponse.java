package com.portfolio.project.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserActivityResponse {
    private Long userId;
    private String username;
    private String email;
    private long totalActions;
    private long pageVisits;
    private long projectViews;
    private long projectLikes;
    private long searches;
    private LocalDateTime firstActivity;
    private LocalDateTime lastActivity;
    private long daysSinceLastActive;
}
