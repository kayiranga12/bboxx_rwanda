package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByProjectIdOrderByCreatedAtAsc(Long projectId);
}
