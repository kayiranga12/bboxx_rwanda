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
@RequestMapping("/admin/inventory")
public class AdminInventoryController {

    @Autowired
    private InventoryService inventoryService;

    // Show inventory list + form
    @GetMapping
    public String viewInventory(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        model.addAttribute("inventoryList", inventoryService.getAllInventory());
        model.addAttribute("inventoryItem", new Inventory());
        return "admin/inventory";
    }

    // Handle new item submission
    @PostMapping("/add")
    public String addInventory(@ModelAttribute Inventory inventory) {
        inventoryService.saveInventory(inventory);
        return "redirect:/admin/inventory";
    }

    // Delete an item
    @GetMapping("/delete/{id}")
    public String deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
        return "redirect:/admin/inventory";
    }
}