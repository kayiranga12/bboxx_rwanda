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
    public String technicianDashboard(HttpSession session, Model model,
                                      @RequestParam(required = false) String status,
                                      @RequestParam(required = false) String q) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Technician".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Task> tasks = taskService.getTasksByTechnician(user.getId(), status, q);
        model.addAttribute("tasks", tasks);
        model.addAttribute("filterStatus", status);
        model.addAttribute("searchQuery", q);

        // KPI metrics
        long assignedCount = tasks.stream().filter(t -> "Assigned".equals(t.getStatus())).count();
        long inProgressCount = tasks.stream().filter(t -> "In Progress".equals(t.getStatus())).count();
        long doneCount = tasks.stream().filter(t -> "Done".equals(t.getStatus())).count();
        long totalTasks = tasks.size();
        long overdueTasks = tasks.stream()
                .filter(t -> t.getDueDate().isBefore(java.time.LocalDate.now()) && !"Done".equals(t.getStatus()))
                .count();
        long dueTodayTasks = tasks.stream()
                .filter(t -> t.getDueDate().isEqual(java.time.LocalDate.now()) && !"Done".equals(t.getStatus()))
                .count();
        String completionRate = totalTasks > 0 ? String.format("%.0f%%", (doneCount * 100.0) / totalTasks) : "0%";

        model.addAttribute("assignedCount", assignedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("doneCount", doneCount);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("overdueTasks", overdueTasks);
        model.addAttribute("dueTodayTasks", dueTodayTasks);
        model.addAttribute("completionRate", completionRate);
        model.addAttribute("statusLabels", List.of("Done", "In Progress", "Assigned"));
        model.addAttribute("statusData", List.of(doneCount, inProgressCount, assignedCount));

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
