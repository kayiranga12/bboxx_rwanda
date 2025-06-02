package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.MaintenanceSchedule;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.CustomerService;
import com.bboxxtrack.Service.EmailService;
import com.bboxxtrack.Service.MaintenanceScheduleService;
import com.bboxxtrack.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/support")
public class MaintenanceScheduleController {

    private static final Logger logger = Logger.getLogger(MaintenanceScheduleController.class.getName());
    private static final int PAGE_SIZE = 10;
    private static final String UPLOAD_DIR = "uploads/";

    private final MaintenanceScheduleService scheduleService;
    private final CustomerService customerService;
    private final EmailService emailService;
    private final UserService userService;

    @Autowired
    public MaintenanceScheduleController(MaintenanceScheduleService scheduleService,
                                         CustomerService customerService,
                                         EmailService emailService,
                                         UserService userService) {
        this.scheduleService = scheduleService;
        this.customerService = customerService;
        this.emailService = emailService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(
            HttpSession session,
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "") String status,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "") String search) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Support".equals(user.getRole())) {
            return "redirect:/login";
        }

        List<MaintenanceSchedule> all = scheduleService.getAllSchedules();
        List<MaintenanceSchedule> filtered = all.stream()
                .filter(s -> status.isEmpty() || s.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        if (!search.isBlank()) {
            String q = search.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(s -> String.valueOf(s.getCustomer().getId()).contains(q) ||
                            s.getPurpose().toLowerCase().contains(q))
                    .collect(Collectors.toList());
        }

        Comparator<MaintenanceSchedule> comparator =
                Comparator.comparing(MaintenanceSchedule::getScheduleDate);
        if ("latest".equals(sort)) {
            comparator = comparator.reversed();
        } else if ("customerId".equals(sort)) {
            comparator = Comparator.comparing(s -> s.getCustomer().getId());
        } else if ("oldest".equals(sort)) {
            // Already sorted by scheduleDate ascending
        }
        filtered.sort(comparator);

        int total = filtered.size();
        int totalPages = (total + PAGE_SIZE - 1) / PAGE_SIZE;
        int from = page * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, total);
        List<MaintenanceSchedule> pageList = filtered.subList(Math.min(from, to), to);

        long completedCount = all.stream()
                .filter(s -> "Completed".equalsIgnoreCase(s.getStatus()))
                .count();
        long pendingCount = all.stream()
                .filter(s -> "Pending".equalsIgnoreCase(s.getStatus()) || "In Progress".equalsIgnoreCase(s.getStatus()))
                .count();

        model.addAttribute("schedules", pageList);
        model.addAttribute("total", total);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("filterStatus", status);
        model.addAttribute("sortOrder", sort);
        model.addAttribute("searchQuery", search);
        model.addAttribute("technicians", userService.getUsersByRole("Technician"));

        return "support/dashboard";
    }

    @GetMapping("/schedules/add")
    public String showForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Support".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("newSchedule", new MaintenanceScheduleForm());
        model.addAttribute("customers", customerService.getAllCustomers());
        return "support/schedule_form";
    }

    @PostMapping("/schedules/add")
    public String scheduleMaintenance(
            @Valid @ModelAttribute("newSchedule") MaintenanceScheduleForm form,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Support".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Please log in as a Support user");
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            logger.warning("Form validation errors: " + result.getAllErrors());
            redirectAttributes.addFlashAttribute("error", "Please correct the form errors");
            return "redirect:/support/schedules/add";
        }

        try {
            MaintenanceSchedule schedule = new MaintenanceSchedule();
            schedule.setCustomer(customerService.getCustomerById(form.getCustomerId()));
            schedule.setScheduleDate(form.getScheduleDate());
            schedule.setPurpose(form.getPurpose());
            schedule.setFollowUp(form.isFollowUp());
            schedule.setStatus("Scheduled");

            logger.info("Saving schedule for customer ID: " + form.getCustomerId());
            scheduleService.saveSchedule(schedule);

            String email = schedule.getCustomer().getEmail();
            if (email != null && !email.isBlank()) {
                String body = "Dear " + schedule.getCustomer().getFullName() + ",\n\n" +
                        "Your maintenance visit is scheduled for " + schedule.getScheduleDate() + ".\n" +
                        "Purpose: " + schedule.getPurpose() + "\n\n" +
                        "Thank you,\nBBOXXTrack Team";
                emailService.sendEmail(email, "Maintenance Visit Scheduled", body);
                logger.info("Sent email to: " + email);
            } else {
                logger.warning("No email available for customer ID: " + form.getCustomerId() + "; skipping email");
            }

            redirectAttributes.addFlashAttribute("message", "Maintenance visit scheduled successfully");
        } catch (Exception e) {
            logger.severe("Error saving schedule: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to schedule maintenance: " + e.getMessage());
            return "redirect:/support/schedules/add";
        }

        return "redirect:/support/dashboard";
    }

    @PostMapping("/schedules/complete")
    public String complete(
            @RequestParam Long id,
            @RequestParam String visitType,
            @RequestParam(required = false) List<String> actions,
            @RequestParam String reportNotes,
            @RequestParam(required = false) MultipartFile photoEvidence,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Support".equals(user.getRole())) {
            redirectAttributes.addFlashAttribute("error", "Please log in as a Support user");
            return "redirect:/login";
        }

        try {
            MaintenanceSchedule s = scheduleService.getScheduleById(id);
            if (s != null) {
                s.setStatus("Completed");
                s.setCompletedBy(user);
                s.setCompletedAt(LocalDateTime.now());
                s.setVisitType(visitType);
                s.setActionsPerformed(actions != null ? String.join(",", actions) : "");
                s.setReportNotes(reportNotes);

                if (photoEvidence != null && !photoEvidence.isEmpty()) {
                    try {
                        String fileName = System.currentTimeMillis() + "_" + photoEvidence.getOriginalFilename();
                        java.nio.file.Path uploadPath = Paths.get(UPLOAD_DIR);
                        if (!Files.exists(uploadPath)) {
                            Files.createDirectories(uploadPath);
                        }
                        Path filePath = uploadPath.resolve(fileName);
                        photoEvidence.transferTo(filePath);
                        s.setPhotoEvidencePath(UPLOAD_DIR + fileName);
                        logger.info("Saved photo evidence: " + s.getPhotoEvidencePath());
                    } catch (IOException e) {
                        logger.severe("Failed to save photo evidence: " + e.getMessage());
                        redirectAttributes.addFlashAttribute("error", "Failed to upload photo: " + e.getMessage());
                    }
                }

                scheduleService.saveSchedule(s);

                String msg = String.format(
                        "Maintenance completed for Customer ID %d by %s on %s\nVisit Type: %s\nActions: %s\nReport: %s",
                        s.getCustomer().getId(),
                        user.getUsername(),
                        s.getCompletedAt(),
                        s.getVisitType(),
                        s.getActionsPerformed(),
                        reportNotes.replace(",", ";"));
                emailService.sendEmail("admin@bboxxtrack.com", "Maintenance Completed", msg);
                redirectAttributes.addFlashAttribute("message", "Maintenance marked as completed");
            } else {
                redirectAttributes.addFlashAttribute("error", "Schedule not found");
            }
        } catch (Exception e) {
            logger.severe("Error completing schedule ID " + id + ": " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Failed to complete maintenance: " + e.getMessage());
        }

        return "redirect:/support/dashboard";
    }

    @GetMapping("/schedules/export")
    public void exportCompleted(HttpServletResponse response, HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Support".equals(user.getRole())) {
            response.sendRedirect("/login");
            return;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"completed_maintenance.csv\"");
        PrintWriter w = response.getWriter();

        w.println("Customer ID,Date,Purpose,Visit Type,Actions Performed,Completed By,Completed At,Report Notes");
        scheduleService.getAllSchedules().stream()
                .filter(s -> "Completed".equalsIgnoreCase(s.getStatus()))
                .forEach(s -> {
                    w.printf("%d,%s,%s,%s,%s,%s,%s,%s\n",
                            s.getCustomer().getId(),
                            s.getScheduleDate(),
                            s.getPurpose().replace(",", ";"),
                            s.getVisitType() != null ? s.getVisitType().replace(",", ";") : "",
                            s.getActionsPerformed() != null ? s.getActionsPerformed().replace(",", ";") : "",
                            s.getCompletedBy() != null ? s.getCompletedBy().getUsername() : "",
                            s.getCompletedAt(),
                            s.getReportNotes() != null ? s.getReportNotes().replace(",", ";") : "");
                });
        w.flush();
        w.close();
    }
}