package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.Role;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Repository.ProjectRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserService userService;

    @Transactional
    public Project saveProject(Project project) {
        if (project.getProjectTitle() == null || project.getProjectTitle().isBlank()) {
            throw new IllegalArgumentException("Project title is required");
        }
        if (project.getStatus() == null || project.getStatus().isBlank()) {
            throw new IllegalArgumentException("Project status is required");
        }
        if (project.getLocation() == null || project.getLocation().isBlank()) {
            throw new IllegalArgumentException("Project location is required");
        }
        if (project.getStartDate() == null) {
            throw new IllegalArgumentException("Project start date is required");
        }
        if (project.getEndDate() == null) {
            throw new IllegalArgumentException("Project end date is required");
        }
        // (Optional) If a manager was explicitly set by the caller, verify their role:
        if (project.getManager() != null && project.getManager().getId() != null) {
            User manager = userService.getUserById(project.getManager().getId());
            if (!"Project Manager".equals(manager.getRole())) {
                throw new IllegalArgumentException("Manager must have role Project Manager");
            }
            project.setManager(manager);
        }
        return projectRepository.save(project);
    }

    public Project getById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + id));
    }

    public Project getProjectById(Long id) {
        return getById(id);
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }

    public Page<Project> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable);
    }

    public List<Project> getProjectsByStatus(String status) {
        return projectRepository.findByStatus(status);
    }

    public List<Project> getProjectsByManager(Long managerId) {
        User manager = userService.getUserById(managerId);
        if (!manager.getRole().equals("Project Manager")) {
            throw new IllegalArgumentException("User is not a Project Manager");
        }
        return projectRepository.findByManagerId(managerId);
    }

    @Transactional
    public void deleteProject(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new IllegalArgumentException("Project not found with ID: " + id);
        }
        projectRepository.deleteById(id);
    }

    @Transactional
    public void importFromCsv(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean firstLine = true;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Skip header
                }
                String[] parts = line.split(",");
                if (parts.length < 5) {
                    continue; // Skip invalid lines
                }
                Project project = new Project();
                project.setProjectTitle(parts[0].replace("\"\"", "\""));
                project.setStatus(parts[1]);
                project.setLocation(parts[2]);
                project.setStartDate(parts[3].isEmpty() ? null : LocalDate.parse(parts[3]));
                project.setEndDate(parts[4].isEmpty() ? null : LocalDate.parse(parts[4]));
                // Assume manager is set separately or default to a known manager
                User manager = userService.getAllUsers().stream()
                        .filter(u -> u.getRole().equals("Project Manager"))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No project manager found"));
                project.setManager(manager);
                projectRepository.save(project);
            }
        }
    }

    public Map<String, Map<String, Long>> getProjectProgressData(String startDate, String endDate) {
        LocalDate start = startDate.isEmpty() ? LocalDate.now().minusMonths(1) : LocalDate.parse(startDate);
        LocalDate end = endDate.isEmpty() ? LocalDate.now() : LocalDate.parse(endDate);

        List<User> managers = userService.getUsersByRole(String.valueOf(Role.valueOf("Project Manager")));
        Map<String, Map<String, Long>> progressData = new HashMap<>();

        for (User manager : managers) {
            Map<String, Long> metrics = new HashMap<>();
            long total = projectRepository.countByManagerIdAndStartDateBetween(
                    manager.getId(), start, end);
            long completed = projectRepository.countByManagerIdAndStatusAndStartDateBetween(
                    manager.getId(), "Completed", start, end);
            metrics.put("total", total);
            metrics.put("completed", completed);
            progressData.put(manager.getUsername(), metrics);
        }

        return progressData;
    }

}