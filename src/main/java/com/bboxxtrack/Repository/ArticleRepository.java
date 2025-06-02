package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article,Long> {
    List<Article> findByTitleContainingIgnoreCase(String q);
}
