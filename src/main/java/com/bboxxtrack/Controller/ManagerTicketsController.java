package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.*;
import com.bboxxtrack.Model.TicketAssignmentDto;
import com.bboxxtrack.Service.ProjectService;
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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/manager/tickets")
public class ManagerTicketsController {

    @Autowired private ProjectService projectService;
    @Autowired private UserService userService;
    @Autowired private TicketService ticketService; // ← make sure this is injected

    @GetMapping
    public String showTickets(Model model, HttpSession session) {
        User manager = (User) session.getAttribute("user");
        if (manager == null || !"Project Manager".equals(manager.getRole())) {
            return "redirect:/login";
        }

        // 1) Load all projects that this manager owns
        List<Project> projects = projectService.getProjectsByManager(manager.getId());

        // 2) Load all users whose role = “Technician” (exact role string must match your DB)
        List<User> techs = userService.getUsersByRole("Technician");


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
        model.addAttribute("projectNames",      projectNames);
        model.addAttribute("techNames",         techNames);

        // Now add exactly these two so Thymeleaf can bind them:
        model.addAttribute("unassignedTickets", unassignedTickets);
        model.addAttribute("assignedTickets", assignedTickets);
        model.addAttribute("ticketAssignment",  new com.bboxxtrack.Model.TicketAssignmentDto());

        return "manager/tickets";
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
        return "redirect:/manager/tickets";
    }
}
