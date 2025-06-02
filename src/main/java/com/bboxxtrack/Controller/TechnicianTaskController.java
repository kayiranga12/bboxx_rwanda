package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.DocumentService;
import com.bboxxtrack.Service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/technician")
public class TechnicianTaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private DocumentService documentService;

    @GetMapping("/tasks")
    public String showTasks(HttpSession session, Model model,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String q) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Technician".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Task> tasks = taskService.getTasksByTechnician(user.getId(), status, q);
        model.addAttribute("tasks", tasks);
        model.addAttribute("filterStatus", status);
        model.addAttribute("searchQuery", q);

        // KPI metrics
        long assignedCount = tasks.stream().filter(t -> "Assigned".equals(t.getStatus())).count();
        long inProgressCount = tasks.stream().filter(t -> "In Progress".equals(t.getStatus())).count();
        long doneCount = tasks.stream().filter(t -> "Done".equals(t.getStatus())).count();
        long totalTasks = tasks.size();
        long overdueTasks = tasks.stream()
                .filter(t -> t.getDueDate().isBefore(java.time.LocalDate.now()) && !"Done".equals(t.getStatus()))
                .count();
        long dueTodayTasks = tasks.stream()
                .filter(t -> t.getDueDate().isEqual(java.time.LocalDate.now()) && !"Done".equals(t.getStatus()))
                .count();
        String completionRate = totalTasks > 0 ? String.format("%.0f%%", (doneCount * 100.0) / totalTasks) : "0%";

        model.addAttribute("assignedCount", assignedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("doneCount", doneCount);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("overdueTasks", overdueTasks);
        model.addAttribute("dueTodayTasks", dueTodayTasks);
        model.addAttribute("completionRate", completionRate);
        model.addAttribute("statusLabels", List.of("Done", "In Progress", "Assigned"));
        model.addAttribute("statusData", List.of(doneCount, inProgressCount, assignedCount));

        return "technician/tasks";
    }

    @PostMapping("/task/update")
    public String updateTask(@RequestParam("id") Long id,
                             @RequestParam("status") String status,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Technician".equals(user.getRole())) {
            return "redirect:/login";
        }

        try {
            Task task = taskService.getTaskById(id);
            if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to update this task");
                return "redirect:/technician/dashboard";
            }
            task.setStatus(status);
            taskService.saveTask(task);
            redirectAttributes.addFlashAttribute("message", "Task status updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update task: " + e.getMessage());
        }

        return "redirect:/technician/dashboard";
    }

    @PostMapping("/tasks/update")
    public String updateTaskWithFiles(@RequestParam("id") Long id,
                                      @RequestParam("status") String status,
                                      @RequestParam(value = "files", required = false) MultipartFile[] files,
                                      HttpSession session,
                                      RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Technician".equals(user.getRole())) {
            return "redirect:/login";
        }

        try {
            Task task = taskService.getTaskById(id);
            if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You are not authorized to update this task");
                return "redirect:/technician/tasks";
            }
            task.setStatus(status);
            taskService.saveTask(task);

            if (files != null && files.length > 0) {
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        documentService.saveDocument(file, "TASK", id);
                    }
                }
            }
            redirectAttributes.addFlashAttribute("message", "Task updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update task: " + e.getMessage());
        }

        return "redirect:/technician/tasks";
    }
}