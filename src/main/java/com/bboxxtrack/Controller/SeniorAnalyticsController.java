// src/main/java/com/bboxxtrack/Controller/SeniorAnalyticsController.java
package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.MaintenanceSchedule;
import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.MaintenanceScheduleService;
import com.bboxxtrack.Service.ProjectService;
import com.bboxxtrack.Service.TaskService;
import com.bboxxtrack.Service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.PrintWriter;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/senior/analytics")
public class SeniorAnalyticsController {

    @Autowired private UserService userService;
    @Autowired private ProjectService projectService;
    @Autowired private TaskService taskService;
    @Autowired private MaintenanceScheduleService scheduleService;

    private static final int MONTHS_BACK = 12;

    @GetMapping
    public String viewAnalytics(Model model, HttpSession session) {
        User senior = (User) session.getAttribute("user");
        if (senior == null || !"Senior Management".equals(senior.getRole())) {
            return "redirect:/login";
        }

        // 1) build last 12 months
        List<YearMonth> months = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = MONTHS_BACK - 1; i >= 0; --i) months.add(now.minusMonths(i));

        List<String> monthLabels = months.stream()
                .map(ym -> ym.getMonth()
                        .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                        + " " + ym.getYear())
                .collect(Collectors.toList());

        // 2) fetch data
        List<Project> projects = projectService.getAllProjects();
        List<Task> tasks       = taskService.getAllTasks();
        List<MaintenanceSchedule> visits = scheduleService.getAllSchedules();

        // 3) group counts by YearMonth
        Map<YearMonth, Long> projPerMonth = projects.stream()
                .map(p -> YearMonth.from(p.getStartDate()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<YearMonth, Long> taskPerMonth = tasks.stream()
                .map(t -> YearMonth.from(t.getDueDate()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<YearMonth, Long> visitPerMonth = visits.stream()
                .filter(v -> "Completed".equalsIgnoreCase(v.getStatus()))
                .map(v -> YearMonth.from(v.getCompletedAt().toLocalDate()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 4) align into lists
        List<Long> projData  = months.stream()
                .map(m -> projPerMonth.getOrDefault(m, 0L))
                .toList();

        List<Long> taskData  = months.stream()
                .map(m -> taskPerMonth.getOrDefault(m, 0L))
                .toList();

        List<Long> visitData = months.stream()
                .map(m -> visitPerMonth.getOrDefault(m, 0L))
                .toList();

        // summary totals
        model.addAttribute("totalProjects", projects.size());
        model.addAttribute("totalTasks",    tasks.size());
        model.addAttribute("totalVisits",   visits.size());

        model.addAttribute("monthLabels", monthLabels);
        model.addAttribute("projData",     projData);
        model.addAttribute("taskData",     taskData);
        model.addAttribute("visitData",    visitData);

        return "senior/analytics";
    }

    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response, HttpSession session) throws Exception {
        User senior = (User) session.getAttribute("user");
        if (senior == null || !"Senior Management".equals(senior.getRole())) {
            response.sendRedirect("/login");
            return;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"analytics_trends.csv\"");

        // reuse same logic to build months & maps
        List<YearMonth> months = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = MONTHS_BACK - 1; i >= 0; --i) months.add(now.minusMonths(i));

        Map<YearMonth, Long> projMap = projectService.getAllProjects().stream()
                .map(p -> YearMonth.from(p.getStartDate()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<YearMonth, Long> taskMap = taskService.getAllTasks().stream()
                .map(t -> YearMonth.from(t.getDueDate()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        Map<YearMonth, Long> visitMap = scheduleService.getAllSchedules().stream()
                .filter(v -> "Completed".equalsIgnoreCase(v.getStatus()))
                .map(v -> YearMonth.from(v.getCompletedAt().toLocalDate()))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        try (PrintWriter w = response.getWriter()) {
            w.println("Month,Projects,Tasks,Visits Completed");
            for (YearMonth ym : months) {
                String label = ym.getMonth()
                        .getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                        + " " + ym.getYear();
                long p = projMap .getOrDefault(ym, 0L);
                long t = taskMap.getOrDefault(ym, 0L);
                long v = visitMap.getOrDefault(ym, 0L);
                w.printf("%s,%d,%d,%d%n", label, p, t, v);
            }
        }
    }
}
