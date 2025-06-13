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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/tickets")
public class AdminTicketController {

    @Autowired private UserService userService;
    @Autowired private ProjectService projectService;
    @Autowired private TicketService ticketService;


    @GetMapping
    public String showTickets(
            HttpSession session,
            Model model
    ) {
        // — Authentication check —
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

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

        List<Ticket> assignedTickets = ticketService.all().stream()
                .filter(t -> t.getAssignedToUserId() != null)
                .collect(Collectors.toList());

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
        model.addAttribute("ticketAssignment", new com.bboxxtrack.Model.TicketAssignmentDto());

        model.addAttribute("assignedTickets", assignedTickets);

        return "admin/tickets";
    }


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


        Long ticketId     = dto.getTicketId();
        Long technicianId = dto.getTechnicianId();

        Ticket ticket = ticketService.get(ticketId);
        User tech     = userService.getUserById(technicianId);

        if (ticket == null) {
            redirectAttributes.addFlashAttribute("error", "Selected ticket not found.");
            return "redirect:/admin/tickets";
        }
        if (tech == null || !"Technician".equals(tech.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Selected user is not a technician.");
            return "redirect:/admin/tickets";
        }

        // Assign the technician’s ID
        ticket.setAssignedToUserId(technicianId);

        // Advance the stage
        ticket.setStage(TicketStage.CREATED);

        ticketService.save(ticket);

        redirectAttributes.addFlashAttribute(
                "message",
                "Ticket #" + ticket.getId() + " has been assigned to “" + tech.getUsername() + "”"
        );
        return "redirect:/admin/tickets";
    }
}
