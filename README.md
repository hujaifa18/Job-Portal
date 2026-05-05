# 🎯 Job Portal - Complete Job Management System

A full-stack web application that connects job seekers with recruiters, featuring job listings, applications, resume management, and role-based dashboards.

---

## ✨ Features

### For Candidates
- 🔍 **Search & Filter Jobs** - Find jobs by title, location, and salary range
- 📋 **Job Applications** - Apply for jobs and track application status
- 📄 **Resume Management** - Upload, download, and manage multiple resumes
- 👤 **Candidate Dashboard** - Central hub for all job-related activities
- ✅ **Application Tracking** - View all applications with status (Pending/Accepted/Rejected)

### For Recruiters
- ➕ **Post Jobs** - Create and manage job listings with detailed descriptions
- 👥 **View Applicants** - See all candidates who applied for posted jobs
- 📊 **Recruiter Dashboard** - Overview of posted jobs and applicants
- 🎯 **Manage Job Postings** - Edit, view, and delete job listings

### General
- 🔐 **User Authentication** - Register and login with role-based access
- 📱 **Responsive Design** - Works seamlessly on desktop and mobile
- ⚡ **Fast Performance** - Optimized database queries with indexing
- 🎨 **Modern UI** - Beautiful gradient design with smooth interactions

---

## 🏗️ Project Architecture

```
Job Portal/
├── backend/
│   ├── src/
│   │   ├── dao/              # Database Access Objects
│   │   │   ├── UserDAO.java
│   │   │   ├── JobDAO.java
│   │   │   ├── ApplicationDAO.java
│   │   │   └── ResumesDAO.java
│   │   ├── model/             # Data Models
│   │   │   ├── User.java
│   │   │   ├── Job.java
│   │   │   └── Application.java
│   │   ├── service/           # Business Logic
│   │   │   ├── UserService.java
│   │   │   ├── JobService.java
│   │   │   └── ApplicationService.java
│   │   ├── server/            # HTTP Server
│   │   │   └── Server.java (Custom HttpServer, Port 8080)
│   │   └── util/              # Utilities
│   │       └── DBConnection.java
│   └── lib/                   # Libraries
├── frontend/
│   ├── index.html             # Home page
│   ├── login.html             # Login page
│   ├── register.html          # Registration page
│   ├── candidate-dashboard.html # Candidate dashboard
│   ├── recruiter-dashboard.html # Recruiter dashboard
│   ├── jobs.html              # Job listings (legacy)
│   ├── postJob.html           # Post job form (legacy)
│   ├── applicants.html        # Applicants view (legacy)
│   ├── resume.html            # Resume management (legacy)
│   ├── script.js              # JavaScript functions & API calls
│   └── style.css              # CSS styling
└── database_schema.sql        # MySQL database schema
```

---

## 🚀 Tech Stack

### Backend
- **Language**: Java
- **Server**: Built-in Java HttpServer (com.sun.net.httpserver)
- **Database**: MySQL
- **Architecture**: DAO Pattern with Service Layer

### Frontend
- **HTML5** - Semantic markup
- **CSS3** - Responsive design with gradients
- **Vanilla JavaScript** - Fetch API for HTTP requests
- **Session Storage** - Client-side session management

### Database
- **MySQL** with 4 main tables:
  - `users` - User accounts and roles
  - `jobs` - Job listings
  - `applications` - Job applications
  - `resumes` - Resume files

---

## 📋 Database Schema

### Users Table
```sql
id, name, email (UNIQUE), password, role (CANDIDATE/RECRUITER), created_at
```

### Jobs Table
```sql
id, title, description, salary, location, company_name, recruiter_email (FK),
status (ACTIVE/CLOSED/DRAFT), created_at, updated_at
```

### Applications Table
```sql
id, job_id (FK), candidate_email (FK), status (PENDING/ACCEPTED/REJECTED/WITHDRAWN),
applied_at, updated_at, UNIQUE(job_id, candidate_email)
```

### Resumes Table
```sql
id, candidate_email (FK), file_name, file_path, file_size, uploaded_at, is_primary
```

---

## 🔧 Setup Instructions

### Prerequisites
- **Java 8** or higher
- **XAMPP** with MySQL (or standalone MySQL)
- Text editor or IDE

### 1️⃣ Database Setup (phpMyAdmin)

**Option A: Using phpMyAdmin (Easiest)**

1. Open XAMPP Control Panel → Start MySQL
2. Go to `http://localhost/phpmyadmin` in browser
3. Click **Import** tab
4. Select **Choose File** → browse to `xampp_database_schema.sql`
5. Click **Go** to execute
6. ✅ Tables created: `users`, `jobs`, `applications`, `resumes`

**Option B: Using Command Line**

```powershell
cd "e:\Job Portal"
mysql -u root < xampp_database_schema.sql
```

**Option C: Using MySQL Workbench**

1. Open MySQL Workbench
2. File → Open SQL Script → Select `xampp_database_schema.sql`
3. Execute (⚡ button)
4. Verify: `USE jobportal_db; SHOW TABLES;`

---

### 2️⃣ Backend Setup & Run

```powershell
# Navigate to project folder
cd "e:\Job Portal"

# Run the backend server
.\RUN_BACKEND.bat

# Expected output:
# ✅ Database Connected
# ✅ Server running at http://localhost:8080
```

**Keep the terminal open while developing!**

---

### 3️⃣ Frontend Access

1. Open browser
2. Navigate to: `file:///e:/Job Portal/frontend/index.html`
3. Or serve with Python:
```powershell
cd frontend
python -m http.server 8000
# Then visit: http://localhost:8000
```
```

### 3. Frontend Setup
```bash
# Open frontend files in a web browser
# Navigate to index.html

# Or use a simple HTTP server
cd frontend
python -m http.server 3000
# Then visit http://localhost:3000
```

---

## 📱 API Endpoints

### Authentication
| Method | Endpoint | Body | Response |
|--------|----------|------|----------|
| POST | `/register` | name, email, password, role | "Registered Successfully" / "Failed" |
| POST | `/login` | email, password | role (RECRUITER/CANDIDATE) / "Invalid" |

### Jobs
| Method | Endpoint | Query/Body | Response |
|--------|----------|-----------|----------|
| POST | `/postjob` | title, description, salary, location, company, email | "Job Posted" / "Failed" |
| GET | `/jobs` | - | JSON array of all jobs |
| GET | `/search` | keyword, location, minSalary, maxSalary | JSON array of filtered jobs |
| GET | `/recruiter-jobs` | email | JSON array of recruiter's jobs |

### Applications
| Method | Endpoint | Query/Body | Response |
|--------|----------|-----------|----------|
| POST | `/apply` | jobId, email | "Applied Successfully" / "Failed" |
| GET | `/applicants` | - | JSON array of all applications |
| GET | `/my-applications` | email | JSON array of candidate's applications |

### Resume Management
| Method | Endpoint | Query/Body | Response |
|--------|----------|-----------|----------|
| POST | `/upload-resume` | email, file | "Resume uploaded successfully" |
| GET | `/resumes` | email | JSON array of candidate's resumes |
| GET | `/download-resume` | id | File download |
| POST | `/set-primary-resume` | resumeId, email | "Primary resume set" |

---

## 👥 User Roles & Access

### Candidate
- View all active jobs
- Search and filter jobs
- Apply for jobs
- Track application status
- Upload and manage resumes
- View personal dashboard

### Recruiter
- Post job listings
- View applicants for their jobs
- Manage job postings
- View recruiter dashboard
- Access job statistics

---

## 🔐 Security Features

✅ Prepared Statements (SQL Injection Prevention)
✅ Password Storage (Hashed - recommended upgrade)
✅ Email Validation
✅ Form Input Validation
✅ CORS Headers
✅ Session Management
✅ Foreign Key Constraints
✅ Unique Constraints

---

## 📝 Usage Examples

### As a Candidate
1. **Register** → Register as "Job Candidate"
2. **Login** → Automatically redirected to Candidate Dashboard
3. **Find Jobs** → Search and filter jobs by title, location, salary
4. **Apply** → Click "Apply Now" on any job
5. **Track** → View all applications in "My Applications" tab
6. **Resume** → Upload and manage resumes in "My Resume" tab

### As a Recruiter
1. **Register** → Register as "Recruiter"
2. **Login** → Automatically redirected to Recruiter Dashboard
3. **Post Job** → Click "Post New Job" and fill in details
4. **View Stats** → See total jobs posted and applicants
5. **Manage** → Edit, view, or delete your job postings
6. **Review** → View all applicants in the "Applicants" tab

---

## 🎯 Key Features Explained

### Search & Filter System
- **Search by Title**: Searches job title and description
- **Filter by Location**: Case-insensitive location matching
- **Filter by Salary**: Min-Max salary range filtering
- **Combined Search**: Mix multiple filters for precise results
- **Smart Querying**: Uses LIKE operator for flexible matching

### Role-Based Dashboards
- **Automatic Redirect**: Users routed to correct dashboard on login
- **Session Management**: Using browser sessionStorage
- **Tab-Based Navigation**: Organized information in tabs
- **Statistics**: Job and applicant counts displayed

### Resume Management
- **Multiple Resumes**: Upload and maintain multiple resume versions
- **Primary Resume**: Set one as primary for job applications
- **Download & Delete**: Full control over resume files
- **File Tracking**: Upload date and file size recorded

---

## 🚦 Sample Test Data

The database includes sample users and jobs:

**Sample Recruiters**
- Email: recruiter@company.com | Password: password123
- Email: hr@company.com | Password: password123

**Sample Candidates**
- Email: john@example.com | Password: password123
- Email: jane@example.com | Password: password123

**Sample Jobs**
- Senior Developer ($120,000) - New York, NY
- Product Manager ($110,000) - San Francisco, CA
- Data Scientist ($130,000) - Boston, MA
- Frontend Developer ($95,000) - Remote

---

## 🐛 Known Limitations & Future Enhancements

### Current Limitations
- ⚠️ Plain text passwords (should use hashing)
- ⚠️ No password reset functionality
- ⚠️ Limited file validation for resumes
- ⚠️ No email verification
- ⚠️ No pagination for large datasets

### Recommended Upgrades
- 🔐 Implement BCrypt/SHA256 for password hashing
- 📧 Add email verification system
- 🔄 Implement JWT authentication
- 📄 Add pagination for jobs and applications
- 🔍 Add advanced filtering (experience level, job type, etc.)
- 💾 Implement connection pooling (HikariCP)
- 🧪 Add unit testing (JUnit, Mockito)
- 📊 Add admin dashboard
- 🔔 Add email notifications

---

## 🧪 Testing Checklist

- [ ] Registration with valid/invalid data
- [ ] Login with correct/incorrect credentials
- [ ] Role-based redirects
- [ ] Post a job (recruiter)
- [ ] Search jobs by title
- [ ] Filter jobs by location
- [ ] Filter jobs by salary range
- [ ] Apply for a job (candidate)
- [ ] View my applications
- [ ] Upload resume
- [ ] Download resume
- [ ] Set primary resume
- [ ] View applicants (recruiter)
- [ ] Session persistence
- [ ] Logout functionality

---

## 📞 Troubleshooting

### Server won't start
- Check if port 8080 is available
- Verify Java installation: `java -version`
- Check server logs for compilation errors

### Database connection failed
- Ensure MySQL is running: `mysql -u root -p`
- Verify credentials in DBConnection.java
- Check database_schema.sql was executed

### Frontend not loading jobs
- Ensure backend server is running
- Check browser console for errors
- Verify CORS is enabled in Server.java

### Resume upload not working
- Check "uploads/" directory exists
- Verify file permissions
- Check file size limits

---

## 📚 Learning Outcomes (Viva Preparation)

### Core Concepts Covered
1. **Client-Server Architecture** - HTTP communication between frontend and backend
2. **DAO Pattern** - Database abstraction and access layer design
3. **Service Layer** - Business logic separation from database
4. **RESTful API Design** - HTTP methods and resource endpoints
5. **Role-Based Access Control** - User authentication and authorization
6. **Session Management** - User session tracking and persistence
7. **Database Design** - Schema, relationships, indexes, constraints
8. **Form Validation** - Client-side and potential server-side validation
9. **Error Handling** - Try-catch blocks, meaningful error messages
10. **Responsive Web Design** - CSS Grid, Flexbox, media queries

### Technologies & Tools
- **Java**: Object-oriented programming, exception handling
- **MySQL**: DDL, DML, query optimization, indexing
- **HTML/CSS**: Semantic markup, modern CSS features
- **JavaScript**: DOM manipulation, Fetch API, event handling
- **HTTP**: Request/response cycle, headers, status codes

### Best Practices Demonstrated
- Prepared statements to prevent SQL injection
- Input validation and sanitization
- Separation of concerns (DAO, Service, Server)
- RESTful API design principles
- User-friendly error messages
- Session-based authentication
- Code organization and file structure

---

## 📄 License

This project is open source and available for educational purposes.

---

## 👨‍💻 Author

Created as a comprehensive full-stack web development project demonstrating modern web application architecture, database design, and user interface implementation.

---

## 🙏 Acknowledgments

- Built with vanilla JavaScript (no frameworks)
- MySQL for reliable data persistence
- Java's built-in HttpServer for simplicity
- Modern web standards (HTML5, CSS3)

---

## 📞 Support & Questions

For questions or issues, review the troubleshooting section or check your:
- Browser console (F12) for JavaScript errors
- Server terminal for backend errors
- MySQL logs for database errors

---

**Happy Job Hunting! 🚀** and **Happy Recruiting! 👔**
