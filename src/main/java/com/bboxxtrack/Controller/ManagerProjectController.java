package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.ProjectService;
import com.bboxxtrack.Service.TaskService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager")
public class ManagerProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TaskService taskService;

        @GetMapping("/dashboard")
        public String managerDashboard(Model model, HttpSession session) {
            User user = (User) session.getAttribute("user");
            if (user == null || !"Project Manager".equals(user.getRole())) {
                return "redirect:/login";
            }

            model.addAttribute("managerName", user.getUsername());

            // Get only projects managed by this manager
            List<Project> managerProjects = projectService.getProjectsByManager(user.getId());
            List<com.bboxxtrack.Model.Task> allTasks = taskService.getAllTasks();;

            // Add counts
            model.addAttribute("totalProjects", projectService.getAllProjects().size());
            model.addAttribute("totalTasks", taskService.getAllTasks().size());
            model.addAttribute("managerProjects", projectService.getProjectsByManager(user.getId()).size());

            // Calculate task statuses
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


            // Calculate project statuses for manager's projects only
            long ongoingProjects = managerProjects.stream()
                    .filter(p -> "Ongoing".equalsIgnoreCase(p.getStatus())).count();
            long completedProjects = managerProjects.stream()
                    .filter(p -> "Completed".equalsIgnoreCase(p.getStatus())).count();
            long upcomingProjects = managerProjects.stream()
                    .filter(p -> "Upcoming".equalsIgnoreCase(p.getStatus())).count();

            // Add task status metrics to model
            model.addAttribute("completedTasks", completedTasks);
            model.addAttribute("inProgressTasks", inProgressTasks);
            model.addAttribute("pendingTasks", pendingTasks);
            model.addAttribute("overdueTasks", overdueTasks);

            // Add project status metrics to model
            model.addAttribute("ongoingProjects", ongoingProjects);
            model.addAttribute("completedProjects", completedProjects);
            model.addAttribute("upcomingProjects", upcomingProjects);

            // Prepare data for timeline chart (manager's projects only)
            List<String> projectNames = new ArrayList<>();
            List<String> projectStartDates = new ArrayList<>();
            List<String> projectEndDates = new ArrayList<>();
            List<String> projectStatuses = new ArrayList<>();

            for (Project project : managerProjects) {
                projectNames.add(project.getProjectTitle());
                projectStartDates.add(project.getStartDate().toString());
                projectEndDates.add(project.getEndDate().toString());
                projectStatuses.add(project.getStatus());
            }

            model.addAttribute("projectNames", projectNames);
            model.addAttribute("projectStartDates", projectStartDates);
            model.addAttribute("projectEndDates", projectEndDates);
            model.addAttribute("projectStatuses", projectStatuses);

            // Add chart data
            model.addAttribute("completedTasks", completedTasks);
            model.addAttribute("inProgressTasks", inProgressTasks);
            model.addAttribute("pendingTasks", pendingTasks);
            model.addAttribute("overdueTasks", overdueTasks);
            model.addAttribute("ongoingProjects", ongoingProjects);
            model.addAttribute("completedProjects", completedProjects);
            model.addAttribute("upcomingProjects", upcomingProjects);

            return "manager/dashboard";
        }

    @GetMapping("/projects")
    public String listProjects(
            @RequestParam(required = false) String status,
            Model model,
            HttpSession session
    ) {
        return getString(status, model, session, projectService);
    }

    static String getString(@RequestParam(required = false) String status, Model model, HttpSession session, ProjectService projectService) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Project Manager".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Project> allProjects = projectService.getProjectsByManager(user.getId());
        List<Project> projects = (status == null || status.isEmpty())
                ? allProjects
                : allProjects.stream()
                .filter(p -> status.equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());

        model.addAttribute("projects", projects);
        model.addAttribute("filterStatus", status);
        model.addAttribute("newProject", new Project());
        return "manager/projects";
    }

    @PostMapping("/projects/add")
    public String addProject(@ModelAttribute("newProject") Project project, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Project Manager".equals(user.getRole())) {
            return "redirect:/login";
        }
        project.setManager(user);
        projectService.saveProject(project);
        return "redirect:/manager/projects";
    }

    @PostMapping("/projects/edit")
    public String editProject(@ModelAttribute Project project, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Project Manager".equals(user.getRole())) {
            return "redirect:/login";
        }
        project.setManager(user);
        projectService.saveProject(project);
        return "redirect:/manager/projects";
    }

    @GetMapping("/projects/delete/{id}")
    public String deleteProject(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Project Manager".equals(user.getRole())) {
            return "redirect:/login";
        }
        projectService.deleteProject(id);
        return "redirect:/manager/projects";
    }

    @GetMapping("/projects/export")
    public void exportCsv(HttpServletResponse response, HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Project Manager".equals(user.getRole())) {
            response.sendRedirect("/login");
            return;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=projects.csv");
        List<Project> all = projectService.getProjectsByManager(user.getId());
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try (PrintWriter writer = response.getWriter()) {
            writer.println("Title,Status,Location,Start Date,End Date");
            for (Project p : all) {
                String start = p.getStartDate() != null ? p.getStartDate().format(fmt) : "";
                String end = p.getEndDate() != null ? p.getEndDate().format(fmt) : "";
                writer.printf("\"%s\",%s,%s,%s,%s%n",
                        p.getProjectTitle().replace("\"","\"\""),
                        p.getStatus(),
                        p.getLocation() != null ? p.getLocation() : "",
                        start,
                        end
                );
            }
        }
    }

    @GetMapping("/projects/import")
    public String showImportForm(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Project Manager".equals(user.getRole())) {
            return "redirect:/login";
        }
        return "manager/projects_import";
    }

    @PostMapping("/projects/import")
    public String importProjects(@RequestParam("file") MultipartFile file, HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Project Manager".equals(user.getRole())) {
            return "redirect:/login";
        }
        projectService.importFromCsv(file);
        return "redirect:/manager/projects";
    }
}


