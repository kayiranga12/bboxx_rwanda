package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.DocumentService;
import com.bboxxtrack.Service.ProjectService;
import com.bboxxtrack.Service.TaskService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/technician")
public class TechnicianTaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private DocumentService documentService;

    /**
     * 1) View & filter your assigned tasks
     */
    @GetMapping("/tasks")
    public String viewTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            Model model,
            HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Technician".equals(user.getRole())) {
            return "redirect:/login";
        }

        // fetch all tasks assigned to this technician
        List<Task> all = taskService.getAllTasks().stream()
                .filter(t -> t.getAssignedToUserId().equals(user.getId()))
                .collect(Collectors.toList());

        // status filter
        List<Task> filtered = (status == null || status.isEmpty())
                ? all
                : all.stream()
                .filter(t -> status.equalsIgnoreCase(t.getStatus()))
                .collect(Collectors.toList());

        // search filter
        if (q != null && !q.isBlank()) {
            String lower = q.toLowerCase();
            filtered = filtered.stream()
                    .filter(t -> t.getTaskName().toLowerCase().contains(lower)
                            || t.getDescription().toLowerCase().contains(lower))
                    .collect(Collectors.toList());
        }

        // build status breakdown for analytics (optional)
        Map<String, Long> counts = all.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));

        model.addAttribute("tasks", filtered);
        model.addAttribute("filterStatus", status);
        model.addAttribute("searchQuery", q);
        model.addAttribute("statusLabels", List.copyOf(counts.keySet()));
        model.addAttribute("statusData", List.copyOf(counts.values()));

        return "technician/tasks";
    }

    /**
     * 2) Update a task's status and optionally attach files
     */
    @PostMapping("/tasks/update")
    public String updateStatus(
            @RequestParam("id") Long id,
            @RequestParam String status,
            @RequestParam(name = "files", required = false) MultipartFile[] files,
            HttpSession session
    ) throws Exception {
        return doUpdate(id, status, files, session);
    }

    /**
     * Alias mapping so that forms posting to /technician/task/update also work
     */
    @PostMapping("/task/update")
    public String updateStatusAlias(
            @RequestParam("taskId") Long id,
            @RequestParam String status,
            @RequestParam(name = "files", required = false) MultipartFile[] files,
            HttpSession session
    ) throws Exception {
        return doUpdate(id, status, files, session);
    }

    /**
     * Shared update logic
     */
    private String doUpdate(
            Long id,
            String status,
            MultipartFile[] files,
            HttpSession session
    ) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Technician".equals(user.getRole())) {
            return "redirect:/login";
        }

        Task t = taskService.getTaskById(id);
        if (t != null) {
            t.setStatus(status);
            taskService.saveTask(t);

            if (files != null) {
                for (MultipartFile f : files) {
                    if (!f.isEmpty()) {
                        documentService.save(f, "TASK", t.getId());
                    }
                }
            }
        }
        return "redirect:/technician/tasks";
    }

    /**
     * 3) Export your tasks to CSV (with project titles)
     */
    @GetMapping("/tasks/export")
    public void exportCsv(HttpServletResponse response, HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Technician".equals(user.getRole())) {
            response.sendRedirect("/login");
            return;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"your_tasks.csv\"");

        List<Task> tasks = taskService.getAllTasks().stream()
                .filter(t -> t.getAssignedToUserId().equals(user.getId()))
                .collect(Collectors.toList());

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try (PrintWriter w = response.getWriter()) {
            w.println("Task,Project,Due Date,Status");
            for (Task t : tasks) {
                String due = t.getDueDate().format(fmt);
                String projectTitle = projectService.getProjectById(t.getProjectId())
                        .getProjectTitle()
                        .replace("\"", "\"\"");
                w.printf("\"%s\",\"%s\",%s,%s%n",
                        t.getTaskName().replace("\"", "\"\""),
                        projectTitle,
                        due,
                        t.getStatus());
            }
        }
    }
}
