package com.bivolaris.centralbank.dtos;

import com.bivolaris.centralbank.entities.AuditStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AuditLogDto {
    private UUID id;
    private String employeeName;
    private String employeeEmail;
    private String bankName;
    private String action;
    private String ipAddress;
    private LocalDateTime actionAt;
    private AuditStatus status;

    // Constructor for mapping from entity
    public AuditLogDto(UUID id, String employeeName, String employeeEmail, 
                       String bankName, String action, String ipAddress, 
                       LocalDateTime actionAt, AuditStatus status) {
        this.id = id;
        this.employeeName = employeeName;
        this.employeeEmail = employeeEmail;
        this.bankName = bankName;
        this.action = action;
        this.ipAddress = ipAddress;
        this.actionAt = actionAt;
        this.status = status;
    }
}
