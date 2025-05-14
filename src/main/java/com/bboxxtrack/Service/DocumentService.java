package com.bboxxtrack.Service;

import com.bboxxtrack.Model.Document;
import com.bboxxtrack.Repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {
    @Autowired private DocumentRepository repo;

    public Document save(
            MultipartFile file,
            String referenceType,
            Long referenceId
    ) throws Exception {
        Document d = new Document();
        d.setFileName(file.getOriginalFilename());
        d.setContentType(file.getContentType());
        d.setReferenceType(referenceType);
        d.setReferenceId(referenceId);
        d.setUploadedAt(LocalDateTime.now());
//        d.setUploadedBy(uploadedBy);
        d.setData(file.getBytes());
        return repo.save(d);
    }

    public List<Document> getByRef(String referenceType, Long referenceId) {
        return repo.findByReferenceTypeAndReferenceId(referenceType, referenceId);
    }

    public Document getById(Long id) {
        return repo.findById(id).orElse(null);
    }
}
