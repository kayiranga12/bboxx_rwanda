package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.*;
import com.bboxxtrack.Service.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/technician")
public class TechnicianTrackerController {

    @Autowired
    private TrackerService trackerService;
    @Autowired private TicketService ticketService;


    @GetMapping("/tracker")
    public String viewTracker(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Technician".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Tracker> trackers = trackerService.getByUser(user.getId());
        List<Map<String,Object>> points = trackers.stream().map(t -> {
            String[] parts = t.getGpsCoordinates().split(",");
            Map<String,Object> m = new HashMap<>();
            m.put("lat",  Double.parseDouble(parts[0].trim()));
            m.put("lng",  Double.parseDouble(parts[1].trim()));
            m.put("status", t.getStatusUpdate());
            m.put("time", t.getTimestamp().toString());
            return m;
        }).collect(Collectors.toList());

        model.addAttribute("trackers", trackers);
        model.addAttribute("newTracker", new Tracker());
        model.addAttribute("points", points);
        return "technician/tracker";
    }

    @PostMapping("/tracker")
    public String saveTracker(@RequestParam String gpsCoordinates,
                              @RequestParam String statusUpdate,
                              @RequestParam Long ticketId,
                              @RequestParam String statusType,
                              HttpSession session) {

        User technician = (User) session.getAttribute("user");
        if (technician == null || !"Technician".equals(technician.getRole())) {
            return "redirect:/login";
        }

        // Create new Tracker object
        Tracker tracker = new Tracker();

        // Set basic fields
        tracker.setUserId(technician.getId());
        tracker.setGpsCoordinates(gpsCoordinates);

        // Combine status type and description for better tracking
        String combinedStatus = statusType + ": " + statusUpdate;
        tracker.setStatusUpdate(combinedStatus);

        // Get the ticket
        Ticket ticket = ticketService.findById(ticketId);
        if (ticket == null) {
            return "technician/tracker";
        }
        tracker.setTicket(ticket);

        // Update ticket stage and closedAt based on status type
        updateTicketBasedOnStatus(ticket, statusType);

        // Save the tracker
        trackerService.save(tracker);

        // Save the updated ticket
        ticketService.save(ticket);

        return "technician/tracker";
    }


    private void updateTicketBasedOnStatus(Ticket ticket, String statusType) {
        switch (statusType.toLowerCase()) {
            case "completed":
                // Task completed - set stage to COMPLETED and set closedAt
                ticket.setStage(TicketStage.COMPLETED);
                ticket.setClosedAt(LocalDateTime.now());
                break;

            case "arrived":
                // Technician arrived at site - set to IN_PROGRESS
                ticket.setStage(TicketStage.IN_PROGRESS);
                break;

            case "installation":
                // Installation in progress
                ticket.setStage(TicketStage.IN_PROGRESS);
                break;

            case "testing":
                // Testing equipment
                ticket.setStage(TicketStage.IN_PROGRESS);
                break;

            case "issue":
                ticket.setStage(TicketStage.IN_PROGRESS);
                break;

            case "break":
                if (ticket.getStage() == TicketStage.ASSIGNED) {
                    ticket.setStage(TicketStage.IN_PROGRESS);
                }
                break;

            case "travelling":
                if (ticket.getStage() == TicketStage.ASSIGNED) {
                    ticket.setStage(TicketStage.IN_PROGRESS);
                }
                break;

            default:
                // For any other status, set to IN_PROGRESS if currently ASSIGNED
                if (ticket.getStage() == TicketStage.ASSIGNED) {
                    ticket.setStage(TicketStage.IN_PROGRESS);
                }
                break;
        }
    }


    @GetMapping("/tracker/export")
    public void exportCsv(HttpServletResponse response, HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Technician".equals(user.getRole())) {
            response.sendRedirect("/login");
            return;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition","attachment; filename=tracker_history.csv");

        List<Tracker> trackers = trackerService.getByUser(user.getId());
        try (PrintWriter w = response.getWriter()) {
            w.println("GPS Coordinates,Status Update,Timestamp");
            for (Tracker t : trackers) {
                w.printf("\"%s\",\"%s\",\"%s\"%n",
                        t.getGpsCoordinates(),
                        t.getStatusUpdate().replace("\"","\"\""),
                        t.getTimestamp().toString()
                );
            }
        }
    }
}
