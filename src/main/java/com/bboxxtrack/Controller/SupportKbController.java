// src/main/java/com/bboxxtrack/Controller/SupportKbController.java
package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.KbArticle;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.KbArticleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/support/kb")
public class SupportKbController {

    private final KbArticleService kbService;
    public SupportKbController(KbArticleService kbService){ this.kbService = kbService; }

    // list all articles
    @GetMapping
    public String list(Model model, HttpSession session){
        User u = (User)session.getAttribute("user");
        if(u==null||!"Support".equals(u.getRole())) return "redirect:/login";
        model.addAttribute("articles", kbService.getAll());
        return "support/kb";
    }

    // show “new article” form
    @GetMapping("/add")
    public String addForm(Model model, HttpSession session){
        User u = (User)session.getAttribute("user");
        if(u==null||!"Support".equals(u.getRole())) return "redirect:/login";
        model.addAttribute("article", new KbArticle());
        return "support/kb_form";
    }

    // handle save
    @PostMapping("/add")
    public String save(@ModelAttribute KbArticle article, HttpSession session){
        User u = (User)session.getAttribute("user");
        if(u==null||!"Support".equals(u.getRole())) return "redirect:/login";
        kbService.save(article);
        return "redirect:/support/kb";
    }

    // detail view
    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session){
        User u = (User)session.getAttribute("user");
        if(u==null||!"Support".equals(u.getRole())) return "redirect:/login";
        KbArticle a = kbService.get(id);
        if(a==null) return "redirect:/support/kb";
        model.addAttribute("article", a);
        return "support/kb_detail";
    }

    // delete
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session){
        User u = (User)session.getAttribute("user");
        if(u==null||!"Support".equals(u.getRole())) return "redirect:/login";
        kbService.delete(id);
        return "redirect:/support/kb";
    }
}
