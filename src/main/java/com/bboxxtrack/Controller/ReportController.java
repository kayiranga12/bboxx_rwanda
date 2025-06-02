//package com.bboxxtrack.Controller;
//
//import com.bboxxtrack.Model.Report;
//import com.bboxxtrack.Model.Task;
//import com.bboxxtrack.Service.ReportService;
//import com.bboxxtrack.Service.TaskService;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.io.IOException;
//import java.io.PrintWriter;
//import java.util.List;
//
//@RestController
//@RequestMapping("/reports")
//public class ReportController {
//    @Autowired
//    private ReportService reportService;
//
//    @Autowired
//    private TaskService taskService;
//
//    @PostMapping("/add")
//    public Report addReport(@RequestBody Report report) {
//        return reportService.saveReport(report);
//    }
//
//    @GetMapping("/all")
//    public List<Report> getAllReports() {
//        return reportService.getAllReports();
//    }
//
//    @GetMapping("/{id}")
//    public Report getReportById(@PathVariable Long id) {
//        return reportService.getReportById(id);
//    }
//
//    @DeleteMapping("/{id}")
//    public void deleteReport(@PathVariable Long id) {
//        reportService.deleteReport(id);
//    }
//
//    @GetMapping("/tasks/export")
//    public void exportTasksToCSV(HttpServletResponse response) throws IOException {
//        response.setContentType("text/csv");
//        response.setHeader("Content-Disposition", "attachment; filename=tasks.csv");
//
//        List<Task> tasks = taskService.getAllTasks();
//        PrintWriter writer = response.getWriter();
//        writer.println("Task Name,Description,Status,Due Date");
//
//        for (Task t : tasks) {
//            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\"%n",
//                    t.getTaskName(), t.getDescription(), t.getStatus(), t.getDueDate());
//        }
//
//        writer.flush();
//        writer.close();
//    }
//
//}
