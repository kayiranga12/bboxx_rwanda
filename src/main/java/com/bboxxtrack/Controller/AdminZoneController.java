package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Zone;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.ZoneService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/zones")
public class AdminZoneController {

    @Autowired private ZoneService zoneService;

    @GetMapping
    public String listZones(Model model, HttpSession session) {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"Admin".equals(admin.getRole())) {
            return "redirect:/login";
        }
        List<Zone> zones = zoneService.getAllZones();
        model.addAttribute("zones", zones);
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
