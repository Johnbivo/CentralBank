CREATE TABLE accounts (
                          id BINARY(16) NOT NULL PRIMARY KEY DEFAULT (UUID_TO_BIN(UUID())),
                          account_number VARCHAR(50) NOT NULL,
                          bank_id BINARY(16) NOT NULL,
                          account_holder_name VARCHAR(255) NOT NULL,
                          account_type ENUM('CHECKING', 'SAVINGS', 'BUSINESS') NOT NULL,
                          balance DECIMAL(15,2) NOT NULL DEFAULT 0,
                          currency CHAR(3) NOT NULL DEFAULT 'EUR',
                          status ENUM('ACTIVE', 'INACTIVE', 'FROZEN', 'CLOSED') NOT NULL DEFAULT 'ACTIVE',
                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          CONSTRAINT fk_account_bank FOREIGN KEY (bank_id) REFERENCES banks(id),
                          CONSTRAINT uk_account_bank UNIQUE (account_number, bank_id)
);

ALTER TABLE transactions
    ADD CONSTRAINT fk_tx_from_account FOREIGN KEY (from_account_id) REFERENCES accounts(id),
ADD CONSTRAINT fk_tx_to_account FOREIGN KEY (to_account_id) REFERENCES accounts(id);