-- Job Portal Database Schema
-- Run this once to set up the database

CREATE DATABASE IF NOT EXISTS jobportal_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jobportal_db;

-- ========================= USERS =========================
CREATE TABLE IF NOT EXISTS users (
    id         INT          NOT NULL AUTO_INCREMENT,
    name       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       ENUM('candidate', 'recruiter') NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB;

-- ========================= JOBS =========================
CREATE TABLE IF NOT EXISTS jobs (
    id              INT           NOT NULL AUTO_INCREMENT,
    title           VARCHAR(200)  NOT NULL,
    description     TEXT,
    salary          DECIMAL(12,2) NOT NULL DEFAULT 0,
    location        VARCHAR(150),
    company_name    VARCHAR(150)  NOT NULL,
    recruiter_email VARCHAR(150)  NOT NULL,
    created_at      TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_recruiter (recruiter_email),
    INDEX idx_location  (location),
    INDEX idx_salary    (salary)
) ENGINE=InnoDB;

-- ========================= APPLICATIONS =========================
CREATE TABLE IF NOT EXISTS applications (
    id              INT       NOT NULL AUTO_INCREMENT,
    job_id          INT       NOT NULL,
    candidate_email VARCHAR(150) NOT NULL,
    status          ENUM('pending', 'reviewed', 'accepted', 'rejected') NOT NULL DEFAULT 'pending',
    applied_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uq_application (job_id, candidate_email),   -- prevents duplicate applications
    INDEX idx_candidate (candidate_email),
    FOREIGN KEY (job_id) REFERENCES jobs(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ========================= RESUMES =========================
CREATE TABLE IF NOT EXISTS resumes (
    id              INT          NOT NULL AUTO_INCREMENT,
    candidate_email VARCHAR(150) NOT NULL,
    file_name       VARCHAR(255) NOT NULL,
    file_path       VARCHAR(500) NOT NULL,
    file_size       INT          NOT NULL DEFAULT 0,
    is_primary      BOOLEAN      NOT NULL DEFAULT FALSE,
    uploaded_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    INDEX idx_resume_email (candidate_email)
) ENGINE=InnoDB;