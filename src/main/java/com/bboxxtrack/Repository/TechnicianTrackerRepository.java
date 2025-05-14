package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.TechnicianTracker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TechnicianTrackerRepository extends JpaRepository<TechnicianTracker, Long> {
}
