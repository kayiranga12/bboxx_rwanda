package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Inventory;
import com.bboxxtrack.Service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/add")
    public Inventory addInventory(@RequestBody Inventory inventory) {
        return inventoryService.saveInventory(inventory);
    }

    @GetMapping("/all")
    public List<Inventory> getAllInventory() {
        return inventoryService.getAllInventory();
    }

    @GetMapping("/{id}")
    public Inventory getInventoryById(@PathVariable Long id) {
        return inventoryService.getInventoryById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteInventory(@PathVariable Long id) {
        inventoryService.deleteInventory(id);
    }
}
