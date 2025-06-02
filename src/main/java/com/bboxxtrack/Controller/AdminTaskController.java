package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.*;
import com.bboxxtrack.Model.TicketAssignmentDto;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/tasks")
public class AdminTaskController {

    @Autowired private TaskService taskService;
    @Autowired private UserService userService;
    @Autowired private ProjectService projectService;
    @Autowired private TicketService ticketService;

    /**
     * GET /admin/tasks
     *
     *  - loads all existing “tasks” (for your normal AdminTask page),
     *  - plus “unassignedTickets” and “technicians” for the ticket‐assignment form,
     *  - and attaches a fresh TicketAssignmentDto.
     */
    @GetMapping
    public String showTasks(
            HttpSession session,
            Model model
    ) {
        // — Authentication check —
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        // — 1) Load existing Tasks / Technicians / Projects for the normal “Create/Edit Task” form: —
        List<Task>    tasks       = taskService.getAllTasks();
        List<User>    technicians = userService.getUsersByRole("Technician");
        List<Project> projects    = projectService.getAllProjects();

        // Map<Long,String> for display names in the task grid:
        Map<Long,String> techNames = technicians.stream()
                .filter(u -> u.getId() != null)
                .collect(Collectors.toMap(
                        User::getId,
                        u -> u.getUsername() == null ? "" : u.getUsername(),
                        (existing, replacement) -> existing
                ));
        Map<Long,String> projectNames = projects.stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(
                        Project::getId,
                        p -> p.getProjectTitle() == null ? "" : p.getProjectTitle(),
                        (existing, replacement) -> existing
                ));

        model.addAttribute("tasks",        tasks);
        model.addAttribute("technicians",  technicians);
        model.addAttribute("projects",     projects);
        model.addAttribute("techNames",    techNames);
        model.addAttribute("projectNames", projectNames);
        model.addAttribute("newTask",      new Task());

        // — 2) Load unassigned tickets + create an empty DTO for “Assign Ticket” form: —
        List<Ticket> unassignedTickets = ticketService.findAllByAssignedToUserIdIsNull();
        model.addAttribute("unassignedTickets", unassignedTickets);

        // Fetch same “technicians” list for assigning tickets:
        model.addAttribute("ticketTechs", technicians);

        // DTO that binds to the “Assign Ticket → Technician” form
        model.addAttribute("ticketAssignment", new TicketAssignmentDto());

        return "admin/tasks";
    }

    /**
     * POST /admin/tasks/add
     *
     * Handles “Create Task” as before.
     */
    @PostMapping("/add")
    public String addTask(
            @Valid @ModelAttribute("newTask") Task task,
            BindingResult bindingResult,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            // On validation error, reload everything into the same page:
            showTasks(session, model);
            return "admin/tasks";
        }

        task.setAssignedDate(LocalDate.now());
        taskService.saveTask(task);
        redirectAttributes.addFlashAttribute("message","Task created successfully");
        return "redirect:/admin/tasks";
    }

    /**
     * POST /admin/tasks/edit
     *
     * Handles “Edit Task” (same as you had).
     */
    @PostMapping("/edit")
    public String editTask(
            @Valid @ModelAttribute Task task,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("error","Invalid task data");
        } else {
            taskService.saveTask(task);
            redirectAttributes.addFlashAttribute("message","Task updated");
        }
        return "redirect:/admin/tasks";
    }

    /**
     * GET /admin/tasks/delete/{id}
     */
    @GetMapping("/delete/{id}")
    public String deleteTask(
            @PathVariable Long id,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }
        taskService.deleteTask(id);
        redirectAttributes.addFlashAttribute("message","Task deleted");
        return "redirect:/admin/tasks";
    }

    /**
     * POST /admin/tasks/assign-ticket
     *
     * This handles “Assign a Ticket → Technician”. We consume the TicketAssignmentDto,
     * validate it, then set assignedToUserId on the Ticket and advance its stage.
     */
    @PostMapping("/assign-ticket")
    public String assignTicketToTechnician(
            @Valid @ModelAttribute("ticketAssignment") TicketAssignmentDto dto,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            // On validation errors, re‐load the same “/admin/tasks” page:
            showTasks(session, model);
            return "admin/tasks";
        }

        Long ticketId     = dto.getTicketId();
        Long technicianId = dto.getTechnicianId();

        Ticket ticket = ticketService.get(ticketId);
        User tech     = userService.getUserById(technicianId);

        if (ticket == null) {
            redirectAttributes.addFlashAttribute("error", "Selected ticket not found.");
            return "redirect:/admin/tasks";
        }
        if (tech == null || !"Technician".equals(tech.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Selected user is not a technician.");
            return "redirect:/admin/tasks";
        }

        // Assign the technician’s ID
        ticket.setAssignedToUserId(technicianId);

        // Advance the stage
        ticket.setStage(TicketStage.ASSIGNED_TO_TECH);

        ticketService.save(ticket);

        redirectAttributes.addFlashAttribute(
                "message",
                "Ticket #" + ticket.getId() + " has been assigned to “" + tech.getUsername() + "”"
        );
        return "redirect:/admin/tasks";
    }
}
