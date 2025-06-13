package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.Ticket;
import com.bboxxtrack.Model.TicketAssignmentDto;
import com.bboxxtrack.Model.TicketStage;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.ProjectService;
import com.bboxxtrack.Service.TaskService;
import com.bboxxtrack.Service.TicketService;
import com.bboxxtrack.Service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager/tasks")
public class ManagerTaskController {

    @Autowired private TaskService taskService;
    @Autowired private ProjectService projectService;
    @Autowired private UserService userService;
    @Autowired private TicketService ticketService; // ← make sure this is injected

    @GetMapping
    public String showTasks(Model model, HttpSession session) {
        User manager = (User) session.getAttribute("user");
        if (manager == null || !"Project Manager".equals(manager.getRole())) {
            return "redirect:/login";
        }

        // 1) Load all projects that this manager owns
        List<Project> projects = projectService.getProjectsByManager(manager.getId());

        // 2) Load all users whose role = “Technician” (exact role string must match your DB)
        List<User> techs = userService.getUsersByRole("Technician");

        // 3) Load all existing tasks
        List<Task> tasks = taskService.getAllTasks();

        // 4) **Load all tickets with assignedToUserId == null** (unassigned tickets)
        List<Ticket> unassignedTickets = ticketService.all().stream()
                .filter(t -> t.getAssignedToUserId() == null)
                .collect(Collectors.toList());

        // 5) **Load all tickets with assignedToUserId (assigned tickets)
        List<Ticket> assignedTickets = ticketService.all().stream()
                .filter(t -> t.getAssignedToUserId() != null)
                .collect(Collectors.toList());

        // Build maps for display (if you need them in the template)
        Map<Long, String> projectNames = projects.stream()
                .collect(Collectors.toMap(Project::getId, Project::getProjectTitle));
        Map<Long, String> techNames = techs.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        model.addAttribute("projects",          projects);
        model.addAttribute("technicians",       techs);
        model.addAttribute("tasks",             tasks);
        model.addAttribute("projectNames",      projectNames);
        model.addAttribute("techNames",         techNames);

        // Now add exactly these two so Thymeleaf can bind them:
        model.addAttribute("unassignedTickets", unassignedTickets);
        model.addAttribute("assignedTickets", assignedTickets);
        model.addAttribute("ticketAssignment",  new TicketAssignmentDto());

        return "manager/tasks";
    }

    @PostMapping("/add")
    public String assignTask(
            @RequestParam String taskName,
            @RequestParam String description,
            @RequestParam Long projectId,
            @RequestParam LocalDate duedate,
            @RequestParam Long assignedToUserId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User manager = (User) session.getAttribute("user");
        if (manager == null || !"Project Manager".equals(manager.getRole())) {
            return "redirect:/login";
        }

        try {
            Task task = new Task();
            task.setTaskName(taskName);
            task.setDescription(description);
            task.setDueDate(duedate);
            task.setAssignedDate(java.time.LocalDate.now());
            task.setStatus("Assigned");

            task.setProject(projectService.getById(projectId));

            User tech = userService.getUserById(assignedToUserId);
            task.setAssignedTo(tech);

            taskService.saveTask(task);
            redirectAttributes.addFlashAttribute("message","Task assigned successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error","Failed to assign task: " + e.getMessage());
        }
        return "redirect:/manager/tasks";
    }

    @PostMapping("/edit")
    public String editTask(
            @RequestParam Long id,
            @RequestParam String taskName,
            @RequestParam String description,
            @RequestParam String status,
            @RequestParam Long projectId,
            @RequestParam Long assignedToUserId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User manager = (User) session.getAttribute("user");
        if (manager == null || !"Project Manager".equals(manager.getRole())) {
            return "redirect:/login";
        }

        try {
            Task existing = taskService.getTaskById(id);
            existing.setTaskName(taskName);
            existing.setDescription(description);
            existing.setStatus(status);

            existing.setProject(projectService.getById(projectId));

            User tech = userService.getUserById(assignedToUserId);
            existing.setAssignedTo(tech);

            taskService.saveTask(existing);
            redirectAttributes.addFlashAttribute("message","Task updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error","Failed to update task: " + e.getMessage());
        }
        return "redirect:/manager/tasks";
    }

    @GetMapping("/delete/{id}")
    public String deleteTask(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User manager = (User) session.getAttribute("user");
        if (manager == null || !"Project Manager".equals(manager.getRole())) {
            return "redirect:/login";
        }

        try {
            taskService.deleteTask(id);
            redirectAttributes.addFlashAttribute("message","Task deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error","Failed to delete task: " + e.getMessage());
        }
        return "redirect:/manager/tasks";
    }

    @PostMapping("/assign-ticket")
    public String assignTicket(
            @Valid @ModelAttribute("ticketAssignment") TicketAssignmentDto dto,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        User manager = (User) session.getAttribute("user");
        if (manager == null || !"Project Manager".equals(manager.getRole())) {
            return "redirect:/login";
        }

        Long ticketId     = dto.getTicketId();
        Long technicianId = dto.getTechnicianId();

        Ticket ticket = ticketService.get(ticketId);
        User tech     = userService.getUserById(technicianId);

        // Assign and advance stage:
        ticket.setAssignedToUserId(technicianId);
        ticket.setStage(TicketStage.ASSIGNED);
        ticketService.save(ticket);

        redirectAttributes.addFlashAttribute(
                "message",
                "Ticket #" + ticket.getId() + " assigned to “" + tech.getUsername() + "”"
        );
        return "redirect:/manager/tasks";
    }
}
