package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.InventoryService;
import com.bboxxtrack.Service.ProjectService;
import com.bboxxtrack.Service.TaskService;
import com.bboxxtrack.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@Controller
@RequestMapping("/senior")
public class SeniorManagementController {

    @Autowired private UserService userService;
    @Autowired private ProjectService projectService;
    @Autowired private TaskService taskService;
    @Autowired private InventoryService inventoryService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"Senior Management".equals(user.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalProjects", projectService.getAllProjects().size());
        model.addAttribute("totalTasks", taskService.getAllTasks().size());
        model.addAttribute("doneTasks", taskService.getAllTasks().stream().filter(t -> "Done".equalsIgnoreCase(t.getStatus())).count());
        model.addAttribute("pendingTasks", taskService.getAllTasks().stream().filter(t -> !"Done".equalsIgnoreCase(t.getStatus())).count());
        model.addAttribute("totalInventory", inventoryService.getAllInventory().size());

        return "senior/dashboard";
    }
}
