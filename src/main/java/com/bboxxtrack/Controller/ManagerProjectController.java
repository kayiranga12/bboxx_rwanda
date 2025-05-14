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

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager")
public class ManagerProjectController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/projects")
    public String listProjects(
            @RequestParam(required = false) String status,
            Model model,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Project Manager".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Project> allProjects = projectService.getAllProjects();
        List<Project> projects = (status == null || status.isEmpty())
                ? allProjects
                : allProjects.stream()
                .filter(p -> status.equalsIgnoreCase(p.getStatus()))
                .collect(Collectors.toList());

        Map<String, Long> statusCounts = allProjects.stream()
                .collect(Collectors.groupingBy(Project::getStatus, Collectors.counting()));
        List<String> statusLabels = List.copyOf(statusCounts.keySet());
        List<Long>   statusData   = List.copyOf(statusCounts.values());

        model.addAttribute("projects", projects);
        model.addAttribute("filterStatus", status);
        model.addAttribute("newProject", new Project());
        model.addAttribute("statusLabels", statusLabels);
        model.addAttribute("statusData", statusData);
        return "manager/projects";
    }

    @PostMapping("/projects/add")
    public String addProject(@ModelAttribute("newProject") Project project) {
        projectService.saveProject(project);
        return "redirect:/manager/projects";
    }

    @PostMapping("/projects/edit")
    public String editProject(@ModelAttribute Project project) {
        projectService.saveProject(project);
        return "redirect:/manager/projects";
    }

    @GetMapping("/projects/delete/{id}")
    public String deleteProject(@PathVariable Long id) {
        projectService.deleteProject(id);
        return "redirect:/manager/projects";
    }

    @GetMapping("/projects/export")
    public void exportCsv(HttpServletResponse response) throws Exception {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=projects.csv");
        List<Project> all = projectService.getAllProjects();
        try (PrintWriter writer = response.getWriter()) {
            writer.println("Title,Status,Location,Start Date,End Date");
            for (Project p : all) {
                writer.printf("\"%s\",%s,%s,%s,%s%n",
                        p.getProjectTitle().replace("\"","\"\""),
                        p.getStatus(),
                        p.getLocation(),
                        p.getStartDate(),
                        p.getEndDate()
                );
            }
        }
    }
}
