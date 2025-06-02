// src/main/java/com/bboxxtrack/Repository/KbArticleRepository.java
package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.KbArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KbArticleRepository extends JpaRepository<KbArticle,Long> {
}
