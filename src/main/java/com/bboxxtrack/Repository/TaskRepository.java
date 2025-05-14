package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {
}
