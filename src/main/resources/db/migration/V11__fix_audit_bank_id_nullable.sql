
ALTER TABLE auditLogs MODIFY COLUMN bank_id BINARY(16) NULL;


ALTER TABLE auditLogs DROP FOREIGN KEY fk_audit_bank;

ALTER TABLE auditLogs ADD CONSTRAINT fk_audit_bank 
    FOREIGN KEY (bank_id) REFERENCES banks(id);
