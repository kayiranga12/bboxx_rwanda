package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.ProjectService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/manager")
public class ProjectManagerController {

    @Autowired
    private ProjectService projectService;

    @GetMapping("/dashboard")
    public String managerDashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Project Manager".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Project> projects = projectService.getAllProjects();

        long total       = projects.size();
        long completed   = projects.stream()
                .filter(p -> "Completed".equalsIgnoreCase(p.getStatus()))
                .count();
        long ongoing     = projects.stream()
                .filter(p -> "Ongoing".equalsIgnoreCase(p.getStatus()))
                .count();
        long upcoming    = projects.stream()
                .filter(p -> "Upcoming".equalsIgnoreCase(p.getStatus()))
                .count();

        model.addAttribute("projects", projects);
        model.addAttribute("totalProjects", total);
        model.addAttribute("completedProjects", completed);
        model.addAttribute("ongoingProjects", ongoing);
        model.addAttribute("upcomingProjects", upcoming);

        return "manager/dashboard";
    }

}
