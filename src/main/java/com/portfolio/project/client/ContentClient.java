package com.portfolio.project.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.portfolio.project.dto.BlogSummaryResponse;
import com.portfolio.project.dto.PaginationResponse;

@FeignClient(name = "content-service")
public interface ContentClient {

	@GetMapping("/api/v1/blog/blogs")
    PaginationResponse<BlogSummaryResponse> getBlogs(
            @RequestParam int page,
            @RequestParam int size);

}