package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProjectService projectService;

    @Transactional
    public Task saveTask(Task task) {
        if (task.getTaskName() == null || task.getTaskName().isBlank()) {
            throw new IllegalArgumentException("Task name is required");
        }
        if (task.getDueDate() == null) {
            throw new IllegalArgumentException("Due date is required");
        }
        if (task.getProject() == null || task.getProject().getId() == null) {
            throw new IllegalArgumentException("Project is required");
        }

        // Validate project
        Project project = projectService.getById(task.getProject().getId());
        task.setProject(project);

        // Handle assignedTo (optional)
        if (task.getAssignedTo() != null && task.getAssignedTo().getId() != null) {
            User assignedTo = userService.getUserById(task.getAssignedTo().getId());
            if (!"Technician".equals(assignedTo.getRole())) {
                throw new IllegalArgumentException("Assigned user must be a Technician");
            }
            task.setAssignedTo(assignedTo);
        } else {
            task.setAssignedTo(null); // Allow unassigned tasks
        }

        // Set default status if not provided
        if (task.getStatus() == null || task.getStatus().isBlank()) {
            task.setStatus("Assigned");
        }

        // No strict validation for recurrenceRule; allow any string or null
        if (task.getRecurrenceRule() != null && task.getRecurrenceRule().isBlank()) {
            task.setRecurrenceRule(null); // Convert blank to null
        }

        return taskRepository.save(task);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Task not found with ID: " + id));
    }

    public List<Task> getTasksByTechnician(Long userId, String status, String query) {
        List<Task> tasks = taskRepository.findByAssignedToId(userId);
        if (status != null && !status.isBlank()) {
            tasks = tasks.stream()
                    .filter(t -> status.equals(t.getStatus()))
                    .collect(Collectors.toList());
        }
        if (query != null && !query.isBlank()) {
            String q = query.toLowerCase();
            tasks = tasks.stream()
                    .filter(t -> t.getTaskName().toLowerCase().contains(q) ||
                            (t.getDescription() != null && t.getDescription().toLowerCase().contains(q)))
                    .collect(Collectors.toList());
        }
        return tasks;
    }

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new IllegalArgumentException("Task not found with ID: " + id);
        }
        taskRepository.deleteById(id);
    }
}