package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Inventory;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User user) {
        String generatedPassword = UUID.randomUUID().toString().substring(0, 8);
        user.setPassword(generatedPassword);

        userService.saveUser(user);

        String message = "Hello " + user.getFullName() + ",\n\n" +
                "Your BBOXXTrack account has been created.\n\n" +
                "Email: " + user.getEmail() + "\n" +
                "Password: " + generatedPassword + "\n\n" +
                "Login here: http://localhost:8081/login";

        emailService.sendEmail(user.getEmail(), "Your BBOXXTrack Credentials", message);

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

        // Dashboard statistics
        List<Map<String, Object>> dashboardStats = new ArrayList<>();
        dashboardStats.add(createStat("Total Users", userService.getAllUsers().size()));
        dashboardStats.add(createStat("Total Projects", projectService.getAllProjects().size()));
        dashboardStats.add(createStat("Total Tasks", taskService.getAllTasks().size()));
        dashboardStats.add(createStat("Total Inventory", inventoryService.getAllInventory().size()));

        // Task statistics
        long doneTasks = taskService.getAllTasks().stream()
                .filter(t -> "Done".equalsIgnoreCase(t.getStatus())).count();
        long pendingTasks = taskService.getAllTasks().size() - doneTasks;

        // Project statistics
        long ongoingProjects = projectService.getAllProjects().stream()
                .filter(p -> "Ongoing".equalsIgnoreCase(p.getStatus())).count();
        long completedProjects = projectService.getAllProjects().stream()
                .filter(p -> "Completed".equalsIgnoreCase(p.getStatus())).count();
        long upcomingProjects = projectService.getAllProjects().stream()
                .filter(p -> "Upcoming".equalsIgnoreCase(p.getStatus())).count();

        // Inventory statistics
        List<Inventory> inventories = inventoryService.getAllInventory();
        List<String> inventoryLabels = inventories.stream()
                .map(Inventory::getItemName)
                .collect(Collectors.toList());
        List<Integer> inventoryQuantities = inventories.stream()
                .map(Inventory::getQuantityAvailable)
                .collect(Collectors.toList());

        model.addAttribute("dashboardStats", dashboardStats);
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalProjects", projectService.getAllProjects().size());
        model.addAttribute("totalTasks", taskService.getAllTasks().size());
        model.addAttribute("doneTasks", doneTasks);
        model.addAttribute("pendingTasks", pendingTasks);
        model.addAttribute("ongoingProjects", ongoingProjects);
        model.addAttribute("completedProjects", completedProjects);
        model.addAttribute("upcomingProjects", upcomingProjects);
        model.addAttribute("inventoryLabels", inventoryLabels);
        model.addAttribute("inventoryQuantities", inventoryQuantities);
//        model.addAttribute("activities", activityService.getRecentActivities());

        return "admin/dashboard";
    }

    private Map<String, Object> createStat(String label, long value) {
        Map<String, Object> stat = new HashMap<>();
        stat.put("label", label);
        stat.put("value", value);
        return stat;
    }
}