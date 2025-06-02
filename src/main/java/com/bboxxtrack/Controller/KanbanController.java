// src/main/java/com/bboxxtrack/Controller/KanbanController.java
package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.ProjectService;
import com.bboxxtrack.Service.TaskService;
import com.bboxxtrack.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/manager/kanban")
public class KanbanController {

    @Autowired private TaskService taskService;
    @Autowired private UserService userService;
    @Autowired private ProjectService projectService;

    @GetMapping
    public String kanbanBoard(Model model, HttpSession session) {
        User mgr = (User) session.getAttribute("user");
        if (mgr == null || !"Project Manager".equals(mgr.getRole())) {
            return "redirect:/login";
        }

        // 1) statuses and technicians
        List<String> statuses = List.of("Assigned", "In Progress", "Done");
        List<User> technicians = userService.getAllUsers()
                .stream()
                .filter(u -> "Technician".equals(u.getRole()))
                .toList();

        // 2) all tasks
        List<Task> allTasks = taskService.getAllTasks();

        // 3) build board: techId → (status → list of tasks)
        Map<Long, Map<String, List<Task>>> board = new HashMap<>();
        for (User tech : technicians) {
            Map<String, List<Task>> perStatus = new LinkedHashMap<>();
            for (String st : statuses) {
                List<Task> bucket = new ArrayList<>();
                for (Task t : allTasks) {
                    // use the relationship getter, not getAssignedToUserId()
                    if (t.getAssignedTo() != null
                            && Objects.equals(t.getAssignedTo().getId(), tech.getId())
                            && st.equals(t.getStatus())) {
                        bucket.add(t);
                    }
                }
                perStatus.put(st, bucket);
            }
            board.put(tech.getId(), perStatus);
        }

        model.addAttribute("technicians", technicians);
        model.addAttribute("statuses", statuses);
        model.addAttribute("board", board);
        return "manager/kanban";
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Void> updateTask(
            @RequestParam Long id,
            @RequestParam String status,
            @RequestParam Long technicianId) {

        Task t = taskService.getTaskById(id);
        if (t == null) return ResponseEntity.notFound().build();

        t.setStatus(status);
        // set the relation instead of a raw ID
        User tech = userService.getUserById(technicianId);
        t.setAssignedTo(tech);

        taskService.saveTask(t);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/kanban")
    public String projectKanban(HttpSession session, Model model) {
        User u = (User) session.getAttribute("user");
        if (u == null || !"Project Manager".equals(u.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("projects", projectService.getAllProjects());
        return "manager/kanban";
    }

    @PostMapping("/kanban/move/{id}")
    @ResponseBody
    public ResponseEntity<Void> moveColumn(
            @PathVariable Long id,
            @RequestParam String status) {

        Project p = projectService.getById(id);
        if (p == null) return ResponseEntity.notFound().build();

        p.setStatus(status);
        projectService.saveProject(p);
        return ResponseEntity.ok().build();
    }
}
