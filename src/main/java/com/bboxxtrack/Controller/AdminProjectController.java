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


@Controller
@RequestMapping("/admin/projects")
public class AdminProjectController {

    @Autowired private ProjectService projectService;
    @Autowired private DocumentService documentService;

    // 1) Show form + list
    @GetMapping
    public String showProjects(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }
        // empty form backing object (if you ever need th:object)
        model.addAttribute("projects", projectService.getAllProjects());
        return "admin/projects";
    }

    // 2) Handle form submission (including files)
    @PostMapping("/add")
    public String addProject(@ModelAttribute Project project,
                             @RequestParam(value="files", required=false) MultipartFile[] files) throws Exception {
        // 2a) save the Project first
        Project saved = projectService.saveProject(project);

        // 2b) if any attachments, save them
        if (files != null) {
            for (MultipartFile f : files) {
                if (!f.isEmpty()) {
                    // refType = "PROJECT", refId = saved.getId()
                    documentService.save(f, "PROJECT", saved.getId());
                }
            }
        }

        // 3) redirect back to the combined GET view
        return "redirect:/admin/projects";
    }
}
