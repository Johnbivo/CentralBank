-- Add updated_at column to transactions table for tracking changes
ALTER TABLE transactions 
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
