package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByFullNameContainingIgnoreCase(String q);
    @Query("SELECT c FROM Customer c WHERE LOWER(c.fullName) LIKE LOWER(CONCAT('%', :query, '%')) OR c.id = :id")
    List<Customer> findByFullNameContainingIgnoreCaseOrId(@Param("query") String query, @Param("id") Long id);

}

