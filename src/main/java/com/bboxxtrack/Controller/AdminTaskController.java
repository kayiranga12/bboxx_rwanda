package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.EmailService;
import com.bboxxtrack.Service.ProjectService;
import com.bboxxtrack.Service.TaskService;
import com.bboxxtrack.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/tasks")
public class AdminTaskController {

    @Autowired private TaskService taskService;
    @Autowired private ProjectService projectService;
    @Autowired private UserService userService;
    @Autowired private EmailService emailService;    // ← inject here

    @GetMapping
    public String showTasks(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Project> projects = projectService.getAllProjects();
        List<User> technicians = userService.getAllUsers().stream()
                .filter(u -> "Technician".equals(u.getRole()))
                .collect(Collectors.toList());
        List<Task> tasks = taskService.getAllTasks();

        // lookup maps for UI
        Map<Long,String> techNames = technicians.stream()
                .collect(Collectors.toMap(User::getId, User::getFullName));
        Map<Long,String> projectNames = projects.stream()
                .collect(Collectors.toMap(Project::getId, Project::getProjectTitle));

        model.addAttribute("projects", projects);
        model.addAttribute("technicians", technicians);
        model.addAttribute("tasks", tasks);
        model.addAttribute("techNames", techNames);
        model.addAttribute("projectNames", projectNames);

        // for new-task form binding
        model.addAttribute("newTask", new Task());

        return "admin/tasks";
    }

    @PostMapping("/add")
    public String addTask(@ModelAttribute("newTask") Task task) {
        task.setStatus("Assigned");
        taskService.saveTask(task);

        // send calendar invite
        LocalDateTime dueStart = task.getDueDate().atStartOfDay();
        LocalDateTime dueEnd   = dueStart.plusHours(1);

        User tech = userService.getUserById(task.getAssignedToUserId());
        if (tech != null && tech.getEmail() != null) {
            emailService.sendCalendarInvite(
                    tech.getEmail(),
                    "New Task Assigned: " + task.getTaskName(),
                    "You have a new task due on " + task.getDueDate(),
                    dueStart,
                    dueEnd,
                    "task-" + task.getId() + "@bboxxtrack.com"
            );
        }

        return "redirect:/admin/tasks";
    }
}
