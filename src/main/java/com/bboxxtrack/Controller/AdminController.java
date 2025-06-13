package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Customer;
import com.bboxxtrack.Model.Inventory;
import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private CustomerService customerService;

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User user) {
        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(generatedPassword);

        userService.saveUser(user);

        String message = "Hello " + user.getUsername() + ",\n\n" +
                "Your BBOXXTrack account has been created.\n\n" +
                "Email: " + user.getEmail() + "\n" +
                "Password: " + generatedPassword + "\n\n" +
                "Login here: http://localhost:8081/login";

        try {
            emailService.sendEmail(user.getEmail(), "Your BBOXXTrack Credentials", message);
        } catch (Exception e) {
            // Log error but don't fail the user creation
            System.err.println("Failed to send email: " + e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/users")
    public String manageUsers(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("users", userService.getAllUsers());
        return "admin/manage_users";
    }

    @GetMapping("/dashboard")
    public String adminDashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Map<String, Object>> dashboardStats = new ArrayList<>();
        dashboardStats.add(createStat("Total Users", userService.getAllUsers().size()));
        dashboardStats.add(createStat("Total Projects", projectService.getAllProjects().size()));
        dashboardStats.add(createStat("Total Tasks", taskService.getAllTasks().size()));
        dashboardStats.add(createStat("Total Inventory", inventoryService.getAllInventory().size()));

        // Calculate task statuses for the chart
        List<com.bboxxtrack.Model.Task> allTasks = taskService.getAllTasks();

        long completedTasks = allTasks.stream()
                .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()) || "Done".equalsIgnoreCase(t.getStatus()))
                .count();

        long inProgressTasks = allTasks.stream()
                .filter(t -> "IN_PROGRESS".equalsIgnoreCase(t.getStatus()) || "In Progress".equalsIgnoreCase(t.getStatus()))
                .count();

        long pendingTasks = allTasks.stream()
                .filter(t -> "ASSIGNED".equalsIgnoreCase(t.getStatus()) || "Pending".equalsIgnoreCase(t.getStatus()))
                .count();

        long overdueTasks = allTasks.stream()
                .filter(t -> "OVERDUE".equalsIgnoreCase(t.getStatus()) || "Overdue".equalsIgnoreCase(t.getStatus()))
                .count();

        // If no specific overdue status, calculate based on due dates using LocalDate
        if (overdueTasks == 0) {
            overdueTasks = allTasks.stream()
                    .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(LocalDate.now())
                            && !"COMPLETED".equalsIgnoreCase(t.getStatus())
                            && !"Done".equalsIgnoreCase(t.getStatus()))
                    .count();
        }

        long doneTasks = completedTasks; // Keep for backward compatibility
        long totalPendingTasks = allTasks.size() - completedTasks; // Keep for backward compatibility

        long ongoingProjects = projectService.getAllProjects().stream()
                .filter(p -> "Ongoing".equalsIgnoreCase(p.getStatus())).count();
        long completedProjects = projectService.getAllProjects().stream()
                .filter(p -> "Completed".equalsIgnoreCase(p.getStatus())).count();
        long upcomingProjects = projectService.getAllProjects().stream()
                .filter(p -> "Upcoming".equalsIgnoreCase(p.getStatus())).count();

        List<Inventory> inventories = inventoryService.getAllInventory();
        List<String> inventoryLabels = inventories.stream()
                .map(Inventory::getItemName)
                .collect(Collectors.toList());
        List<Integer> inventoryQuantities = inventories.stream()
                .map(Inventory::getQuantityAvailable)
                .collect(Collectors.toList());

        List<Project> allProjects = projectService.getAllProjects();
        List<String> projectNames = new ArrayList<>();
        List<String> projectStartDates = new ArrayList<>();
        List<String> projectEndDates = new ArrayList<>();
        List<String> projectStatuses = new ArrayList<>();
        List<Long> projectDurations = new ArrayList<>();

        for (Project project : allProjects) {
            projectNames.add(project.getProjectTitle());
            projectStartDates.add(project.getStartDate().toString()); // Convert LocalDate to String
            projectEndDates.add(project.getEndDate().toString());
            projectStatuses.add(project.getStatus());

            // Calculate duration in days
            long duration = ChronoUnit.DAYS.between(project.getStartDate(), project.getEndDate());
            projectDurations.add(duration);
        }

        // Add to model for Gantt chart
        model.addAttribute("projectNames", projectNames);
        model.addAttribute("projectStartDates", projectStartDates);
        model.addAttribute("projectEndDates", projectEndDates);
        model.addAttribute("projectStatuses", projectStatuses);
        model.addAttribute("projectDurations", projectDurations);

        model.addAttribute("dashboardStats", dashboardStats);
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalProjects", projectService.getAllProjects().size());
        model.addAttribute("totalTasks", taskService.getAllTasks().size());
        model.addAttribute("doneTasks", doneTasks);
        model.addAttribute("pendingTasks", totalPendingTasks);

        // Add new task status data for the chart
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("inProgressTasks", inProgressTasks);
        model.addAttribute("pendingTasksChart", pendingTasks);
        model.addAttribute("overdueTasks", overdueTasks);

        model.addAttribute("ongoingProjects", ongoingProjects);
        model.addAttribute("completedProjects", completedProjects);
        model.addAttribute("upcomingProjects", upcomingProjects);
        model.addAttribute("inventoryLabels", inventoryLabels);
        model.addAttribute("inventoryQuantities", inventoryQuantities);

        model.addAttribute("customers", customerService.getAllCustomers().size());

        return "admin/dashboard";
    }

    private Map<String, Object> createStat(String label, long value) {
        Map<String, Object> stat = new HashMap<>();
        stat.put("label", label);
        stat.put("value", value);
        return stat;
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, HttpSession session) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}