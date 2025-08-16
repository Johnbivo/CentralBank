package com.bivolaris.centralbank.repositories;

import com.bivolaris.centralbank.entities.Auditlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<Auditlog, UUID> {
}
