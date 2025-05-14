package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
