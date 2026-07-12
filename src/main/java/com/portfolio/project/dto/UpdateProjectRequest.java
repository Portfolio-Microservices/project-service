package com.portfolio.project.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProjectRequest {

    @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters")
    private String title;

    @Size(min = 10, max = 5000, message = "Description must be between 10 and 5000 characters")
    private String description;

    @Size(min = 3, max = 500, message = "Tech stack must be between 3 and 500 characters")
    private String techStack;

    private String githubUrl;

    private String liveUrl;
}
