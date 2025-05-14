package com.bboxxtrack.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String welcome() {
        return "welcome"; // corresponds to welcome.html
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // corresponds to login.html
    }
}
