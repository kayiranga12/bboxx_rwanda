package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.*;
import com.bboxxtrack.Service.*;

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
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin/reports")
public class AdminReportController {

    @Autowired private PdfReportService pdfReportService;
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

        LocalDate start = (from != null && !from.isEmpty()) ? LocalDate.parse(from) : null;
        LocalDate end   = (to   != null && !to.isEmpty())   ? LocalDate.parse(to)   : null;

        List<User> users               = userService.getAllUsers();
        List<Customer> customers       = customerService.getAllCustomers();
        List<Project> projects         = projectService.getAllProjects();
        List<Task> tasks               = taskService.getAllTasks();
        List<Inventory> inventory      = inventoryService.getAllInventory();
        List<MaintenanceSchedule> schedules = scheduleService.getAllSchedules();

        long totalUsers      = users.size();
        long totalCustomers  = customers.size();
        long totalProjects   = projects.size();
        long totalTasks      = tasks.size();
        long totalInventory  = inventory.size();
        long visitsCompleted = schedules.stream()
                .filter(s -> "Completed".equalsIgnoreCase(s.getStatus()))
                .count();
        long visitsPending   = schedules.size() - visitsCompleted;

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
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"bboxxtrack_report.csv\"");

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

        Map<String,Long> taskStatusCounts = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        Map<String,Long> projectStatusCounts = projects.stream()
                .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Metric,Count");
            writer.printf("Total Users,%d%n", users.size());
            writer.printf("Total Customers,%d%n", customers.size());
            writer.printf("Total Projects,%d%n", projects.size());
            writer.printf("Total Tasks,%d%n", tasks.size());
            writer.printf("Total Inventory Items,%d%n", inventoryItems.size());
            writer.printf("Visits Completed,%d%n", visitsCompleted);
            writer.printf("Visits Pending,%d%n", visitsPending);

            writer.println();

            writer.println("Task Status,Count");
            for (var entry : taskStatusCounts.entrySet()) {
                writer.printf("%s,%d%n", entry.getKey(), entry.getValue());
            }

            writer.println();

            writer.println("Project Status,Count");
            for (var entry : projectStatusCounts.entrySet()) {
                writer.printf("%s,%d%n", entry.getKey(), entry.getValue());
            }

            writer.flush();
        }
    }



    /**
     * Gathers metrics and streams back a PDF.
     */
    @GetMapping("/export/pdf")
    public void exportPdf(HttpSession session,
                          HttpServletResponse response) throws Exception {

        // 1) authorization check
        var user = session.getAttribute("user");
        if (user == null || !"Admin".equals(((com.bboxxtrack.Model.User)user).getRole())) {
            response.sendRedirect("/login");
            return;
        }

        // 2) compute real metrics
        long totalUsers      = userService.getAllUsers().size();
        long totalCustomers  = customerService.getAllCustomers().size();
        long totalProjects   = projectService.getAllProjects().size();
        long totalTasks      = taskService.getAllTasks().size();
        long totalInventory  = inventoryService.getAllInventory().size();
        long visitsCompleted = scheduleService.getAllSchedules().stream()
                .filter(s -> "Completed".equalsIgnoreCase(s.getStatus()))
                .count();

        // 3) insert into ordered LinkedHashMap
        Map<String,Long> metrics = new LinkedHashMap<>();
        metrics.put("Total Users",         totalUsers);
        metrics.put("Total Customers",     totalCustomers);
        metrics.put("Total Projects",      totalProjects);
        metrics.put("Total Tasks",         totalTasks);
        metrics.put("Inventory Items",     totalInventory);
        metrics.put("Visits Completed",    visitsCompleted);
        metrics.put("Visits Pending",      scheduleService.getAllSchedules().size() - visitsCompleted);

        // 4) generate PDF bytes
        byte[] pdfBytes = pdfReportService.generateReport(
                "BBOXX Rwanda",          // company name in header
                metrics,
                new Date()               // today
        );

        // 5) stream out
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=AdminReport.pdf");
        response.getOutputStream().write(pdfBytes);
        response.getOutputStream().flush();
    }
}