package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.ProjectService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager")
public class ManagerProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/dashboard")
    public String managerDashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Project Manager".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Example stats you might want to show:
//        long total = projectService.countByManager(user.getId());
//        long inProgress = projectService.countByManagerAndStatus(user.getId(), "IN_PROGRESS");
//        long completed  = projectService.countByManagerAndStatus(user.getId(), "COMPLETED");

        model.addAttribute("managerName", user.getUsername());
//        model.addAttribute("totalProjects", total);
//        model.addAttribute("inProgressCount", inProgress);
//        model.addAttribute("completedCount", completed);

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


