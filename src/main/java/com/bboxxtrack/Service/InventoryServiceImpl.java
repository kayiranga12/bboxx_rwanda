package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Inventory;
import com.bboxxtrack.Repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryServiceImpl extends InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Override
    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll();
    }

    @Override
    public Inventory saveInventory(Inventory inventory) {
        return inventoryRepository.save(inventory);
    }

    @Override
    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }
}
