-- Make bank_id nullable in auditLogs table to allow logging employee actions without bank context
ALTER TABLE auditLogs MODIFY COLUMN bank_id BINARY(16) NULL;

-- Drop the foreign key constraint temporarily
ALTER TABLE auditLogs DROP FOREIGN KEY fk_audit_bank;

-- Re-add the foreign key constraint to allow null values
ALTER TABLE auditLogs ADD CONSTRAINT fk_audit_bank 
    FOREIGN KEY (bank_id) REFERENCES banks(id);
