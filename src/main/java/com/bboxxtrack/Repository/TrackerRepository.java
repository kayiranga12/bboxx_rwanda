package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.Tracker;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TrackerRepository extends JpaRepository<Tracker, Long> {
    List<Tracker> findAllByUserIdOrderByTimestampDesc(Long userId);
}
