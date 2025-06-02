// src/main/java/com/bboxxtrack/Repository/ProjectRepository.java
package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.Project;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    // findAll() is inherited; you don’t need to re‐declare it, but it’s okay if you do.
    List<Project> findAll();

    Page<Project> findAll(Pageable pageable);

    List<Project> findByStatus(String status);

    List<Project> findByManagerId(Long managerId);

    long countByManagerIdAndStartDateBetween(Long managerId, LocalDate start, LocalDate end);

    long countByManagerIdAndStatusAndStartDateBetween(Long managerId, String status, LocalDate start, LocalDate end);
}
