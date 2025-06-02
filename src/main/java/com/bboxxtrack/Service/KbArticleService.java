// src/main/java/com/bboxxtrack/Service/KbArticleService.java
package com.bboxxtrack.Service;

import com.bboxxtrack.Model.KbArticle;
import com.bboxxtrack.Repository.KbArticleRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class KbArticleService {
    private final KbArticleRepository repo;
    public KbArticleService(KbArticleRepository repo){this.repo=repo;}
    public List<KbArticle> getAll() { return repo.findAll(); }
    public KbArticle get(Long id){ return repo.findById(id).orElse(null); }
    public KbArticle save(KbArticle a){ return repo.save(a); }
    public void delete(Long id){ repo.deleteById(id); }
}
