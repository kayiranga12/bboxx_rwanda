package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Customer;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.CustomerService;
import com.bboxxtrack.Service.DocumentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/admin/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DocumentService documentService;

    /**
     * Show the list of customers.
     */
    @GetMapping
    public String showCustomers(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"Admin".equals(user.getRole())) {
            return "redirect:/login";
        }
        model.addAttribute("customers", customerService.getAllCustomers());
        return "admin/customers";
    }

    /**
     * Handle new customer registration and any file attachments.
     */
    @PostMapping("/add")
    public String addCustomer(
            @ModelAttribute Customer customer,
            @RequestParam(name = "files", required = false) MultipartFile[] files,
            HttpSession session
    ) throws Exception {
        // ensure admin is logged in
        User admin = (User) session.getAttribute("user");
        if (admin == null || !"Admin".equals(admin.getRole())) {
            return "redirect:/login";
        }

        // save the customer entity
        Customer saved = customerService.saveCustomer(customer);

        // save each non-empty uploaded file as a Document
        if (files != null) {
            for (MultipartFile f : files) {
                if (!f.isEmpty()) {
                    documentService.save(
                            f,
                            "CUSTOMER",
                            saved.getId()
                    );
                }
            }
        }

        return "redirect:/admin/customers";
    }
}
