package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
