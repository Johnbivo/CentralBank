ALTER TABLE employees
DROP FOREIGN KEY fk_employee_auth;

ALTER TABLE employees
DROP COLUMN auth_id;

ALTER TABLE auth
    ADD COLUMN employee_id BIGINT UNIQUE;

ALTER TABLE auth
    ADD CONSTRAINT fk_auth_employee FOREIGN KEY (employee_id) REFERENCES employees(id);


