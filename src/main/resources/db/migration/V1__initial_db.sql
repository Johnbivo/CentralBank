


CREATE TABLE banks (
                       id BINARY(16) NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
                       name VARCHAR(55) NOT NULL,
                       swift VARCHAR(11) NOT NULL,
                       base_api_endpoint VARCHAR(255) NOT NULL,
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                       status ENUM('ACTIVE','INACTIVE','SUSPENDED') NOT NULL DEFAULT 'INACTIVE'
);


CREATE TABLE transactions (
                              id BINARY(16) NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
                              from_account_id BINARY(16) NOT NULL,
                              to_account_id BINARY(16) NOT NULL,
                              from_bank_id BINARY(16) NOT NULL,
                              to_bank_id BINARY(16) NOT NULL,
                              amount DECIMAL(15,2) NOT NULL DEFAULT 0,
                              currency CHAR(3) NOT NULL DEFAULT 'EUR',
                              status ENUM('PENDING','COMPLETED','FAILED','CANCELLED') NOT NULL DEFAULT 'PENDING',
                              message TEXT,
                              initiated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              completed_at TIMESTAMP NULL,

                              CONSTRAINT fk_tx_from_bank FOREIGN KEY (from_bank_id) REFERENCES banks(id),
                              CONSTRAINT fk_tx_to_bank   FOREIGN KEY (to_bank_id) REFERENCES banks(id)
);


CREATE TABLE auth (
                      id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                      username VARCHAR(255) NOT NULL UNIQUE,
                      password VARCHAR(255) NOT NULL,
                      agreed_to_terms BOOLEAN NOT NULL DEFAULT FALSE,
                      verified_email BOOLEAN NOT NULL DEFAULT FALSE,
                      tfa_enabled BOOLEAN NOT NULL DEFAULT FALSE,
                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      last_login TIMESTAMP,
                      status ENUM('ACTIVE','INACTIVE','LOCKED') NOT NULL DEFAULT 'INACTIVE'
);


CREATE TABLE employees (
                           id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                           bank_id BINARY(16) NOT NULL,
                           first_name VARCHAR(255) NOT NULL,
                           last_name VARCHAR(255) NOT NULL,
                           email VARCHAR(255) NOT NULL UNIQUE,
                           phone_number VARCHAR(255) NOT NULL,
                           address VARCHAR(255) NOT NULL,
                           city VARCHAR(255) NOT NULL,
                           profession VARCHAR(55) NOT NULL,
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                           CONSTRAINT fk_employee_bank FOREIGN KEY (bank_id) REFERENCES banks(id)
);


CREATE TABLE fraudCases (
                            id BINARY(16) NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
                            transaction_id BINARY(16) NOT NULL,
                            bank_id BINARY(16) NOT NULL,
                            reviewed_by BIGINT NOT NULL,
                            reason TEXT NOT NULL,
                            status ENUM('PENDING','REVIEWED','DISMISSED') NOT NULL DEFAULT 'PENDING',
                            flagged_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                            CONSTRAINT fk_fraud_tx FOREIGN KEY (transaction_id) REFERENCES transactions(id),
                            CONSTRAINT fk_fraud_bank FOREIGN KEY (bank_id) REFERENCES banks(id),
                            CONSTRAINT fk_fraud_employee FOREIGN KEY (reviewed_by) REFERENCES employees(id)
);


CREATE TABLE auditLogs (
                           id BINARY(16) NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
                           employee_id BIGINT NOT NULL,
                           bank_id BINARY(16) NOT NULL,
                           action VARCHAR(255) NOT NULL,
                           ip_address VARCHAR(255) NOT NULL,
                           action_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                           status ENUM('UNREVIEWED','REVIEWED') NOT NULL DEFAULT 'UNREVIEWED',

                           CONSTRAINT fk_audit_employee FOREIGN KEY (employee_id) REFERENCES employees(id),
                           CONSTRAINT fk_audit_bank FOREIGN KEY (bank_id) REFERENCES banks(id)
);
