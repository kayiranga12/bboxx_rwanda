package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Comment;
import com.bboxxtrack.Model.User;
import com.bboxxtrack.Service.CommentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager/projects/{projectId}/comments")
public class CommentController {
    @Autowired private CommentService commentService;

    @GetMapping
    public String list(@PathVariable Long projectId, Model model, HttpSession session) {
        User u = (User) session.getAttribute("user");
        if(u==null || !"Project Manager".equals(u.getRole())) return "redirect:/login";
        model.addAttribute("comments", commentService.getCommentsForProject(projectId));
        return "manager/comments_fragment";
    }

    @PostMapping
    public String add(@PathVariable Long projectId,
                      @RequestParam String content,
                      HttpSession session) {
        User u = (User) session.getAttribute("user");
        if(u==null || !"Project Manager".equals(u.getRole())) return "redirect:/login";
        Comment c = new Comment();
        c.setProjectId(projectId);
        c.setUserId(u.getId());
        c.setContent(content);
        commentService.addComment(c);
        return "redirect:/manager/projects";
    }
}
