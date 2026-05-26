# Neural Navigator - Job Portal

A professional job portal built as a team project by Neural Navigator from Khwaja Yunus Ali University.

This repository delivers a full-stack solution that enables recruiters to publish jobs and candidates to search, apply, and manage resumes in a centralized platform.

---

## Project Summary

Neural Navigator provides:
- A recruiter workflow for posting jobs and reviewing applicants
- A candidate experience for browsing jobs, submitting applications, and managing resumes
- Role-based access control for recruiters and candidates
- A Java backend paired with a responsive HTML/CSS/JavaScript frontend

---

## Team

Neural Navigator team members:
- Abid Hasan Hujaifa
- Md. Rabbi
- Md. Rakibul Islam
- Fatima Rahman Shoshi

From: Department of Computer Science and Engineering, Khwaja Yunus Ali University

---

## Features

Candidate features:
- Search and filter job listings
- Submit and track job applications
- Upload and manage resume documents
- Candidate dashboard for overview

Recruiter features:
- Post new job opportunities
- View and manage applicant details
- Recruiter dashboard for job tracking

Shared features:
- User registration and login
- Role-based dashboard access
- Resume upload and download
- MySQL data persistence

---

## Repository Structure

Job Portal/
├── backend/                    # Java server source, DAO, service, model, utility code
│   ├── bin/
│   ├── dao/
│   ├── model/
│   ├── server/
│   ├── service/
│   └── util/
├── frontend/                   # Client pages, styles, and script
│   ├── index.html
│   ├── login.html
│   ├── register.html
│   ├── candidate-dashboard.html
│   ├── recruiter-dashboard.html
│   ├── script.js
│   └── style.css
├── uploads/                    # Uploaded resume files
├── xampp_database_schema.sql   # MySQL database schema
├── RUN_BACKEND.bat             # Start backend server script
├── compile_backend.py          # Optional backend compilation helper
└── docs/                       # Project documentation
    └── PROJECT_OVERVIEW.md

---

## Technology Stack

- Backend: Java
- Frontend: HTML, CSS, JavaScript
- Database: MySQL
- Architecture: DAO + Service Layer
- Server: Java HttpServer-based backend

---

## Setup Instructions

### Prerequisites
- Java 8 or later
- MySQL database (XAMPP recommended)
- Browser for frontend pages

### Step 1: Create the Database

Import `xampp_database_schema.sql` into MySQL using one of the following methods:

Using phpMyAdmin:
1. Start MySQL in XAMPP.
2. Open http://localhost/phpmyadmin.
3. Choose the Import tab.
4. Select `xampp_database_schema.sql`.
5. Click Go.

Using MySQL CLI:

```powershell
cd "e:\Job Portal"
mysql -u root < xampp_database_schema.sql
```

---

### Step 2: Start the Backend

Run the backend server:

```powershell
cd "e:\Job Portal"
.\RUN_BACKEND.bat
```

---

### Step 3: Open the Frontend

Open the frontend pages directly by launching `frontend/index.html` in your browser.

To serve locally:

```powershell
cd "e:\Job Portal\frontend"
python -m http.server 8000
```

Visit http://localhost:8000.

---

## Notes

- `frontend/` contains the user interface pages and assets.
- `backend/` contains Java source code with DAO, model, service, and server layers.
- `uploads/` stores user resume files.
- `docs/PROJECT_OVERVIEW.md` provides a repository summary.

---

## Contribution

This project was developed by the Neural Navigator team as an academic project. Review the code, test the flows, and suggest improvements.
