// src/main/java/com/bboxxtrack/Service/ReportGenerationService.java
package com.bboxxtrack.Service;

import com.bboxxtrack.Model.ScheduledReport;
import com.bboxxtrack.Model.Task;
import com.bboxxtrack.Model.Project;
import com.bboxxtrack.Model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportGenerationService {

    @Autowired private TaskService    taskService;
    @Autowired private ProjectService projectService;
    @Autowired private UserService    userService;

    /**
     * Returns a CSV byte array for the “completedTasksLast7” template.
     */
    public byte[] generate(ScheduledReport rpt) throws Exception {
        if ("completedTasksLast7".equals(rpt.getTemplateKey())) {
            int days = Integer.parseInt(rpt.getParameters()); // e.g. "7"
            LocalDate cutoff = LocalDate.now().minusDays(days);

            List<Task> tasks = taskService.getAllTasks().stream()
                    .filter(t -> "Done".equalsIgnoreCase(t.getStatus()))
                    .filter(t -> !t.getDueDate().isBefore(cutoff))
                    .toList();

            if ("CSV".equalsIgnoreCase(rpt.getFormat())) {
                try (var baos = new ByteArrayOutputStream();
                     var writer = new PrintWriter(baos)) {

                    writer.println("Task,Project,Technician,Done Date");
                    DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE;

                    for (Task t : tasks) {
                        // look up project title via the relationship
                        String projectTitle = "";
                        Project p = t.getProject();
                        if (p != null) {
                            projectTitle = p.getProjectTitle();
                        }

                        // look up technician name via the relationship
                        String techName = "";
                        User u = t.getAssignedTo();
                        if (u != null) {
                            techName = String.valueOf(u.getUsername());
                        }

                        // escape quotes
                        String taskNameEsc = t.getTaskName().replace("\"", "\"\"");
                        String projEsc     = projectTitle.replace("\"", "\"\"");
                        String techEsc     = techName.replace("\"", "\"\"");

                        writer.printf(
                                "\"%s\",\"%s\",\"%s\",%s%n",
                                taskNameEsc,
                                projEsc,
                                techEsc,
                                t.getDueDate().format(fmt)
                        );
                    }

                    writer.flush();
                    return baos.toByteArray();
                }
            }
            // you could add a PDF branch here if needed...
        }

        throw new IllegalArgumentException("Unknown template: " + rpt.getTemplateKey());
    }
}
