function register() {

    let name = document.getElementById("name").value.trim();
    let email = document.getElementById("email").value.trim();
    let password = document.getElementById("password").value.trim();
    let role = document.getElementById("role").value;

    // Validation
    if (!name || !email || !password || !role) {
        alert("Please fill in all fields");
        return;
    }

    if (password.length < 6) {
        alert("Password must be at least 6 characters long");
        return;
    }

    if (!validateEmail(email)) {
        alert("Please enter a valid email address");
        return;
    }

    fetch("http://localhost:8080/register", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `name=${encodeURIComponent(name)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}&role=${role}`
    })
    .then(res => res.text())
    .then(data => {
        if (data === 'Registered Successfully') {
            alert("Registration successful! Please login now.");
            window.location.href = 'login.html';
        } else {
            alert("Registration failed. Email may already exist.");
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Registration failed. Please try again.');
    });
}

function validateEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}
function login() {

    let email = document.getElementById("email").value;
    let password = document.getElementById("password").value;

    if (!email || !password) {
        alert("Please fill in all fields");
        return;
    }

    fetch("http://localhost:8080/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
    })
    .then(res => res.text())
    .then(data => {
        if (data !== "Invalid") {
            sessionStorage.setItem('email', email);
            sessionStorage.setItem('role', data);
            alert("Login successful!");
            
            // Redirect based on role
            if (data === 'RECRUITER') {
                window.location.href = 'recruiter-dashboard.html';
            } else if (data === 'CANDIDATE') {
                window.location.href = 'candidate-dashboard.html';
            }
        } else {
            alert("Invalid email or password");
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Login failed. Please try again.');
    });
}

function applyJob(jobId, candidateEmail) {

    if (!jobId || !candidateEmail) {
        alert("Please login first to apply for jobs");
        return;
    }

    fetch("http://localhost:8080/apply", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `jobId=${jobId}&email=${encodeURIComponent(candidateEmail)}`
    })
    .then(res => res.text())
    .then(data => {
        if (data === 'Applied Successfully') {
            alert("Applied successfully!");
        } else if (data === 'Failed') {
            alert("You have already applied for this job or an error occurred");
        } else {
            alert(data);
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Failed to apply. Please try again.');
    });
}

function postJob() {

    let title = document.getElementById("title").value.trim();
    let description = document.getElementById("description").value.trim();
    let salary = document.getElementById("salary").value.trim();
    let location = document.getElementById("location").value.trim();
    let company = document.getElementById("company").value.trim();
    let email = document.getElementById("email").value.trim();

    // Validation
    if (!title || !description || !salary || !location || !company || !email) {
        alert("Please fill in all fields");
        return;
    }

    if (isNaN(salary) || salary <= 0) {
        alert("Please enter a valid salary");
        return;
    }

    fetch("http://localhost:8080/postjob", {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: `title=${encodeURIComponent(title)}&description=${encodeURIComponent(description)}&salary=${salary}&location=${encodeURIComponent(location)}&company=${encodeURIComponent(company)}&email=${encodeURIComponent(email)}`
    })
    .then(res => res.text())
    .then(data => {
        if (data === 'Job Posted') {
            alert("Job posted successfully!");
            // Reset form
            document.getElementById("title").value = '';
            document.getElementById("description").value = '';
            document.getElementById("salary").value = '';
            document.getElementById("location").value = '';
            document.getElementById("company").value = '';
            document.getElementById("email").value = '';
        } else {
            alert("Failed to post job");
        }
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Failed to post job. Please try again.');
    });
}
function loadJobs() {

    fetch("http://localhost:8080/jobs")
    .then(res => res.json())
    .then(data => {

        let output = "";

        if (data.length === 0) {
            output = '<p style="text-align: center; color: #999;">No jobs available at the moment.</p>';
        } else {
            data.forEach(job => {

                output += `
                    <div class="card">
                        <h3>${job.title}</h3>
                        <p><span class="card-label">Company:</span> ${job.company}</p>
                        <p><span class="card-label">Location:</span> ${job.location}</p>
                        <p><span class="card-label">Salary:</span> $${parseFloat(job.salary).toLocaleString()}</p>
                        <button class="btn-secondary" onclick="applyJob(${job.id}, '${sessionStorage.getItem('email')}')">Apply Now</button>
                    </div>
                `;
            });
        }

        document.getElementById("jobList").innerHTML = output;
    })
    .catch(error => {
        console.error('Error loading jobs:', error);
        document.getElementById("jobList").innerHTML = '<p style="color: red;">Error loading jobs. Please try again.</p>';
    });
}
function loadApplicants() {

    fetch("http://localhost:8080/applicants")
    .then(res => res.json())
    .then(data => {

        let output = "";

        if (data.length === 0) {
            output = '<p style="text-align: center; color: #999;">No applicants yet.</p>';
        } else {
            data.forEach(a => {

                output += `
                    <div class="card">
                        <p><span class="card-label">Candidate:</span> ${a.email}</p>
                        <p><span class="card-label">Position:</span> ${a.job}</p>
                        <p><span class="card-label">Company:</span> ${a.company}</p>
                    </div>
                `;
            });
        }

        document.getElementById("list").innerHTML = output;
    })
    .catch(error => {
        console.error('Error loading applicants:', error);
        document.getElementById("list").innerHTML = '<p style="color: red;">Error loading applicants. Please try again.</p>';
    });
}

function logout() {
    sessionStorage.clear();
    window.location.href = "login.html";
}
