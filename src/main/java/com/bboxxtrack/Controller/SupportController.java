//package com.bboxxtrack.Controller;
//
//import com.bboxxtrack.Model.MaintenanceSchedule;
//import com.bboxxtrack.Model.User;
//import com.bboxxtrack.Service.MaintenanceScheduleService;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//@RequestMapping("/support")
//public class SupportController {
//
//    @Autowired
//    private MaintenanceScheduleService scheduleService;
//
//    @GetMapping("/dashboard")
//    public String supportDashboard(HttpSession session, Model model) {
//        User user = (User) session.getAttribute("user");
//
//        if (user == null || !"Support".equals(user.getRole())) {
//            return "redirect:/login";
//        }
//
//        model.addAttribute("schedules", scheduleService.getAllSchedules());
//        return "support/dashboard";
//    }
//
//    @PostMapping("/schedule/update")
//    public String updateSchedule(@RequestParam Long id, @RequestParam String status) {
//        MaintenanceSchedule schedule = scheduleService.getScheduleById(id);
//        if (schedule != null) {
//            schedule.setStatus(status);
//            scheduleService.saveSchedule(schedule);
//        }
//        return "redirect:/support/dashboard";
//    }
//}
