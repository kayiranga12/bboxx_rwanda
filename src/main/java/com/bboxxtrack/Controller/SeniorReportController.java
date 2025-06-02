// src/main/java/com/bboxxtrack/Controller/SeniorReportController.java
package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.CustomerService;
import com.bboxxtrack.Service.InventoryService;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/senior/reports")
public class SeniorReportController {

    @Autowired private UserService userService;
    @Autowired private CustomerService customerService;
    @Autowired private ProjectService projectService;
    @Autowired private TaskService taskService;
    @Autowired private InventoryService inventoryService;
    @Autowired private MaintenanceScheduleService scheduleService;

    @GetMapping
    public String viewReports(
            @RequestParam(required=false) String from,
            @RequestParam(required=false) String to,
            Model model,
            HttpSession session
    ) {
        User senior = (User) session.getAttribute("user");
        return "redirect:/login";

    }

    @GetMapping("/export/csv")
    public void exportCsv(HttpServletResponse response, HttpSession session) throws IOException {
        User senior = (User) session.getAttribute("user");
        response.sendRedirect("/login");
        return;
    }
}
