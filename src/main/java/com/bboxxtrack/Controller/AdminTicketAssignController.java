//package com.bboxxtrack.Controller;
//
//import com.bboxxtrack.Model.Ticket;
//import com.bboxxtrack.Model.TicketAssignmentDto;
//import com.bboxxtrack.Model.TicketStage;
//import com.bboxxtrack.Model.User;
//import com.bboxxtrack.Service.TicketService;
//import com.bboxxtrack.Service.UserService;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.validation.BindingResult;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import jakarta.validation.Valid;
//import java.util.List;
//
//@Controller
//@RequestMapping("/admin")
//public class AdminTicketAssignController {
//
//    @Autowired private TicketService ticketService;
//    @Autowired private UserService userService;
//
//    /**
//     * Shows the same “/admin/tasks” page as before, but now adds:
//     *  - unassignedTickets: all tickets whose assignedToUserId is still null
//     *  - technicians: all users with role “Technician”
//     *  - ticketAssignment: a fresh DTO for the form
//     *
//     * You still need to include any existing model attributes your “tasks” page expects
//     * (e.g. tasks, projects, etc.). Below we just add the new ones.
//     */
//    @GetMapping("/tasks")
//    public String showTasksAndAssignments(HttpSession session, Model model) {
//        User user = (User) session.getAttribute("user");
//        if (user == null || !"Admin".equals(user.getRole())) {
//            return "redirect:/login";
//        }
//
//        // … your existing code to populate "tasks", "projects", etc. …
//
//        // 1) Fetch all tickets where assignedToUserId is null (i.e. unassigned)
//        List<Ticket> unassignedTickets = ticketService.findAllByAssignedToUserIdIsNull();
//
//        // 2) Fetch all users whose role = “Technician”
//        List<User> technicians = userService.getUsersByRole("Technician");
//
//        model.addAttribute("unassignedTickets", unassignedTickets);
//        model.addAttribute("technicians", technicians);
//        model.addAttribute("ticketAssignment", new TicketAssignmentDto());
//
//        return "admin/tasks";
//    }
//
//    /**
//     * Handles the POST from the “Assign Ticket to Technician” form.
//     */
//    @PostMapping("/tickets/assign")
//    public String assignTicketToTechnician(
//            @Valid @ModelAttribute("ticketAssignment") TicketAssignmentDto dto,
//            BindingResult bindingResult,
//            HttpSession session,
//            RedirectAttributes redirectAttributes,
//            Model model) {
//
//        User user = (User) session.getAttribute("user");
//        if (user == null || !"Admin".equals(user.getRole())) {
//            return "redirect:/login";
//        }
//
//        if (bindingResult.hasErrors()) {
//            // If there are errors, re‐populate unassignedTickets & technicians and show the same page again:
//            List<Ticket> unassignedTickets = ticketService.findAllByAssignedToUserIdIsNull();
//            List<User> technicians = userService.getUsersByRole("Technician");
//            model.addAttribute("unassignedTickets", unassignedTickets);
//            model.addAttribute("technicians", technicians);
//            // Also remember to re‐add any other attributes your page expects (tasks, projects, etc.)
//            return "admin/tasks";
//        }
//
//        Long ticketId     = dto.getTicketId();
//        Long technicianId = dto.getTechnicianId();
//
//        // 3) Retrieve the Ticket using your existing get(...)
//        Ticket ticket = ticketService.get(ticketId);
//        // 4) Retrieve the User object for the technician
//        User tech = userService.getUserById(technicianId);
//
//        if (ticket == null) {
//            redirectAttributes.addFlashAttribute("error", "Selected ticket not found.");
//            return "redirect:/admin/tasks";
//        }
//        if (tech == null || !"Technician".equals(tech.getRole())) {
//            redirectAttributes.addFlashAttribute("error", "Selected user is not a technician.");
//            return "redirect:/admin/tasks";
//        }
//
//        // 5) Assign the technician by ID (your Ticket has a Long assignedToUserId field)
//        ticket.setAssignedToUserId(technicianId);
//
//        // 6) Advance the ticket’s stage (you said you track TicketStage instead of status)
//        ticket.setStage(TicketStage.ASSIGNED_TO_TECH);
//
//        ticketService.save(ticket);
//
//        // 7) Optionally, send an email to tech.getEmail() about the assignment…
//
//        redirectAttributes.addFlashAttribute(
//                "message",
//                "Ticket #" + ticket.getId() + " has been assigned to technician " + tech.getUsername()
//        );
//        return "redirect:/admin/tasks";
//    }
//}
