package com.signatureapp.repository;

import com.signatureapp.model.Document;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
}
