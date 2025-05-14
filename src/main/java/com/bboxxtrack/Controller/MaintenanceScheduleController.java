package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Customer;
import com.bboxxtrack.Model.MaintenanceSchedule;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.CustomerService;
import com.bboxxtrack.Service.EmailService;
import com.bboxxtrack.Service.MaintenanceScheduleService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/support")
public class MaintenanceScheduleController {

    private final MaintenanceScheduleService scheduleService;
    private final CustomerService customerService;
    private final EmailService emailService;

    @Autowired
    public MaintenanceScheduleController(MaintenanceScheduleService scheduleService,
                                         CustomerService customerService,
                                         EmailService emailService) {
        this.scheduleService = scheduleService;
        this.customerService = customerService;
        this.emailService = emailService;
    }

    /** Dashboard listing + stats **/
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null || !"Support".equals(user.getRole())){
            return "redirect:/login";
        }

        List<MaintenanceSchedule> schedules = scheduleService.getAllSchedules();
        long total        = schedules.size();
        long completed    = schedules.stream().filter(s -> "Completed".equalsIgnoreCase(s.getStatus())).count();
        long pending      = total - completed;

        model.addAttribute("schedules", schedules);
        model.addAttribute("total", total);
        model.addAttribute("completedCount", completed);
        model.addAttribute("pendingCount", pending);
        return "support/dashboard";
    }

    /** Show “new schedule” form **/
    @GetMapping("/schedules/add")
    public String showForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if(user == null || !"Support".equals(user.getRole())){
            return "redirect:/login";
        }
        model.addAttribute("newSchedule", new MaintenanceSchedule());
        model.addAttribute("customers", customerService.getAllCustomers());
        return "support/schedule_form";
    }

    /** Handle form POST **/
    @PostMapping("/schedules/add")
    public String scheduleMaintenance(@ModelAttribute("newSchedule") MaintenanceSchedule schedule,
                                      HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null || !"Support".equals(user.getRole())){
            return "redirect:/login";
        }
        schedule.setStatus("Scheduled");
        scheduleService.saveSchedule(schedule);

        // Notify customer (lookup real email in a real app)
        String body = "Dear Customer, your maintenance visit is scheduled on "
                + schedule.getScheduleDate();
        emailService.sendEmail(
                "customer@example.com",
                "Maintenance Visit Scheduled",
                body
        );

        return "redirect:/support/dashboard";
    }

    /** Mark complete + send HQ email **/
    @PostMapping("/schedules/complete")
    public String complete(@RequestParam Long id,
                           @RequestParam String reportNotes,
                           HttpSession session) {
        User user = (User) session.getAttribute("user");
        if(user == null || !"Support".equals(user.getRole())){
            return "redirect:/login";
        }
        MaintenanceSchedule s = scheduleService.getScheduleById(id);
        if(s != null){
            s.setStatus("Completed");
            s.setCompletedBy(user.getFullName());
            s.setCompletedAt(LocalDateTime.now());
            s.setReportNotes(reportNotes);
            scheduleService.saveSchedule(s);

            String msg = String.format(
                    "Maintenance completed for Customer ID %d by %s on %s%nReport: %s",
                    s.getCustomerId(),
                    user.getFullName(),
                    s.getCompletedAt(),
                    reportNotes.replace(",", ";")
            );
            emailService.sendEmail("admin@bboxxtrack.com", "Maintenance Completed", msg);
        }
        return "redirect:/support/dashboard";
    }

    /** Export completed visits as CSV **/
    @GetMapping("/schedules/export")
    public void exportCompleted(HttpServletResponse response, HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user");
        if(user == null || !"Support".equals(user.getRole())){
            response.sendRedirect("/login");
            return;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition","attachment; filename=\"completed_maintenance.csv\"");
        PrintWriter w = response.getWriter();
        w.println("Customer ID,Date,Purpose,Completed By,Completed At,Report Notes");

        scheduleService.getAllSchedules().stream()
                .filter(s -> "Completed".equalsIgnoreCase(s.getStatus()))
                .forEach(s -> {
                    w.printf("%d,%s,%s,%s,%s,%s%n",
                            s.getCustomerId(),
                            s.getScheduleDate(),
                            s.getPurpose().replace(",", ";"),
                            s.getCompletedBy(),
                            s.getCompletedAt(),
                            s.getReportNotes()==null ? "" : s.getReportNotes().replace(",", ";")
                    );
                });
        w.flush();
        w.close();
    }


}
