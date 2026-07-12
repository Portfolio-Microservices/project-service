package com.portfolio.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.portfolio.project.dto.ContactResponse;
import com.portfolio.project.dto.CreateContactRequest;
import com.portfolio.project.service.ContactService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
@Validated
public class ContactController {

    private final ContactService service;

    @PostMapping
    public ResponseEntity<ContactResponse> send(@Valid @RequestBody CreateContactRequest request) {
        ContactResponse response = service.save(request);
        return ResponseEntity.ok(response);
    }
}
