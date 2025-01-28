CREATE TABLE employees
(
    id             VARCHAR(255) PRIMARY KEY,
    first_name     VARCHAR(255) NOT NULL,
    last_name      VARCHAR(255),
    email          VARCHAR(255) NOT NULL UNIQUE,
    contact_number VARCHAR(255) NOT NULL,
    created_at     BIGINT,
    status         VARCHAR(255) NOT NULL
);

CREATE TABLE roles
(
    id            VARCHAR(255) PRIMARY KEY,
    role          VARCHAR(255) NOT NULL,
    employee_id   VARCHAR(255) NOT NULL,
    CONSTRAINT fk_employee FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);