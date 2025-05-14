package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Inventory;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.InventoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/logistics")
public class LogisticsController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");

        if (user == null || !"Logistics".equals(user.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("inventory", inventoryService.getAllInventory());
        return "logistics/dashboard";
    }

    @PostMapping("/inventory/add")
    public String addInventory(@ModelAttribute Inventory item) {
        inventoryService.saveInventory(item);
        return "redirect:/logistics/dashboard";
    }
}
