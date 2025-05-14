package com.bboxxtrack.Service;

import com.bboxxtrack.Model.MaintenanceSchedule;
import com.bboxxtrack.Repository.MaintenanceScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MaintenanceScheduleService {
    @Autowired
    private MaintenanceScheduleRepository scheduleRepository;

    public MaintenanceSchedule saveSchedule(MaintenanceSchedule schedule) {
        return scheduleRepository.save(schedule);
    }

    public List<MaintenanceSchedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public MaintenanceSchedule getScheduleById(Long id) {
        return scheduleRepository.findById(id).orElse(null);
    }

    public void deleteSchedule(Long id) {
        scheduleRepository.deleteById(id);
    }
}
