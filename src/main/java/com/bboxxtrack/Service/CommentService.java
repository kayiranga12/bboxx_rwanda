package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Comment;
import com.bboxxtrack.Repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository repo;

    public List<Comment> getCommentsForProject(Long projectId) {
        return repo.findByProjectIdOrderByCreatedAtAsc(projectId);
    }

    public Comment addComment(Comment c) {
        return repo.save(c);
    }
}
