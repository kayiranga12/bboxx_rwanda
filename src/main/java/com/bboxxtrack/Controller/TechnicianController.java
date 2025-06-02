package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.TechnicianTracker;
import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.TaskService;
import com.bboxxtrack.Service.TechnicianTrackerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/technician")
public class TechnicianController {

    @Autowired private TaskService taskService;
    @Autowired private TechnicianTrackerService trackerService;

    @GetMapping("/dashboard")
    public String technicianDashboard(Model model, HttpSession session) {
        User technician = (User) session.getAttribute("user");
        if (technician == null || !"Technician".equals(technician.getRole())) {
            return "redirect:/login";
        }

        // filter tasks by assignedTo relationship
        List<Task> myTasks = taskService.getAllTasks().stream()
                .filter(t ->
                        t.getAssignedTo() != null &&
                                technician.getId().equals(t.getAssignedTo().getId())
                )
                .collect(Collectors.toList());

        // filter trackers by technician relationship
        List<TechnicianTracker> myTrackers = trackerService.getAllTrackers().stream()
                .filter(tr ->
                        tr.getTechnician() != null &&
                                technician.getId().equals(tr.getTechnician().getId())
                )
                .collect(Collectors.toList());

        model.addAttribute("tasks",    myTasks);
        model.addAttribute("trackers", myTrackers);
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
        tracker.setTechnician(technician);
        tracker.setGpsCoordinates(gpsCoordinates);
        tracker.setStatusUpdate(statusUpdate);
        tracker.setTimestamp(LocalDateTime.now());

        trackerService.saveTracker(tracker);
        return "redirect:/technician/dashboard";
    }
}
