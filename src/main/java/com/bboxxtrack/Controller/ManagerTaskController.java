package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.ProjectService;
import com.bboxxtrack.Service.TaskService;
import com.bboxxtrack.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/manager/tasks")
public class ManagerTaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String showTaskForm(@RequestParam(required = false) String status, Model model, HttpSession session) {
        User manager = (User) session.getAttribute("user");
        if (manager == null || !"Project Manager".equals(manager.getRole())) {
            return "redirect:/login";
        }

        List<User> technicians = userService.getAllUsers().stream()
                .filter(u -> "Technician".equalsIgnoreCase(u.getRole()))
                .toList();

        List<Task> allTasks = taskService.getAllTasks();
        if (status != null && !status.isEmpty()) {
            allTasks = allTasks.stream()
                    .filter(t -> t.getStatus().equalsIgnoreCase(status))
                    .toList();
        }

        model.addAttribute("technicians", technicians);
        model.addAttribute("projects", projectService.getAllProjects());
        model.addAttribute("tasks", allTasks);
        model.addAttribute("filterStatus", status);

        return "manager/tasks";
    }

    @PostMapping("/edit")
    public String editTask(@ModelAttribute Task updatedTask) {
        Task existing = taskService.getTaskById(updatedTask.getId());
        if (existing != null) {
            existing.setTaskName(updatedTask.getTaskName());
            existing.setDescription(updatedTask.getDescription());
            existing.setDueDate(updatedTask.getDueDate());
            existing.setStatus(updatedTask.getStatus());
            existing.setProjectId(updatedTask.getProjectId());
            existing.setAssignedToUserId(updatedTask.getAssignedToUserId());
            taskService.saveTask(existing);
        }
        return "redirect:/manager/tasks";
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return "redirect:/manager/tasks";
    }

    @GetMapping("/export")
    public void exportTasksToCSV(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.csv");

        List<Task> tasks = taskService.getAllTasks();
        PrintWriter writer = response.getWriter();
        writer.println("Task Name,Description,Status,Due Date");

        for (Task t : tasks) {
            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\"%n",
                    t.getTaskName(), t.getDescription(), t.getStatus(), t.getDueDate());
        }

        writer.flush();
        writer.close();
    }

    @PostMapping("/add")
    public String assignTask(@ModelAttribute Task task, HttpSession session) {
        User manager = (User) session.getAttribute("user");
        if (manager == null || !"Project Manager".equals(manager.getRole())) {
            return "redirect:/login";
        }

        task.setAssignedDate(LocalDate.now());
        task.setStatus("Assigned");
        taskService.saveTask(task);
        return "redirect:/manager/tasks";
    }
}
