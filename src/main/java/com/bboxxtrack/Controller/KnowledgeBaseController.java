//package com.bboxxtrack.Controller;
//
//import com.bboxxtrack.Model.Article;
//import com.bboxxtrack.Service.ArticleService;
//import com.bboxxtrack.Model.User;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//
//@Controller
//@RequestMapping("/support/kb")
//public class KnowledgeBaseController {
//
//    @Autowired private ArticleService articleService;
//
//    @GetMapping
//    public String list(@RequestParam(required=false) String q,
//                       Model model, HttpSession session) {
//        User u = (User)session.getAttribute("user");
//        if(u==null||!"Support".equals(u.getRole())) return "redirect:/login";
//
//        model.addAttribute("articles", q==null ?
//                articleService.all() :
//                articleService.search(q));
//        model.addAttribute("searchQuery", q);
//        return "support/kb_list";
//    }
//
//    @GetMapping("/add")
//    public String showForm(Model model,HttpSession session){
//        User u=(User)session.getAttribute("user");
//        if(u==null||!"Support".equals(u.getRole()))return"redirect:/login";
//        model.addAttribute("article",new Article());
//        return "support/kb_form";
//    }
//
//    @PostMapping("/add")
//    public String create(@ModelAttribute Article a){
//        articleService.save(a);
//        return "redirect:/support/kb";
//    }
//
//    @GetMapping("/{id}")
//    public String view(@PathVariable Long id, Model model){
//        model.addAttribute("article", articleService.get(id));
//        return "support/kb_detail";
//    }
//
//    @PostMapping("/{id}/delete")
//    public String delete(@PathVariable Long id){
//        articleService.delete(id);
//        return "redirect:/support/kb";
//    }
//}
