// src/main/java/com/bboxxtrack/Controller/SupportReportController.java
package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Ticket;
import com.bboxxtrack.Model.TicketStage;      // ← use TicketStage instead
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.SLAService;
import com.bboxxtrack.Service.TicketService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/support")
public class SupportReportController {

    private final SLAService slaService;
    private final TicketService ticketService;

    public SupportReportController(SLAService slaService,
                                   TicketService ticketService) {
        this.slaService = slaService;
        this.ticketService = ticketService;
    }

    @GetMapping("/reports")
    public String reports(Model model, HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (u == null || !"Support".equals(u.getRole())) {
            return "redirect:/login";
        }

        // core SLA stats
        model.addAttribute("openCount", slaService.getOpenCount());
        model.addAttribute("avgResp",   String.format("%.1f", slaService.getAvgResponseHours()));
        model.addAttribute("avgReso",   String.format("%.1f", slaService.getAvgResolutionHours()));
        model.addAttribute("breaches48", slaService.getBreachesOver48());

        // breakdown by **stage** (was status)
        LinkedHashMap<TicketStage, Long> stageCounts = ticketService.all().stream()
                .collect(Collectors.groupingBy(
                        Ticket::getStage,                     // ← use getStage()
                        LinkedHashMap::new,
                        Collectors.counting()
                ));

        model.addAttribute("stageLabels", stageCounts.keySet());
        model.addAttribute("stageData",   stageCounts.values());

        // mock 7‐day timeseries (in a real app, query DB)
        LocalDate today = LocalDate.now();
        List<String> days = today.minusDays(6).datesUntil(today.plusDays(1))
                .map(LocalDate::toString)
                .collect(Collectors.toList());
        model.addAttribute("days", days);

        // random example data—replace with real query if needed
        model.addAttribute("dailyResp", days.stream()
                .map(d -> Math.round(Math.random()*5 + 1))
                .collect(Collectors.toList()));
        model.addAttribute("dailyReso", days.stream()
                .map(d -> Math.round(Math.random()*8 + 2))
                .collect(Collectors.toList()));

        return "support/reports";
    }
}
