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
        model.addAttribute("projects", projectService.getAllProjects());
        model.addAttribute("newProject", new Project());
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

}