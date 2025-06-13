package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.Ticket;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.DocumentService;
import com.bboxxtrack.Service.TaskService;
import com.bboxxtrack.Service.TicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/technician")
public class TechnicianTaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private DocumentService documentService;
    @Autowired private TicketService ticketService;

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

        // Task KPI metrics
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

        // Get assigned tickets
        List<Ticket> assignedTickets = ticketService.all().stream()
                .filter(t -> Objects.equals(t.getAssignedToUserId(), user.getId()))
                .collect(Collectors.toList());

        // Ticket KPI metrics
        long totalTickets = assignedTickets.size();

        // Ticket counts by stage
        long ticketsInProgress = assignedTickets.stream()
                .filter(t -> "IN_PROGRESS".equals(t.getStage() != null ? t.getStage().name() : ""))
                .count();
        long ticketsCompleted = assignedTickets.stream()
                .filter(t -> "COMPLETED".equals(t.getStage() != null ? t.getStage().name() : ""))
                .count();

        // Ticket counts by priority
        long highPriorityTickets = assignedTickets.stream()
                .filter(t -> "HIGH".equals(t.getPriority() != null ? t.getPriority().name() : ""))
                .count();
        long mediumPriorityTickets = assignedTickets.stream()
                .filter(t -> "MEDIUM".equals(t.getPriority() != null ? t.getPriority().name() : ""))
                .count();
        long normalPriorityTickets = assignedTickets.stream()
                .filter(t -> "NORMAL".equals(t.getPriority() != null ? t.getPriority().name() : "") || t.getPriority() == null)
                .count();

        // Ticket completion rate
        String ticketCompletionRate = totalTickets > 0 ?
                String.format("%.0f%%", (ticketsCompleted * 100.0) / totalTickets) : "0%";

        // Urgent tickets (could be based on urgency or high priority)
        long urgentTickets = assignedTickets.stream()
                .filter(t -> "URGENT".equals(t.getUrgency() != null ? t.getUrgency().name() : "") ||
                        "HIGH".equals(t.getPriority() != null ? t.getPriority().name() : ""))
                .count();

        // Active tickets (not completed)
        long activeTickets = assignedTickets.stream()
                .filter(t -> !"COMPLETED".equals(t.getStage() != null ? t.getStage().name() : ""))
                .count();

        // Create techNames map for displaying technician names
        Map<Long, String> techNames = new HashMap<>();
        Set<Long> techIds = assignedTickets.stream()
                .map(Ticket::getAssignedToUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        techIds.add(user.getId());

        // Populate techNames - you'll need to implement this based on your User service
        for (Long techId : techIds) {
            // Replace with your actual user lookup logic
            techNames.put(techId, "Technician " + techId); // Temporary - replace with actual user lookup
        }

        // Add task attributes to model
        model.addAttribute("assignedCount", assignedCount);
        model.addAttribute("inProgressCount", inProgressCount);
        model.addAttribute("doneCount", doneCount);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("overdueTasks", overdueTasks);
        model.addAttribute("dueTodayTasks", dueTodayTasks);
        model.addAttribute("completionRate", completionRate);
        model.addAttribute("statusLabels", List.of("Done", "In Progress", "Assigned"));
        model.addAttribute("statusData", List.of(doneCount, inProgressCount, assignedCount));

        // Add ticket attributes to model
        model.addAttribute("assignedTickets", assignedTickets);
        model.addAttribute("techNames", techNames);
        model.addAttribute("totalTickets", totalTickets);
        model.addAttribute("ticketsInProgress", ticketsInProgress);
        model.addAttribute("ticketsCompleted", ticketsCompleted);
        model.addAttribute("highPriorityTickets", highPriorityTickets);
        model.addAttribute("mediumPriorityTickets", mediumPriorityTickets);
        model.addAttribute("normalPriorityTickets", normalPriorityTickets);
        model.addAttribute("ticketCompletionRate", ticketCompletionRate);
        model.addAttribute("urgentTickets", urgentTickets);
        model.addAttribute("activeTickets", activeTickets);

        // Ticket stage data for charts
        model.addAttribute("ticketStageLabels", List.of("Completed", "In Progress", "Assigned"));
        model.addAttribute("ticketStageData", List.of(ticketsCompleted, ticketsInProgress));

        // Ticket priority data for charts
        model.addAttribute("ticketPriorityLabels", List.of("High", "Medium", "Normal"));
        model.addAttribute("ticketPriorityData", List.of(highPriorityTickets, mediumPriorityTickets, normalPriorityTickets));

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