package com.signatureapp.controller;

import com.signatureapp.dto.DocumentResponse;
import com.signatureapp.security.UserPrincipal;
import com.signatureapp.service.AuthService;
import com.signatureapp.service.DocumentService;
import com.signatureapp.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/docs")
public class DocumentController {
    private final DocumentService documentService;
    private final AuthService authService;

    public DocumentController(DocumentService documentService, AuthService authService) {
        this.documentService = documentService;
        this.authService = authService;
    }

    @PostMapping("/upload")
    public DocumentResponse upload(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request
    ) throws IOException {
        return documentService.upload(file, authService.currentUser(principal), RequestUtil.clientIp(request));
    }

    @GetMapping
    public List<DocumentResponse> list(@AuthenticationPrincipal UserPrincipal principal) {
        return documentService.list(authService.currentUser(principal));
    }

    @GetMapping("/{id}")
    public DocumentResponse get(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request
    ) {
        return documentService.getOwned(id, authService.currentUser(principal), RequestUtil.clientIp(request));
    }

    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> file(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return pdf(documentService.resource(id, authService.currentUser(principal), false));
    }

    @GetMapping("/{id}/signed-file")
    public ResponseEntity<Resource> signedFile(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return pdf(documentService.resource(id, authService.currentUser(principal), true));
    }

    private ResponseEntity<Resource> pdf(Resource resource) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
