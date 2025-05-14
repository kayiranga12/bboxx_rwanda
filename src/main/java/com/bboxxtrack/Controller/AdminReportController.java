package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.*;
import com.bboxxtrack.Service.CustomerService;
import com.bboxxtrack.Service.InventoryService;
import com.bboxxtrack.Service.MaintenanceScheduleService;
import com.bboxxtrack.Service.ProjectService;
import com.bboxxtrack.Service.TaskService;
import com.bboxxtrack.Service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// **IMPORTANT**: use iText PDF imports, not Swing!
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired private UserService userService;
    @Autowired private CustomerService customerService;
    @Autowired private ProjectService projectService;
    @Autowired private TaskService taskService;
    @Autowired private InventoryService inventoryService;
    @Autowired private MaintenanceScheduleService scheduleService;

    @GetMapping
    public String viewReports(
            @RequestParam(required=false) String from,
            @RequestParam(required=false) String to,
            Model model,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        // parse date-range if provided
        LocalDate start = (from != null && !from.isEmpty()) ? LocalDate.parse(from) : null;
        LocalDate end   = (to   != null && !to.isEmpty())   ? LocalDate.parse(to)   : null;

        // load all data
        List<User> users               = userService.getAllUsers();
        List<?> customers              = customerService.getAllCustomers();
        List<Project> projects         = projectService.getAllProjects();
        List<Task> tasks               = taskService.getAllTasks();
        List<Inventory> inventory      = inventoryService.getAllInventory();
        List<MaintenanceSchedule> schedules = scheduleService.getAllSchedules();

        // metrics
        long totalUsers      = users.size();
        long totalCustomers  = customers.size();
        long totalProjects   = projects.size();
        long totalTasks      = tasks.size();
        long totalInventory  = inventory.size();
        long visitsCompleted = schedules.stream()
                .filter(s -> "Completed".equalsIgnoreCase(s.getStatus()))
                .count();
        long visitsPending   = schedules.size() - visitsCompleted;

        // group counts
        Map<String,Long> taskStatusCounts    = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        Map<String,Long> projectStatusCounts = projects.stream()
                .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalProjects", totalProjects);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("totalInventory", totalInventory);
        model.addAttribute("visitsCompleted", visitsCompleted);
        model.addAttribute("visitsPending", visitsPending);

        model.addAttribute("taskStatusCounts", taskStatusCounts);
        model.addAttribute("projectStatusCounts", projectStatusCounts);

        model.addAttribute("from", from);
        model.addAttribute("to", to);

        return "admin/reports";
    }

    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        // set up response headers
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"bboxxtrack_report.csv\"");

        // gather all data
        List<User> users               = userService.getAllUsers();
        List<Customer> customers       = customerService.getAllCustomers();
        List<Project> projects         = projectService.getAllProjects();
        List<Task> tasks               = taskService.getAllTasks();
        List<Inventory> inventoryItems = inventoryService.getAllInventory();
        List<MaintenanceSchedule> schedules = scheduleService.getAllSchedules();

        long visitsCompleted = schedules.stream()
                .filter(s -> "Completed".equalsIgnoreCase(s.getStatus()))
                .count();
        long visitsPending = schedules.size() - visitsCompleted;

        // compute breakdowns
        Map<String,Long> taskStatusCounts = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        Map<String,Long> projectStatusCounts = projects.stream()
                .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));

        try (PrintWriter writer = response.getWriter()) {
            // Metrics section
            writer.println("Metric,Count");
            writer.printf("Total Users,%d%n", users.size());
            writer.printf("Total Customers,%d%n", customers.size());
            writer.printf("Total Projects,%d%n", projects.size());
            writer.printf("Total Tasks,%d%n", tasks.size());
            writer.printf("Total Inventory Items,%d%n", inventoryItems.size());
            writer.printf("Visits Completed,%d%n", visitsCompleted);
            writer.printf("Visits Pending,%d%n", visitsPending);

            writer.println(); // blank line

            // Task status breakdown
            writer.println("Task Status,Count");
            for (var entry : taskStatusCounts.entrySet()) {
                writer.printf("%s,%d%n", entry.getKey(), entry.getValue());
            }

            writer.println();

            // Project status breakdown
            writer.println("Project Status,Count");
            for (var entry : projectStatusCounts.entrySet()) {
                writer.printf("%s,%d%n", entry.getKey(), entry.getValue());
            }

            writer.flush();
        }
    }

}
