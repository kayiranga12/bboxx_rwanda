// src/main/java/com/bboxxtrack/Repository/ScheduledReportRepository.java
package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.ScheduledReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduledReportRepository extends JpaRepository<ScheduledReport,Long> {
    List<ScheduledReport> findByEnabledTrue();
}
