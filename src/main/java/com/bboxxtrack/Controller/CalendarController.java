package com.bboxxtrack.Controller;

import com.bboxxtrack.Dto.CalendarEvent;
import com.bboxxtrack.Model.MaintenanceSchedule;
import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.MaintenanceScheduleService;
import com.bboxxtrack.Service.ProjectService;
import com.bboxxtrack.Service.TaskService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/support")
public class CalendarController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final MaintenanceScheduleService scheduleService;

    @Autowired
    public CalendarController(TaskService taskService,
                              ProjectService projectService,
                              MaintenanceScheduleService scheduleService) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.scheduleService = scheduleService;
    }

    @GetMapping("/calendar")
    public String calendarView(HttpSession session, Model model) {
        User u = (User) session.getAttribute("user");
        if (u == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", u);
        return "support/calendar";
    }

    @GetMapping("/calendar/api/events")
    @ResponseBody
    public List<CalendarEvent> fetchEvents() {
        List<CalendarEvent> events = new ArrayList<>();
        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        for (Task t : taskService.getAllTasks()) {
            CalendarEvent e = new CalendarEvent();
            e.setId(t.getId());
            e.setType("task");
            e.setTitle("Task: " + t.getTaskName());
            e.setStart(t.getDueDate().atStartOfDay());
            e.setEnd(t.getDueDate().atStartOfDay().plusHours(1));
            e.setUrl("/admin/tasks");
            events.add(e);
        }

        for (Project p : projectService.getAllProjects()) {
            CalendarEvent e = new CalendarEvent();
            e.setId(p.getId());
            e.setType("project");
            e.setTitle("Project: " + p.getProjectTitle());
            e.setStart(p.getStartDate() != null ? p.getStartDate().atStartOfDay() : null);
            e.setEnd(p.getEndDate() != null ? p.getEndDate().atStartOfDay() : null);
            e.setUrl("/admin/projects");
            events.add(e);
        }

        for (MaintenanceSchedule s : scheduleService.getAllSchedules()) {
            CalendarEvent e = new CalendarEvent();
            e.setId(s.getId());
            e.setType("visit");
            e.setTitle("Visit #" + s.getId());
            e.setStart(s.getScheduleDate().atStartOfDay());
            e.setEnd(s.getScheduleDate().atStartOfDay().plusHours(2));
            e.setUrl("/support/schedules");
            events.add(e);
        }

        return events;
    }

    @PostMapping("/calendar/api/events/{type}/{id}")
    @ResponseBody
    public String moveEvent(@PathVariable String type,
                            @PathVariable Long id,
                            @RequestParam String start,
                            @RequestParam String end) {
        switch (type) {
            case "task":
                Task t = taskService.getTaskById(id);
                t.setDueDate(java.time.LocalDate.parse(start.substring(0, 10)));
                taskService.saveTask(t);
                break;
            case "project":
                Project p = projectService.getById(id);
                p.setStartDate(java.time.LocalDate.parse(start.substring(0, 10)));
                p.setEndDate(java.time.LocalDate.parse(end.substring(0, 10)));
                projectService.saveProject(p);
                break;
            case "visit":
                MaintenanceSchedule v = scheduleService.getScheduleById(id);
                v.setScheduleDate(java.time.LocalDate.parse(start.substring(0, 10)));
                scheduleService.saveSchedule(v);
                break;
        }
        return "ok";
    }

    @GetMapping("/calendar/calendar.ics")
    public void icsFeed(HttpServletResponse resp) throws Exception {
        resp.setHeader("Content-Disposition", "attachment; filename=bboxxtrack.ics");
        PrintWriter w = resp.getWriter();
        w.println("BEGIN:VCALENDAR");
        w.println("VERSION:2.0");
        w.println("PRODID:-//BBOXXTrack//EN");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        fetchEvents().forEach(e -> {
            if (e.getStart() != null && e.getEnd() != null) {
                w.println("BEGIN:VEVENT");
                w.println("UID:" + e.getType() + "-" + e.getId() + "@bboxxtrack.com");
                w.println("DTSTAMP:" + dtf.format(java.time.LocalDateTime.now()));
                w.println("DTSTART:" + dtf.format(e.getStart()));
                w.println("DTEND:" + dtf.format(e.getEnd()));
                w.println("SUMMARY:" + e.getTitle());
                w.println("DESCRIPTION:via BBOXXTrack");
                w.println("URL;VALUE=URI:" + e.getUrl());
                w.println("END:VEVENT");
            }
        });
        w.println("END:VCALENDAR");
    }
}