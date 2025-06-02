package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.User;
import com.bboxxtrack.Repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password, HttpSession session) {
        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user != null && user.getPassword().equals(password)) {
            session.setAttribute("user", user);

            // Role-based routing
            switch (user.getRole()) {
                case "Admin":
                    return "redirect:/admin/dashboard";
                case "Technician":
                    return "redirect:/technician/dashboard";
                case "Project Manager":
                    return "redirect:/manager/dashboard";
                case "Logistics":
                    return "redirect:/logistics/dashboard";
                case "Support":
                    return "redirect:/support/dashboard";
                case "Senior Management":
                    return "redirect:/senior/dashboard";
                default:
                    return "redirect:/login?error";
            }
        }
        return "redirect:/login?error";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}