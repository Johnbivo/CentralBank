ALTER TABLE employees
DROP COLUMN auth_id;

ALTER TABLE employees
    ADD COLUMN auth_id BIGINT UNIQUE;

ALTER TABLE employees
    ADD CONSTRAINT fk_employee_auth FOREIGN KEY (auth_id) REFERENCES auth(id);