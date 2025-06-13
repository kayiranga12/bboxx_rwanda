package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Ticket;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.DocumentService;
import com.bboxxtrack.Service.TicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/technician")
public class TechnicianTicketsController {

    @Autowired
    private DocumentService documentService;
    @Autowired private TicketService ticketService;

    @GetMapping("/tickets")
    public String showTickets(HttpSession session, Model model,
                            @RequestParam(required = false) String status,
                            @RequestParam(required = false) String q) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Technician".equals(user.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("filterStatus", status);
        model.addAttribute("searchQuery", q);

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


        model.addAttribute("statusLabels", List.of("Done", "In Progress", "Assigned"));

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

        return "technician/tickets";
    }

}
