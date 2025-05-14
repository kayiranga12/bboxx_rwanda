package com.bboxxtrack.Service;

import com.bboxxtrack.Model.TechnicianTracker;
import com.bboxxtrack.Repository.TechnicianTrackerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TechnicianTrackerService {
    @Autowired
    private TechnicianTrackerRepository trackerRepository;

    public TechnicianTracker saveTracker(TechnicianTracker tracker) {
        return trackerRepository.save(tracker);
    }

    public List<TechnicianTracker> getAllTrackers() {
        return trackerRepository.findAll();
    }

    public TechnicianTracker getTrackerById(Long id) {
        return trackerRepository.findById(id).orElse(null);
    }

    public void deleteTracker(Long id) {
        trackerRepository.deleteById(id);
    }
}
