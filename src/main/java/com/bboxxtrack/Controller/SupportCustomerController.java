package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Customer;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/support/customers")
public class SupportCustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * Show search form and (optionally) results
     * Quick Action: Find Customer
     */
    @GetMapping("/search")
    public String searchForm(
            @RequestParam(value = "q", required = false) String q,
            Model model,
            HttpSession session
    ) {
        User u = (User) session.getAttribute("user");
        if (u == null || !"Support".equals(u.getRole())) {
            return "redirect:/login";
        }

        List<Customer> results;
        if (q != null && !q.isBlank()) {
            results = customerService.searchCustomers(q);
        } else {
            results = List.of(); // empty initially
        }

        model.addAttribute("query", q);
        model.addAttribute("results", results);
        return "support/customer_search";
    }
}
