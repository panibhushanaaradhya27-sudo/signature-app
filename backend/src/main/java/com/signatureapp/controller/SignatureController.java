package com.signatureapp.controller;

import com.signatureapp.dto.SignatureRequestDto;
import com.signatureapp.dto.SignatureResponse;
import com.signatureapp.security.UserPrincipal;
import com.signatureapp.service.AuthService;
import com.signatureapp.service.SignatureService;
import com.signatureapp.util.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/signatures")
public class SignatureController {
    private final SignatureService signatureService;
    private final AuthService authService;

    public SignatureController(SignatureService signatureService, AuthService authService) {
        this.signatureService = signatureService;
        this.authService = authService;
    }

    @PostMapping
    public SignatureResponse create(
            @RequestBody SignatureRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest servletRequest
    ) {
        return signatureService.create(request, authService.currentUser(principal), RequestUtil.clientIp(servletRequest));
    }

    @GetMapping("/{docId}")
    public List<SignatureResponse> list(
            @PathVariable Long docId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return signatureService.list(docId, authService.currentUser(principal));
    }

    @PostMapping("/finalize/{docId}")
    public Map<String, String> finalizeDocument(
            @PathVariable Long docId,
            @AuthenticationPrincipal UserPrincipal principal,
            HttpServletRequest request
    ) throws IOException {
        signatureService.finalizeOwned(docId, authService.currentUser(principal), RequestUtil.clientIp(request));
        return Map.of("message", "Finalize completed when all signatures are signed");
    }
}
