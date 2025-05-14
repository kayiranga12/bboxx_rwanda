package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {
}
