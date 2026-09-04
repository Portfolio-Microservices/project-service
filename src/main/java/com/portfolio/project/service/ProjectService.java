package com.portfolio.project.service;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.portfolio.project.client.ContentClient;
import com.portfolio.project.dto.BlogSummaryResponse;
import com.portfolio.project.dto.CreateProjectRequest;
import com.portfolio.project.dto.DashboardResponse;
import com.portfolio.project.dto.PaginationResponse;
import com.portfolio.project.dto.ProjectResponse;
import com.portfolio.project.dto.UpdateProjectRequest;
import com.portfolio.project.entity.Project;
import com.portfolio.project.entity.ProjectLike;
import com.portfolio.project.entity.User;
import com.portfolio.project.exception.DuplicateActionException;
import com.portfolio.project.exception.InvalidInputException;
import com.portfolio.project.exception.ResourceNotFoundException;
import com.portfolio.project.repository.ProjectLikeRepository;
import com.portfolio.project.repository.ProjectRepository;
import com.portfolio.project.repository.UserRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final ProjectLikeRepository projectLikeRepository;
	private final UserRepository userRepository;
	private final ContentClient contentClient;
	private final ContentClientService contentClientService;

	private static final int DEFAULT_PAGE_SIZE = 20;
	private static final int MAX_PAGE_SIZE = 100;

	@Transactional(readOnly = true)
	@Cacheable(value = "projects", key = "#pageNumber + '_' + #pageSize")
	public PaginationResponse<ProjectResponse> getAllProjects(Integer pageNumber, Integer pageSize) {
		pageNumber = pageNumber == null || pageNumber < 0 ? 0 : pageNumber;
		pageSize = validatePageSize(pageSize);

		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<Project> projects = projectRepository.findByDeletedAtIsNull(pageable);

		return buildPaginationResponse(projects);
	}

	@Transactional(readOnly = true)
	public DashboardResponse getDashboardResponse(Integer page, Integer size) {
		DashboardResponse response = new DashboardResponse();
		response.setProjects(getAllProjects(page, size));
		response.setBlogs(contentClientService.getBlogsFromContentService(page, size));
		return response;
	}

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

	@Transactional(readOnly = true)
	public PaginationResponse<ProjectResponse> searchByTitle(String title, Integer pageNumber, Integer pageSize) {
		if (title == null || title.trim().isEmpty()) {
			throw new InvalidInputException("Search title cannot be empty");
		}

		pageNumber = pageNumber == null || pageNumber < 0 ? 0 : pageNumber;
		pageSize = validatePageSize(pageSize);

		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<Project> projects = projectRepository.findByTitleContainingIgnoreCaseAndDeletedAtIsNull(title.trim(),
				pageable);

		return buildPaginationResponse(projects);
	}

	@Transactional(readOnly = true)
	public PaginationResponse<ProjectResponse> getMostViewedProjects(Integer pageNumber, Integer pageSize) {
		pageNumber = pageNumber == null || pageNumber < 0 ? 0 : pageNumber;
		pageSize = validatePageSize(pageSize);

		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<Project> projects = projectRepository.findAllByDeletedAtIsNullOrderByViewsCountDesc(pageable);

		return buildPaginationResponse(projects);
	}

	@Transactional(readOnly = true)
	public PaginationResponse<ProjectResponse> getMostLikedProjects(Integer pageNumber, Integer pageSize) {
		pageNumber = pageNumber == null || pageNumber < 0 ? 0 : pageNumber;
		pageSize = validatePageSize(pageSize);

		Pageable pageable = PageRequest.of(pageNumber, pageSize);
		Page<Project> projects = projectRepository.findAllByDeletedAtIsNullOrderByLikesCountDesc(pageable);

		return buildPaginationResponse(projects);
	}

	@Transactional(readOnly = true)
	public ProjectResponse getProjectById(Long id) {
		Project project = findProjectById(id);
		return convertToResponse(project, null);
	}

	@Transactional(readOnly = true)
	public ProjectResponse getProjectById(Long id, Long userId) {
		Project project = findProjectById(id);
		return convertToResponse(project, userId);
	}

	@Transactional
	@CacheEvict(value = "projects", allEntries = true)
	public ProjectResponse createProject(CreateProjectRequest request) {
		validateCreateProjectRequest(request);

		Project project = Project.builder().title(request.getTitle().trim())
				.description(request.getDescription().trim()).techStack(request.getTechStack().trim())
				.githubUrl(request.getGithubUrl() != null ? request.getGithubUrl().trim() : null)
				.liveUrl(request.getLiveUrl() != null ? request.getLiveUrl().trim() : null).viewsCount(0).likesCount(0)
				.build();

		Project savedProject = projectRepository.save(project);
		log.info("Project created successfully with ID: {}", savedProject.getId());

		return convertToResponse(savedProject, null);
	}

	@Transactional
	@CacheEvict(value = "projects", allEntries = true)
	public ProjectResponse updateProject(Long id, UpdateProjectRequest request) {
		Project project = findProjectById(id);

		if (request.getTitle() != null && !request.getTitle().isBlank()) {
			project.setTitle(request.getTitle().trim());
		}
		if (request.getDescription() != null && !request.getDescription().isBlank()) {
			project.setDescription(request.getDescription().trim());
		}
		if (request.getTechStack() != null && !request.getTechStack().isBlank()) {
			project.setTechStack(request.getTechStack().trim());
		}
		if (request.getGithubUrl() != null && !request.getGithubUrl().isBlank()) {
			project.setGithubUrl(request.getGithubUrl().trim());
		}
		if (request.getLiveUrl() != null && !request.getLiveUrl().isBlank()) {
			project.setLiveUrl(request.getLiveUrl().trim());
		}

		Project updatedProject = projectRepository.save(project);
		log.info("Project updated successfully with ID: {}", id);

		return convertToResponse(updatedProject, null);
	}

	@Transactional
	public void deleteProject(Long id) {
		Project project = findProjectById(id);

		project.setDeletedAt(java.time.LocalDateTime.now());
		projectRepository.save(project);

		projectLikeRepository.deleteByProject_Id(id);

		log.info("Project soft deleted with ID: {}", id);
	}

	@Transactional
	public ProjectResponse incrementViewCounter(Long id) {
		Project project = findProjectById(id);

		project.setViewsCount(project.getViewsCount() + 1);
		Project updated = projectRepository.save(project);

		log.debug("View counter incremented for project ID: {}", id);
		return convertToResponse(updated, null);
	}

	@Transactional
	public ProjectResponse likeProject(Long projectId, Long userId) {
		if (projectId == null || projectId <= 0) {
			throw new InvalidInputException("Invalid project ID");
		}
		if (userId == null || userId <= 0) {
			throw new InvalidInputException("Invalid user ID");
		}

		Project project = findProjectById(projectId);

		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

		if (projectLikeRepository.existsByProject_IdAndUser_Id(projectId, userId)) {
			throw new DuplicateActionException("You have already liked this project");
		}

		ProjectLike like = ProjectLike.builder().project(project).user(user).build();

		projectLikeRepository.save(like);

		project.setLikesCount(project.getLikesCount() + 1);
		Project updated = projectRepository.save(project);

		log.info("Project liked by user ID: {} for project ID: {}", userId, projectId);

		return convertToResponse(updated, userId);
	}

	@Transactional
	public ProjectResponse unlikeProject(Long projectId, Long userId) {
		if (projectId == null || projectId <= 0) {
			throw new InvalidInputException("Invalid project ID");
		}
		if (userId == null || userId <= 0) {
			throw new InvalidInputException("Invalid user ID");
		}

		Project project = findProjectById(projectId);

		ProjectLike like = projectLikeRepository.findByProject_IdAndUser_Id(projectId, userId)
				.orElseThrow(() -> new DuplicateActionException("You have not liked this project"));

		projectLikeRepository.delete(like);

		project.setLikesCount(Math.max(0, project.getLikesCount() - 1));
		Project updated = projectRepository.save(project);

		log.info("Project unliked by user ID: {} for project ID: {}", userId, projectId);

		return convertToResponse(updated, userId);
	}

	@Transactional(readOnly = true)
	public boolean hasUserLikedProject(Long projectId, Long userId) {
		if (userId == null) {
			return false;
		}
		return projectLikeRepository.existsByProjectIdAndUserId(projectId, userId);
	}

	private Project findProjectById(Long id) {
		if (id == null || id <= 0) {
			throw new InvalidInputException("Invalid project ID");
		}

		Project project = projectRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Project not found with ID: " + id));

		if (project.getDeletedAt() != null) {
			throw new ResourceNotFoundException("Project not found with ID: " + id);
		}

		return project;
	}

	private void validateCreateProjectRequest(CreateProjectRequest request) {
		if (request == null) {
			throw new InvalidInputException("Project request cannot be null");
		}

		if (request.getTitle() == null || request.getTitle().isBlank()) {
			throw new InvalidInputException("Project title is required");
		}

		if (request.getTitle().length() < 3 || request.getTitle().length() > 255) {
			throw new InvalidInputException("Title must be between 3 and 255 characters");
		}

		if (request.getDescription() == null || request.getDescription().isBlank()) {
			throw new InvalidInputException("Project description is required");
		}

		if (request.getDescription().length() < 10 || request.getDescription().length() > 5000) {
			throw new InvalidInputException("Description must be between 10 and 5000 characters");
		}

		if (request.getTechStack() == null || request.getTechStack().isBlank()) {
			throw new InvalidInputException("Tech stack is required");
		}

		if (request.getTechStack().length() < 3 || request.getTechStack().length() > 500) {
			throw new InvalidInputException("Tech stack must be between 3 and 500 characters");
		}
	}

	private int validatePageSize(Integer pageSize) {
		if (pageSize == null || pageSize <= 0) {
			return DEFAULT_PAGE_SIZE;
		}
		return Math.min(pageSize, MAX_PAGE_SIZE);
	}

	private ProjectResponse convertToResponse(Project project, Long userId) {
		boolean userHasLiked = userId != null && hasUserLikedProject(project.getId(), userId);

		return ProjectResponse.builder().id(project.getId()).title(project.getTitle())
				.description(project.getDescription()).techStack(project.getTechStack())
				.githubUrl(project.getGithubUrl()).liveUrl(project.getLiveUrl()).viewsCount(project.getViewsCount())
				.likesCount(project.getLikesCount()).userHasLiked(userHasLiked).createdAt(project.getCreatedAt())
				.updatedAt(project.getUpdatedAt()).build();
	}

	private PaginationResponse<ProjectResponse> buildPaginationResponse(Page<Project> projects) {
		return PaginationResponse.<ProjectResponse>builder()
				.content(projects.getContent().stream().map(p -> convertToResponse(p, null)).toList())
				.pageNumber(projects.getNumber()).pageSize(projects.getSize())
				.totalElements(projects.getTotalElements()).totalPages(projects.getTotalPages())
				.isFirst(projects.isFirst()).isLast(projects.isLast()).hasNext(projects.hasNext())
				.hasPrevious(projects.hasPrevious()).build();
	}
}
