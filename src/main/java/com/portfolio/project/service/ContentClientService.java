package com.portfolio.project.service;

import org.springframework.stereotype.Service;

import com.portfolio.project.client.ContentClient;
import com.portfolio.project.dto.BlogSummaryResponse;
import com.portfolio.project.dto.PaginationResponse;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContentClientService {

	private final ContentClient contentClient;

	@Retry(name = "contentService")
	@CircuitBreaker(name = "contentService", fallbackMethod = "getBlogsFallback")
	public PaginationResponse<BlogSummaryResponse> getBlogsFromContentService(Integer page, Integer size) {
		log.info("Calling Content Service...");
		return contentClient.getBlogs(page, size);
	}

	private PaginationResponse<BlogSummaryResponse> getBlogsFallback(Integer page, Integer size, Throwable throwable) {
		log.error("========== CIRCUIT BREAKER FALLBACK ==========");
		log.error("Content Service failed: {}", throwable.getMessage());
		return new PaginationResponse<>();
	}
}
