package com.bboxxtrack.Controller;

import com.bboxxtrack.Model.Document;
import com.bboxxtrack.Service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class DocumentController {
    @Autowired private DocumentService docService;

    @GetMapping("/attachments/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        Document d = docService.getById(id);
        if (d == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(d.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + d.getFileName() + "\"")
                .body(d.getData());
    }
}
