package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Tracker;
import com.bboxxtrack.Repository.TrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TrackerService {
    @Autowired private TrackerRepository repo;

    public Tracker save(Tracker t) {
        return repo.save(t);
    }

    public List<Tracker> getByUser(Long userId) {
        return repo.findAllByUserIdOrderByTimestampDesc(userId);
    }
}
