package com.portfolio.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.portfolio.project.entity.ProjectLike;

@Repository
public interface ProjectLikeRepository extends JpaRepository<ProjectLike, Long> {

    boolean existsByProject_IdAndUser_Id(Long projectId, Long userId);

    Optional<ProjectLike> findByProject_IdAndUser_Id(Long projectId, Long userId);

    void deleteByProject_Id(Long projectId);
    
	boolean existsByProjectIdAndUserId(Long projectId, Long userId);
}
