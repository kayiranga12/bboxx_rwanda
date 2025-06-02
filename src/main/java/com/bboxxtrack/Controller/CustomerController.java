// src/main/java/com/bboxxtrack/Controller/CustomerController.java
package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Customer;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.CustomerService;
import com.bboxxtrack.Service.DocumentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DocumentService documentService;

    /**
     * Show the “Register New Customer” form + list of existing customers.
     * Add an empty Customer (for form binding) plus the list of Customers.
     */
    @GetMapping
    public String showCustomers(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }

        // 1) For the form binding, we need an empty Customer in the model:
        model.addAttribute("customer", new Customer());

        // 2) Pull all existing customers so we can display them below the form:
        List<Customer> all = customerService.getAllCustomers();
        model.addAttribute("customers", all);

        return "admin/customers";
    }

    /**
     * Handle the form POST for "Register New Customer".
     * If validation fails, redisplay the form with error messages & existing list.
     */
    @PostMapping("/add")
    public String addCustomer(
            @Valid @ModelAttribute("customer") Customer customer,
            BindingResult bindingResult,
            @RequestParam(name = "files", required = false) MultipartFile[] files,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes
    ) throws Exception {
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"Admin".equals(admin.getRole())) {
            return "redirect:/login";
        }

        // If binding/validation errors → redisplay the same page with errors + customer list:
        if (bindingResult.hasErrors()) {
            // Re‐add the list of existing customers so the page renders below the form
            model.addAttribute("customers", customerService.getAllCustomers());
            return "admin/customers";
        }

        // 1) Save the Customer entity
        Customer saved = customerService.saveCustomer(customer);

        // 2) If any attachment files were included, save them via DocumentService
        if (files != null) {
            for (MultipartFile f : files) {
                if (f != null && !f.isEmpty()) {
                    documentService.save(f, "CUSTOMER", saved.getId());
                }
            }
        }

        redirectAttributes.addFlashAttribute("message", "Customer registered successfully");
        return "redirect:/admin/customers";
    }
}
