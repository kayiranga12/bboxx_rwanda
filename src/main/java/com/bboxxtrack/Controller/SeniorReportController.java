// src/main/java/com/bboxxtrack/Controller/SeniorReportController.java
package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.MaintenanceSchedule;
import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.User;
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

@Controller
@RequestMapping("/senior/reports")
public class SeniorReportController {

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
        User senior = (User) session.getAttribute("user");
        if (senior == null || !"Senior Management".equals(senior.getRole())) {
            return "redirect:/login";
        }

        LocalDate start = (from!=null && !from.isEmpty()) ? LocalDate.parse(from) : null;
        LocalDate end   = (to!=null   && !to.isEmpty())   ? LocalDate.parse(to)   : null;

        List<User> users       = userService.getAllUsers();
        List<?> customers      = customerService.getAllCustomers();
        List<Project> projects = projectService.getAllProjects();
        List<Task> tasks       = taskService.getAllTasks();
        List<?> inventory      = inventoryService.getAllInventory();
        List<MaintenanceSchedule> sched = scheduleService.getAllSchedules();

        long totalUsers     = users.size();
        long totalCustomers = customers.size();
        long totalProjects  = projects.size();
        long totalTasks     = tasks.size();
        long totalInventory = inventory.size();

        long visitsCompleted = sched.stream()
                .filter(s -> "Completed".equalsIgnoreCase(s.getStatus()))
                .count();
        long visitsPending = sched.size() - visitsCompleted;

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

        // for Chart.js
        model.addAttribute("taskLabels",   List.copyOf(taskStatusCounts.keySet()));
        model.addAttribute("taskData",     List.copyOf(taskStatusCounts.values()));
        model.addAttribute("projectLabels",List.copyOf(projectStatusCounts.keySet()));
        model.addAttribute("projectData",  List.copyOf(projectStatusCounts.values()));

        return "senior/reports";
    }

    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response, HttpSession session) throws IOException {
        User senior = (User) session.getAttribute("user");
        if (senior == null || !"Senior Management".equals(senior.getRole())) {
            response.sendRedirect("/login");
            return;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition","attachment; filename=senior_report.csv");

        List<User> users       = userService.getAllUsers();
        List<?> customers      = customerService.getAllCustomers();
        List<Project> projects = projectService.getAllProjects();
        List<Task> tasks       = taskService.getAllTasks();
        List<?> inventory      = inventoryService.getAllInventory();
        List<MaintenanceSchedule> sched = scheduleService.getAllSchedules();

        long visitsCompleted = sched.stream()
                .filter(s -> "Completed".equalsIgnoreCase(s.getStatus()))
                .count();
        long visitsPending = sched.size() - visitsCompleted;

        Map<String,Long> taskStatusCounts    = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        Map<String,Long> projectStatusCounts = projects.stream()
                .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));

        try (PrintWriter w = response.getWriter()) {
            w.println("Metric,Count");
            w.printf("Total Users,%d%n", users.size());
            w.printf("Total Customers,%d%n", customers.size());
            w.printf("Total Projects,%d%n", projects.size());
            w.printf("Total Tasks,%d%n", tasks.size());
            w.printf("Total Inventory Items,%d%n", inventory.size());
            w.printf("Visits Completed,%d%n", visitsCompleted);
            w.printf("Visits Pending,%d%n", visitsPending);
            w.println();
            w.println("Task Status,Count");
            taskStatusCounts.forEach((k,v)->w.printf("%s,%d%n",k,v));
            w.println();
            w.println("Project Status,Count");
            projectStatusCounts.forEach((k,v)->w.printf("%s,%d%n",k,v));
        }
    }
}
