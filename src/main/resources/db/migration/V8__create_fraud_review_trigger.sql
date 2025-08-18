
DELIMITER $$

CREATE TRIGGER fraud_case_review_trigger
    AFTER UPDATE ON fraudcases
    FOR EACH ROW
BEGIN

    IF NEW.status = 'DISMISSED' AND OLD.status = 'PENDING' THEN

        UPDATE transactions 
        SET status = 'PENDING',
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.transaction_id 
          AND status = 'FLAGGED_FOR_FRAUD';
    END IF;

    IF NEW.status = 'REVIEWED' AND OLD.status = 'PENDING' THEN
        UPDATE transactions 
        SET status = 'FAILED',
            completed_at = CURRENT_TIMESTAMP,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.transaction_id 
          AND status = 'FLAGGED_FOR_FRAUD';
    END IF;
END$$

DELIMITER ;
