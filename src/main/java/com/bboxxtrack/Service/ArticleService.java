package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Article;
import com.bboxxtrack.Repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ArticleService {
    @Autowired private ArticleRepository repo;
    public List<Article> all()             { return repo.findAll(); }
    public Article get(Long id)            { return repo.findById(id).orElse(null); }
    public Article save(Article a)         { return repo.save(a); }
    public List<Article> search(String q)  { return repo.findByTitleContainingIgnoreCase(q); }
    public void delete(Long id)            { repo.deleteById(id); }
}
