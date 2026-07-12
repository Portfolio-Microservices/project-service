package com.portfolio.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectAnalyticsResponse {
    private Long projectId;
    private String projectTitle;
    private long totalViews;
    private long totalLikes;
    private long totalUnlikes;
    private long likesToViewRatio;
    private long uniqueViewers;
    private long engagementRate;
}
