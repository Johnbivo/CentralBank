package com.bivolaris.centralbank.repositories;

import com.bivolaris.centralbank.entities.Auditlog;
import com.bivolaris.centralbank.entities.AuditStatus;
import com.bivolaris.centralbank.entities.Bank;
import com.bivolaris.centralbank.entities.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<Auditlog, UUID> {
    
    // Find audit logs by employee
    Page<Auditlog> findByEmployeeOrderByActionAtDesc(Employee employee, Pageable pageable);
    
    // Find audit logs by bank
    Page<Auditlog> findByBankOrderByActionAtDesc(Bank bank, Pageable pageable);
    
    // Find audit logs by status
    Page<Auditlog> findByStatusOrderByActionAtDesc(AuditStatus status, Pageable pageable);
    
    // Find audit logs by action
    Page<Auditlog> findByActionContainingIgnoreCaseOrderByActionAtDesc(String action, Pageable pageable);
    
    // Find audit logs by date range
    @Query("SELECT a FROM Auditlog a WHERE a.actionAt BETWEEN :startDate AND :endDate ORDER BY a.actionAt DESC")
    Page<Auditlog> findByActionAtBetweenOrderByActionAtDesc(
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        Pageable pageable
    );
    
    // Find recent unreviewed audit logs
    @Query("SELECT a FROM Auditlog a WHERE a.status = 'UNREVIEWED' ORDER BY a.actionAt DESC")
    List<Auditlog> findRecentUnreviewedLogs(Pageable pageable);
    
    // Count audit logs by employee in date range
    @Query("SELECT COUNT(a) FROM Auditlog a WHERE a.employee = :employee AND a.actionAt BETWEEN :startDate AND :endDate")
    long countByEmployeeAndDateRange(
        @Param("employee") Employee employee,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Find audit logs by IP address (useful for security investigations)
    Page<Auditlog> findByIpAddressOrderByActionAtDesc(String ipAddress, Pageable pageable);
}
