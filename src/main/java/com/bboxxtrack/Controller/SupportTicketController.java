package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.*;
import com.bboxxtrack.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/support/tickets")
public class SupportTicketController {

    @Autowired private TicketService ticketService;
    @Autowired private TicketCommentService commentService;
    @Autowired private CustomerService customerService;
    @Autowired private EmailService emailService;
    @Autowired private UserService userService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String stage, // if you filter by stage
            Model model,
            HttpSession session
    ) {
        User u = (User) session.getAttribute("user");
        if (u == null || !"Support".equals(u.getRole())) {
            return "redirect:/login";
        }

        List<Ticket> tickets;
        if (q != null && !q.isBlank()) {
            tickets = ticketService.search(q);
        } else if (stage != null && !stage.isEmpty()) {
            // you could filter by stage if needed
            tickets = ticketService.filterByStage(TicketStage.valueOf(stage.toUpperCase()));
        } else {
            tickets = ticketService.all();
        }
        List<User> techs = userService.getUsersByRole("Technician");
        Map<Long, String> techNames = techs.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));

        model.addAttribute("techNames",         techNames);
        model.addAttribute("technicians",       techs);
        model.addAttribute("tickets", tickets);
        model.addAttribute("customers", customerService.getAllCustomers());
        model.addAttribute("searchQuery", q);
        model.addAttribute("filterStage", stage);
        return "support/tickets";
    }

    @GetMapping({"/new", "/add"})
    public String showForm(Model model, HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (u == null || !"Support".equals(u.getRole())) {
            return "redirect:/login";
        }

        Ticket ticket = new Ticket();
        ticket.setPriority(Priority.MEDIUM);
        ticket.setStage(TicketStage.CREATED);

        model.addAttribute("ticket", ticket);

        // Let Support pick which Technician to assign to
        List<User> allPMs = userService.getUsersByRole("Technician");
        model.addAttribute("technician", allPMs);


        List<Customer> customers = customerService.getAllCustomers();
        model.addAttribute("customers", customers);

        // ADD THIS LINE - Convert customers to JSON for JavaScript
        model.addAttribute("customersJson", customers);

        return "support/ticket_form";
    }

    @PostMapping("/add")
    public String create(
            @ModelAttribute("ticket") Ticket ticket,
            @RequestParam(value = "files", required = false) MultipartFile[] files,
            HttpSession session,
            Model model
    ) {
        User u = (User) session.getAttribute("user");
        if (u == null || !"Support".equals(u.getRole())) {
            return "redirect:/login";
        }

        try {
            // 1) Validate Customer
            Customer customer = customerService.getCustomerById(ticket.getCustomer().getId());
            if (customer == null) {
                throw new IllegalArgumentException("Invalid customer ID");
            }
            ticket.setCustomer(customer);

            // 3) Set the stage to ASSIGNED_TO_PM
            ticket.setStage(TicketStage.CREATED);

            // 4) Ensure priority is set
            if (ticket.getPriority() == null) {
                ticket.setPriority(Priority.MEDIUM);
            }

            ticketService.save(ticket);

            return "redirect:/support/tickets";
        }
        catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("customers", customerService.getAllCustomers());
            model.addAttribute("technician", userService.getUsersByRole("Technician"));
            return "support/ticket_form";
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (u == null || !"Support".equals(u.getRole())) {
            return "redirect:/login";
        }
        Ticket t = ticketService.get(id);
        if (t == null) {
            return "redirect:/support/tickets";
        }
        List<TicketComment> comments = commentService.findForTicket(id);
        model.addAttribute("ticket", t);
        model.addAttribute("comments", comments);
        model.addAttribute("newComment", new TicketComment());
        return "support/ticket_detail";
    }

    @PostMapping("/{id}/comment")
    public String comment(
            @PathVariable Long id,
            @ModelAttribute("newComment") TicketComment c,
            HttpSession session
    ) {
        User u = (User) session.getAttribute("user");
        Ticket t = ticketService.get(id);
        if (u == null || t == null) {
            return "redirect:/support/tickets";
        }
        c.setTicket(t);
        c.setUserId(u.getId());
        commentService.save(c);
        return "redirect:/support/tickets/" + id;
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String stage
    ) {
        // If you really need to let Support change “stage” (e.g. reopen or close),
        // you can parse the incoming “stage” string into TicketStage enum:
        Ticket t = ticketService.get(id);
        if (t != null) {
            if ("IN_PROGRESS".equalsIgnoreCase(stage) && t.getFirstResponseAt() == null) {
                t.setFirstResponseAt(LocalDateTime.now());
            }
            if ("CLOSED".equalsIgnoreCase(stage)) {
                t.setClosedAt(LocalDateTime.now());
            }
            // t.setStatus(...) → replaced with:
            t.setStage(TicketStage.valueOf(stage.toUpperCase()));
            ticketService.save(t);
        }
        return "redirect:/support/tickets/" + id;
    }

    @PostMapping("/{id}/escalate")
    public String escalate(@PathVariable Long id, HttpSession session) {
        User u = (User) session.getAttribute("user");
        if (u == null || !"Support".equals(u.getRole())) {
            return "redirect:/login";
        }
        Ticket t = ticketService.get(id);
        if (t != null) {
            // Instead of t.setStatus(TicketStatus.ESCALATED), we do:
            t.setStage(TicketStage.CREATED);
            ticketService.save(t);

            emailService.sendEmail(
                    "manager@bboxxtrack.com",
                    "Ticket Escalated: #" + t.getId(),
                    "A high-priority ticket has been escalated:\n\n" + t.getTitle()
            );
        }
        return "redirect:/support/tickets/" + id;
    }
}
