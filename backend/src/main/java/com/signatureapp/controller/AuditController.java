package com.signatureapp.controller;

import com.signatureapp.dto.AuditResponse;
import com.signatureapp.security.UserPrincipal;
import com.signatureapp.service.AuditService;
import com.signatureapp.service.AuthService;
import com.signatureapp.service.DocumentService;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit")
public class AuditController {
    private final AuditService auditService;
    private final DocumentService documentService;
    private final AuthService authService;

    public AuditController(AuditService auditService, DocumentService documentService, AuthService authService) {
        this.auditService = auditService;
        this.documentService = documentService;
        this.authService = authService;
    }

    @GetMapping("/{docId}")
    public List<AuditResponse> documentAudit(
            @PathVariable Long docId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        documentService.ownedDocument(docId, authService.currentUser(principal));
        return auditService.forDocument(docId);
    }
}
