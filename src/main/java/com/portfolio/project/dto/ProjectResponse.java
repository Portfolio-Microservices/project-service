package com.portfolio.project.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectResponse {
    private Long id;
    private String title;
    private String description;
    private String techStack;
    private String githubUrl;
    private String liveUrl;
    private Integer viewsCount;
    private Integer likesCount;
    private Boolean userHasLiked;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
