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

    @Autowired private TaskService taskService;
    @Autowired private ProjectService projectService;
    @Autowired private MaintenanceScheduleService scheduleService;

    /** 1) Show the calendar page **/
    @GetMapping("/calendar")
    public String calendarView(HttpSession session, Model model) {
        User u = (User) session.getAttribute("user");
        if (u == null || !"Support".equals(u.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("user", u);
        return "support/calendar";
    }

    /** 2) Serve all events as JSON **/
    @GetMapping("/api/calendar/events")
    @ResponseBody
    public List<CalendarEvent> fetchEvents() {
        List<CalendarEvent> events = new ArrayList<>();
        DateTimeFormatter iso = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        // tasks
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
        // projects
        for (Project p : projectService.getAllProjects()) {
            CalendarEvent e = new CalendarEvent();
            e.setId(p.getId());
            e.setType("project");
            e.setTitle("Project: " + p.getProjectTitle());
            e.setStart(p.getStartDate().atStartOfDay());
            e.setEnd(p.getEndDate().atStartOfDay());
            e.setUrl("/admin/projects");
            events.add(e);
        }
        // maintenance visits
        for (MaintenanceSchedule s : scheduleService.getAllSchedules()) {
            CalendarEvent e = new CalendarEvent();
            e.setId(s.getId());
            e.setType("visit");
            e.setTitle("Visit #" + s.getId());
            e.setStart(s.getScheduleDate().atStartOfDay());
            e.setEnd(s.getScheduleDate().atStartOfDay().plusHours(2));
            e.setUrl("/support/dashboard");
            events.add(e);
        }

        return events;
    }

    /** 3) Handle drag/drop or resize **/
    @PostMapping("/api/calendar/events/{type}/{id}")
    @ResponseBody
    public String moveEvent(@PathVariable String type,
                            @PathVariable Long id,
                            @RequestParam String start,
                            @RequestParam String end) {
        switch (type) {
            case "task": {
                Task t = taskService.getTaskById(id);
                t.setDueDate(java.time.LocalDate.parse(start.substring(0,10)));
                taskService.saveTask(t);
                break;
            }
            case "project": {
                Project p = projectService.getProjectById(id);
                p.setStartDate(java.time.LocalDate.parse(start.substring(0,10)));
                p.setEndDate(java.time.LocalDate.parse(end.substring(0,10)));
                projectService.saveProject(p);
                break;
            }
            case "visit": {
                MaintenanceSchedule v = scheduleService.getScheduleById(id);
                v.setScheduleDate(java.time.LocalDate.parse(start.substring(0,10)));
                scheduleService.saveSchedule(v);
                break;
            }
        }
        return "ok";
    }

    /** 4) ICS feed **/
    @GetMapping(value="/calendar.ics", produces="text/calendar")
    public void icsFeed(HttpServletResponse resp) throws Exception {
        resp.setHeader("Content-Disposition","attachment; filename=bboxxtrack.ics");
        PrintWriter w = resp.getWriter();
        w.println("BEGIN:VCALENDAR");
        w.println("VERSION:2.0");
        w.println("PRODID:-//BBOXXTrack//EN");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
        fetchEvents().forEach(e -> {
            w.println("BEGIN:VEVENT");
            w.println("UID:" + e.getType() + "-" + e.getId() + "@bboxxtrack.com");
            w.println("DTSTAMP:" + dtf.format(java.time.LocalDateTime.now()));
            w.println("DTSTART:" + dtf.format(e.getStart()));
            w.println("DTEND:"   + dtf.format(e.getEnd()));
            w.println("SUMMARY:" + e.getTitle());
            w.println("DESCRIPTION:via BBOXXTrack");
            w.println("URL;VALUE=URI:" + e.getUrl());
            w.println("END:VEVENT");
        });

        w.println("END:VCALENDAR");
    }
}
