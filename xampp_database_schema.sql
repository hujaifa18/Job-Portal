-- Job Portal Database Schema for XAMPP
-- This script creates a clean database from scratch

CREATE DATABASE IF NOT EXISTS jobportal_db;
USE jobportal_db;

-- Users Table
CREATE TABLE users (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('CANDIDATE', 'RECRUITER') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- Jobs Table
CREATE TABLE jobs (
    id INT NOT NULL AUTO_INCREMENT,
    title VARCHAR(150) NOT NULL,
    description LONGTEXT NOT NULL,
    salary DECIMAL(10, 2) NOT NULL,
    location VARCHAR(150) NOT NULL,
    company_name VARCHAR(150) NOT NULL,
    recruiter_email VARCHAR(100) NOT NULL,
    status ENUM('ACTIVE', 'CLOSED', 'DRAFT') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (recruiter_email) REFERENCES users(email) ON DELETE CASCADE,
    INDEX idx_location (location),
    INDEX idx_salary (salary),
    INDEX idx_status (status),
    INDEX idx_recruiter_email (recruiter_email)
);

-- Applications Table
CREATE TABLE applications (
    id INT NOT NULL AUTO_INCREMENT,
    job_id INT NOT NULL,
    candidate_email VARCHAR(100) NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED', 'WITHDRAWN') DEFAULT 'PENDING',
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE,
    FOREIGN KEY (candidate_email) REFERENCES users(email) ON DELETE CASCADE,
    UNIQUE KEY unique_application (job_id, candidate_email),
    INDEX idx_candidate_email (candidate_email),
    INDEX idx_status (status)
);

-- Resumes Table
CREATE TABLE resumes (
    id INT NOT NULL AUTO_INCREMENT,
    candidate_email VARCHAR(100) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size INT NOT NULL,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_primary BOOLEAN DEFAULT FALSE,
    PRIMARY KEY (id),
    FOREIGN KEY (candidate_email) REFERENCES users(email) ON DELETE CASCADE,
    INDEX idx_candidate_email (candidate_email),
    INDEX idx_is_primary (is_primary)
);

-- Insert Sample Data
INSERT INTO users (name, email, password, role) VALUES
('Tech Recruiter', 'recruiter@company.com', 'password123', 'RECRUITER'),
('HR Manager', 'hr@company.com', 'password123', 'RECRUITER'),
('John Doe', 'john@example.com', 'password123', 'CANDIDATE'),
('Jane Smith', 'jane@example.com', 'password123', 'CANDIDATE');

INSERT INTO jobs (title, description, salary, location, company_name, recruiter_email, status) VALUES
('Senior Developer', 'We are looking for a Senior Developer with 5+ years of experience in Java and Spring Boot.', 120000, 'New York, NY', 'Tech Corp', 'recruiter@company.com', 'ACTIVE'),
('Product Manager', 'Product Manager needed to lead our mobile app team with excellent communication skills.', 110000, 'San Francisco, CA', 'Tech Corp', 'recruiter@company.com', 'ACTIVE'),
('Data Scientist', 'Looking for Data Scientist with ML and Python expertise to join our analytics team.', 130000, 'Boston, MA', 'DataTech Inc', 'hr@company.com', 'ACTIVE'),
('Frontend Developer', 'React/Vue.js developer needed for our web platform. 3+ years required.', 95000, 'Remote', 'Tech Corp', 'recruiter@company.com', 'ACTIVE');

INSERT INTO applications (job_id, candidate_email, status) VALUES
(1, 'john@example.com', 'PENDING'),
(1, 'jane@example.com', 'PENDING'),
(2, 'john@example.com', 'ACCEPTED'),
(3, 'jane@example.com', 'PENDING'),
(4, 'john@example.com', 'REJECTED');
