package com.signatureapp.controller;

import com.signatureapp.dto.PublicSignRequest;
import com.signatureapp.dto.RejectRequest;
import com.signatureapp.dto.SignatureResponse;
import com.signatureapp.service.DocumentService;
import com.signatureapp.service.SignatureService;
import com.signatureapp.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class PublicSigningController {
    private final SignatureService signatureService;
    private final DocumentService documentService;

    public PublicSigningController(SignatureService signatureService, DocumentService documentService) {
        this.signatureService = signatureService;
        this.documentService = documentService;
    }

    @GetMapping("/sign/{token}")
    public SignatureResponse info(@PathVariable String token) {
        return signatureService.publicInfo(token);
    }

    @GetMapping("/sign/{token}/file")
    public ResponseEntity<Resource> file(@PathVariable String token) {
        Resource resource = documentService.publicResource(token);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @PostMapping("/sign/{token}")
    public SignatureResponse sign(
            @PathVariable String token,
            @RequestBody PublicSignRequest request,
            HttpServletRequest servletRequest
    ) throws IOException {
        return signatureService.publicSign(token, request, RequestUtil.clientIp(servletRequest));
    }

    @PostMapping("/sign/{token}/reject")
    public SignatureResponse reject(
            @PathVariable String token,
            @RequestBody RejectRequest request,
            HttpServletRequest servletRequest
    ) {
        return signatureService.reject(token, request, RequestUtil.clientIp(servletRequest));
    }
}
