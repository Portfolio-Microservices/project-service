package com.portfolio.project.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContactResponse {
    private Long id;
    private String name;
    private String email;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
