package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Tracker;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.EmailService;
import com.bboxxtrack.Service.SmsService;
import com.bboxxtrack.Service.TrackerService;
import com.bboxxtrack.Service.ZoneService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/technician")
public class TechnicianTrackerController {

    @Autowired
    private TrackerService trackerService;
    @Autowired private ZoneService zoneService;
    @Autowired private EmailService emailService;
    @Autowired private SmsService smsService;

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

    @PostMapping("/tracker/add")
    public String addTracker(@ModelAttribute Tracker tracker, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Technician".equals(user.getRole())) {
            return "redirect:/login";
        }
        tracker.setUserId(user.getId());
        trackerService.save(tracker);

        // geofence check
        double lat = Double.parseDouble(tracker.getGpsCoordinates().split(",")[0].trim());
        double lng = Double.parseDouble(tracker.getGpsCoordinates().split(",")[1].trim());
        boolean inside = (zoneService.findZoneContaining(lat, lng) != null);

        if (!inside) {
            String msg = "Alert: Technician " + user.getFullName() +
                    " reported from outside any zone at " + tracker.getGpsCoordinates();
            // email HQ
            emailService.sendEmail("hq@bboxxtrack.com",
                    "Geofence breach by " + user.getFullName(),
                    msg);
            // SMS HQ
            smsService.sendSms("+2507XXXXXXXX", msg);
        }

        return "redirect:/technician/tracker";
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
