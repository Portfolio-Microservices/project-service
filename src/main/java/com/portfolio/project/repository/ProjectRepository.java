package com.portfolio.project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.portfolio.project.entity.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    Page<Project> findByDeletedAtIsNull(Pageable pageable);

    Page<Project> findByTitleContainingIgnoreCaseAndDeletedAtIsNull(String title, Pageable pageable);

    Page<Project> findByTechStackContainingIgnoreCaseAndDeletedAtIsNull(String techStack, Pageable pageable);

    Page<Project> findAllByDeletedAtIsNullOrderByViewsCountDesc(Pageable pageable);

    Page<Project> findAllByDeletedAtIsNullOrderByLikesCountDesc(Pageable pageable);
}
