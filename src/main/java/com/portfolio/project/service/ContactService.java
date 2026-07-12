package com.portfolio.project.service;

import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.project.dto.ContactResponse;
import com.portfolio.project.dto.CreateContactRequest;
import com.portfolio.project.dto.PaginationResponse;
import com.portfolio.project.entity.Contact;
import com.portfolio.project.repository.ContactRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactService {

    private final ContactRepository repo;

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    @Transactional
    @CacheEvict(value = "contacts", allEntries = true)
    public ContactResponse save(CreateContactRequest request) {
        log.info("Saving new contact message from: {}", request.getEmail());

        Contact contact = new Contact();
        contact.setName(request.getName());
        contact.setEmail(request.getEmail());
        contact.setMessage(request.getMessage());

        Contact saved = repo.save(contact);
        log.info("Contact message saved successfully with ID: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "contacts", key = "#pageNumber + '_' + #pageSize")
    public PaginationResponse<ContactResponse> getAll(Integer pageNumber, Integer pageSize) {
        log.debug("Retrieving contact messages with pagination - page: {}, size: {}", pageNumber, pageSize);

        pageNumber = pageNumber == null || pageNumber < 0 ? 0 : pageNumber;
        pageSize = validatePageSize(pageSize);

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<Contact> contacts = repo.findAllByOrderByCreatedAtDesc(pageable);

        PaginationResponse<ContactResponse> response = buildPaginationResponse(contacts);
        log.info("Retrieved {} contact messages out of {} total", response.getContent().size(),
                response.getTotalElements());

        return response;
    }

    private int validatePageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(pageSize, MAX_PAGE_SIZE);
    }

    private PaginationResponse<ContactResponse> buildPaginationResponse(Page<Contact> contacts) {
        return PaginationResponse.<ContactResponse>builder()
                .content(contacts.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))
                .pageNumber(contacts.getNumber()).pageSize(contacts.getSize())
                .totalElements(contacts.getTotalElements()).totalPages(contacts.getTotalPages())
                .isFirst(contacts.isFirst()).isLast(contacts.isLast()).hasNext(contacts.hasNext())
                .hasPrevious(contacts.hasPrevious()).build();
    }

    private ContactResponse mapToResponse(Contact contact) {
        return new ContactResponse(contact.getId(), contact.getName(), contact.getEmail(), contact.getMessage(),
                contact.getCreatedAt(), contact.getUpdatedAt());
    }
}
