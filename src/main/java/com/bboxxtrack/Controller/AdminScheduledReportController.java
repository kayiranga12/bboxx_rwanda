// src/main/java/com/bboxxtrack/Controller/AdminScheduledReportController.java
package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.ScheduledReport;
import com.bboxxtrack.Repository.ScheduledReportRepository;
import com.bboxxtrack.Model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/admin/scheduled-reports")
public class AdminScheduledReportController {

    @Autowired private ScheduledReportRepository repo;

    @GetMapping
    public String list(Model m, HttpSession s) {
        User u = (User) s.getAttribute("user");
        if (u==null || !"Admin".equals(u.getRole())) return "redirect:/login";
        List<ScheduledReport> all = repo.findAll();
        m.addAttribute("reports", all);
        m.addAttribute("newReport", new ScheduledReport());
        return "admin/scheduled_reports";
    }

    @PostMapping("/add")
    public String add(@ModelAttribute ScheduledReport rpt) {
        repo.save(rpt);
        return "redirect:/admin/scheduled-reports";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Long id) {
        repo.findById(id).ifPresent(r -> {
            r.setEnabled(!r.isEnabled());
            repo.save(r);
        });
        return "redirect:/admin/scheduled-reports";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/admin/scheduled-reports";
    }
}
