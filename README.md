# Neural Navigator — Job Portal

A professional job portal built as a team project by Neural Navigator for Khwaja Yunus Ali University.

This repository delivers a complete full-stack solution that enables recruiters to publish jobs and candidates to search, apply, and manage resumes in a centralized platform.

---

## 🚀 Project Summary

Neural Navigator provides:
- A recruiter-facing workflow for posting jobs and reviewing applicants
- A candidate-facing experience for browsing jobs, submitting applications, and managing resumes
- Role-based access control for recruiters and candidates
- A Java backend paired with a responsive HTML/CSS/JavaScript frontend

---

## 👥 Team

**Neural Navigator**
- Abid Hasan Hujaifa
- Md. Rabbi
- Md. Rakibul Islam
- Fatima Rahman Shoshi

**University**: Department of Computer Science and Engineering, Khwaja Yunus Ali University

---

## ✨ Features

### Candidate features
- Search and filter job listings
- Submit and track job applications
- Upload and manage resume documents
- Candidate dashboard for application overview

### Recruiter features
- Post new job opportunities
- View and manage applicant details
- Recruiter dashboard for job and application tracking

### Common features
- User registration and login
- Role-based dashboard access
- Resume upload and file handling
- Database-backed persistence with MySQL

---

## 🏗️ Repository Structure

```
Job Portal/
├── backend/                    # Java server, models, DAO, services, utilities
│   ├── bin/
│   ├── dao/
│   ├── model/
│   ├── server/
│   ├── service/
│   └── util/
├── frontend/                   # Client pages, stylesheet, and script
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
├── compile_backend.py          # Backend compile helper script
├── DebugDB.java                # Debug class
└── docs/                       # Project documentation
    └── PROJECT_OVERVIEW.md
```

---

## 🛠️ Technology Stack

- **Backend**: Java
- **Frontend**: HTML, CSS, JavaScript
- **Database**: MySQL
- **Architecture**: DAO + Service Layer
- **Server**: Java HttpServer-based backend

---

## 📥 Setup Instructions

### Prerequisites
- Java 8 or later
- MySQL database (XAMPP recommended)
- Browser for frontend pages

### Step 1: Create the Database

Import `xampp_database_schema.sql` into MySQL using one of the following methods:

**Using phpMyAdmin**
1. Start MySQL in XAMPP.
2. Open `http://localhost/phpmyadmin`.
3. Choose the `Import` tab.
4. Select `xampp_database_schema.sql`.
5. Click `Go`.

**Using MySQL CLI**
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

The backend should start and connect to the database.

---

### Step 3: Open the Frontend

Open the frontend pages directly by launching `frontend/index.html` in your browser.

For a local server:

```powershell
cd "e:\Job Portal\frontend"
python -m http.server 8000
```
Then visit `http://localhost:8000`.

---

## 📄 Notes

- `frontend/` contains the user interface pages and assets.
- `backend/` contains Java source code with DAO, model, service, and server layers.
- `uploads/` stores user resume files.
- `docs/PROJECT_OVERVIEW.md` provides a repository-level summary.

---

## 📚 Documentation

Additional documentation is available in the `docs/` folder.

---

## 🙌 Contribution

This project was developed by the Neural Navigator team as an academic project. Feel free to review the code, test the flows, and propose improvements through the repository.

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
