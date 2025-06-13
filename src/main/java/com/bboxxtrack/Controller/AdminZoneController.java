package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Tracker;
import com.bboxxtrack.Model.Zone;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.TrackerService;
import com.bboxxtrack.Service.ZoneService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/zones")
public class AdminZoneController {

    @Autowired private ZoneService zoneService;
    @Autowired
    private TrackerService trackerService;

    @GetMapping
    public String listZones(Model model, HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"Admin".equals(admin.getRole())) {
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
        return "admin/zones";
    }

    @PostMapping("/add")
    public String addZone(@RequestParam String name,
                          @RequestParam String polygonGeoJson) {
        Zone z = new Zone();
        z.setName(name);
        z.setPolygonGeoJson(polygonGeoJson);
        zoneService.saveZone(z);
        return "redirect:/admin/zones";
    }
}
