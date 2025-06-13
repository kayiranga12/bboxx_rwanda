package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Tracker;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.TicketService;
import com.bboxxtrack.Service.TrackerService;
import com.bboxxtrack.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/zones")
public class Zones {

    @Autowired
    private TrackerService trackerService;
    @Autowired private TicketService ticketService;

    @GetMapping("/manager")
    public String viewZonesManager(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Project Manager".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Tracker> trackers = trackerService.getAll();
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
        return "manager/zones";
    }

    @GetMapping("/support")
    public String viewZonesSupport(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Support".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<Tracker> trackers = trackerService.getAll();
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
        return "support/zones";
    }
}
