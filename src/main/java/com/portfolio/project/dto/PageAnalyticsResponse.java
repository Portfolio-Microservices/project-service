package com.portfolio.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageAnalyticsResponse {
    private String page;
    private long totalVisits;
    private long uniqueVisitors;
    private double avgVisitsPerDay;
    private long bounceCount;
}
