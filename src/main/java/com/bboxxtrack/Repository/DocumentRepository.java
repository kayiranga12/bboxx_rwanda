package com.bboxxtrack.Repository;

import com.bboxxtrack.Model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByEntityTypeAndEntityId(String entityType, Long entityId);
}
