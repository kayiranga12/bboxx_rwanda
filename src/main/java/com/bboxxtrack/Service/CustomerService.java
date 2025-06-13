package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Customer;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Repository.CustomerRepository;
import com.bboxxtrack.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private UserRepository userRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id).orElse(null);
    }

    public List<User> getAllTechnicians() {
        return userRepository.findByRole("Technician");
    }

    public List<Customer> searchCustomers(String query) {
        try {
            Long id = Long.parseLong(query);
            return customerRepository.findByFullNameContainingIgnoreCaseOrId(query, id);
        }
        catch (NumberFormatException e) {
            return customerRepository.findByFullNameContainingIgnoreCaseOrId(query, null);
        }
    }

    public Customer saveCustomer(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }
        if (customer.getFullName() == null || customer.getFullName().isBlank()) {
            throw new IllegalArgumentException("Customer full name is required");
        }
        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new IllegalArgumentException("Customer email is required");
        }
        // Additional validation (e.g. well‐formed email) could go here
        return customerRepository.save(customer);
    }
    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new IllegalArgumentException("Customer not found with ID: " + id);
        }
        customerRepository.deleteById(id);
    }
}