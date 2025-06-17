package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.DocumentService;
import com.bboxxtrack.Service.ProjectService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/projects")
public class AdminProjectController {

    @Autowired private ProjectService projectService;
    @Autowired private DocumentService documentService;

    @GetMapping
    public String showProjects(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        // Get all projects
        List<Project> projects = projectService.getAllProjects();
        model.addAttribute("projects", projects);
        model.addAttribute("newProject", new Project());

        // Calculate statistics
        long totalProjects = projects.size();
        long ongoingProjects = projects.stream()
                .filter(p -> "Ongoing".equalsIgnoreCase(p.getStatus()))
                .count();
        long completedProjects = projects.stream()
                .filter(p -> "Completed".equalsIgnoreCase(p.getStatus()))
                .count();
        long upcomingProjects = projects.stream()
                .filter(p -> "Upcoming".equalsIgnoreCase(p.getStatus()))
                .count();

        // Add statistics to model
        model.addAttribute("totalProjects", totalProjects);
        model.addAttribute("ongoingProjects", ongoingProjects);
        model.addAttribute("completedProjects", completedProjects);
        model.addAttribute("upcomingProjects", upcomingProjects);

        return "admin/projects";
    }

    @PostMapping("/add")
    public String addProject(
            @ModelAttribute Project project,
            @RequestParam(value="files", required=false) MultipartFile[] files,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        try {
            Project saved = projectService.saveProject(project);
            if (files != null) {
                for (MultipartFile f : files) {
                    if (!f.isEmpty()) {
                        documentService.save(f, "PROJECT", saved.getId());
                    }
                }
            }
            redirectAttributes.addFlashAttribute("message", "Project created successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create project: " + e.getMessage());
        }

        return "redirect:/admin/projects";
    }

    @GetMapping("/delete/{id}")
    public String deleteProject(@PathVariable Long id, HttpSession session) {
        projectService.deleteProject(id);
        return "redirect:/admin/projects";
    }

    // NEW: GET method to show edit form
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        try {
            Project project = projectService.getProjectById(id);
            model.addAttribute("project", project);
            model.addAttribute("isEdit", true);

            // Get all projects for the main list (to maintain the page structure)
            List<Project> projects = projectService.getAllProjects();
            model.addAttribute("projects", projects);

            // Calculate statistics
            long totalProjects = projects.size();
            long ongoingProjects = projects.stream()
                    .filter(p -> "Ongoing".equalsIgnoreCase(p.getStatus()))
                    .count();
            long completedProjects = projects.stream()
                    .filter(p -> "Completed".equalsIgnoreCase(p.getStatus()))
                    .count();
            long upcomingProjects = projects.stream()
                    .filter(p -> "Upcoming".equalsIgnoreCase(p.getStatus()))
                    .count();

            model.addAttribute("totalProjects", totalProjects);
            model.addAttribute("ongoingProjects", ongoingProjects);
            model.addAttribute("completedProjects", completedProjects);
            model.addAttribute("upcomingProjects", upcomingProjects);

            return "admin/projects";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Project not found: " + e.getMessage());
            return "redirect:/admin/projects";
        }
    }

    // NEW: POST method to handle edit form submission
    @PostMapping("/edit/{id}")
    public String updateProject(
            @PathVariable Long id,
            @ModelAttribute Project project,
            @RequestParam(value="files", required=false) MultipartFile[] files,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        try {
            // Set the ID to ensure we're updating the existing project
            project.setId(id);

            // Get the existing project to preserve manager if not set
            Project existingProject = projectService.getProjectById(id);
            if (project.getManager() == null && existingProject.getManager() != null) {
                project.setManager(existingProject.getManager());
            }

            Project updated = projectService.saveProject(project);

            // Handle file uploads if any
            if (files != null) {
                for (MultipartFile f : files) {
                    if (!f.isEmpty()) {
                        documentService.save(f, "PROJECT", updated.getId());
                    }
                }
            }

            redirectAttributes.addFlashAttribute("message", "Project updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update project: " + e.getMessage());
        }

        return "redirect:/admin/projects";
    }

}