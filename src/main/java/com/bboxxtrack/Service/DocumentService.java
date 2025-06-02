package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Document;
import com.bboxxtrack.Repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    public void saveDocument(MultipartFile file, String entityType, Long entityId) throws IOException {
        Document document = new Document();
        document.setFileName(file.getOriginalFilename());
        document.setContentType(file.getContentType());
        document.setData(file.getBytes());
        document.setEntityType(entityType);
        document.setEntityId(entityId);
        document.setUploadedAt(LocalDateTime.now());
        documentRepository.save(document);
    }

    public List<Document> getByRef(String entityType, Long entityId) {
        if (entityType == null || entityType.isBlank()) {
            throw new IllegalArgumentException("Entity type is required");
        }
        if (entityId == null) {
            throw new IllegalArgumentException("Entity ID is required");
        }
        return documentRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public Document getById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Document not found with ID: " + id));
    }

    public void save(MultipartFile f, String project, Long id) {

    }
}