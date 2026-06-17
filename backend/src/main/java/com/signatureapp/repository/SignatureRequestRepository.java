package com.signatureapp.repository;

import com.signatureapp.model.SignatureRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignatureRequestRepository extends JpaRepository<SignatureRequest, Long> {
    List<SignatureRequest> findByDocumentIdOrderByCreatedAtAsc(Long documentId);
    Optional<SignatureRequest> findByPublicToken(String publicToken);
}
