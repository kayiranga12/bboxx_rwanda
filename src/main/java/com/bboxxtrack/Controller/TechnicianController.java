package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.TechnicianTracker;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.TaskService;
import com.bboxxtrack.Service.TechnicianTrackerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/technician")
public class TechnicianController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TechnicianTrackerService trackerService;

    @GetMapping("/dashboard")
    public String technicianDashboard(Model model, HttpSession session) {
        User technician = (User) session.getAttribute("user");

        if (technician == null || !"Technician".equals(technician.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("tasks", taskService.getAllTasks().stream()
                .filter(task -> technician.getId().equals(task.getAssignedToUserId()))
                .collect(Collectors.toList()));

        model.addAttribute("trackers", trackerService.getAllTrackers().stream()
                .filter(t -> technician.getId().equals(t.getTechnicianId()))
                .collect(Collectors.toList()));

        return "technician/dashboard";
    }

    @PostMapping("/tracker")
    public String saveTracker(@RequestParam String gpsCoordinates,
                              @RequestParam String statusUpdate,
                              HttpSession session) {
        User technician = (User) session.getAttribute("user");

        if (technician == null || !"Technician".equals(technician.getRole())) {
            return "redirect:/login";
        }

        TechnicianTracker tracker = new TechnicianTracker();
        tracker.setTechnicianId(technician.getId());
        tracker.setGpsCoordinates(gpsCoordinates);
        tracker.setStatusUpdate(statusUpdate);
        tracker.setTimestamp(LocalDateTime.now());

        trackerService.saveTracker(tracker);

        return "redirect:/technician/dashboard";
    }
}
