/* ============================================================
   JobPortal — script.js
   All original features preserved. Bugs fixed.
   ============================================================ */

/* ── Register ── */
function register() {
  const name     = document.getElementById('name').value.trim();
  const email    = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value.trim();
  const role     = document.getElementById('role').value;

  if (!name || !email || !password || !role) {
    alert('Please fill in all fields.'); return;
  }
  if (password.length < 6) {
    alert('Password must be at least 6 characters.'); return;
  }
  if (!validateEmail(email)) {
    alert('Please enter a valid email address.'); return;
  }

  fetch('http://localhost:8080/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `name=${encodeURIComponent(name)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}&role=${role}`
  })
  .then(res => res.text())
  .then(data => {
    if (data === 'Registered Successfully') {
      alert('✅ Registration successful! Please login now.');
      window.location.href = 'login.html';
    } else {
      alert('❌ Registration failed. Email may already exist.');
    }
  })
  .catch(() => alert('Registration failed. Please try again.'));
}

function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

/* ── Login ── */
function login() {
  const email    = document.getElementById('email').value.trim();
  const password = document.getElementById('password').value.trim();

  if (!email || !password) {
    alert('Please fill in all fields.'); return;
  }

  fetch('http://localhost:8080/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
  })
  .then(res => res.text())
  .then(data => {
    if (data !== 'Invalid') {
      sessionStorage.setItem('email', email);
      sessionStorage.setItem('role', data);
      alert('✅ Login successful!');
      if (data === 'RECRUITER') {
        window.location.href = 'recruiter-dashboard.html';
      } else if (data === 'CANDIDATE') {
        window.location.href = 'candidate-dashboard.html';
      }
    } else {
      alert('❌ Invalid email or password.');
    }
  })
  .catch(() => alert('Login failed. Please try again.'));
}

/* ── Apply for Job ── */
function applyJob(jobId, candidateEmail) {
  if (!jobId || !candidateEmail) {
    alert('Please login first to apply for jobs.'); return;
  }

  fetch('http://localhost:8080/apply', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `jobId=${jobId}&email=${encodeURIComponent(candidateEmail)}`
  })
  .then(res => res.text())
  .then(data => {
    if (data === 'Applied Successfully') {
      alert('✅ Applied successfully!');
    } else if (data === 'Failed') {
      alert('⚠️ You have already applied for this job.');
    } else {
      alert(data);
    }
  })
  .catch(() => alert('Failed to apply. Please try again.'));
}

/* ── Post Job (standalone page) ── */
function postJob() {
  const title       = document.getElementById('title').value.trim();
  const description = document.getElementById('description').value.trim();
  const salary      = document.getElementById('salary').value.trim();
  const location    = document.getElementById('location').value.trim();
  const company     = document.getElementById('company').value.trim();
  const email       = document.getElementById('email').value.trim();

  if (!title || !description || !salary || !location || !company || !email) {
    alert('Please fill in all fields.'); return;
  }
  if (isNaN(salary) || Number(salary) <= 0) {
    alert('Please enter a valid salary.'); return;
  }

  fetch('http://localhost:8080/postjob', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `title=${encodeURIComponent(title)}&description=${encodeURIComponent(description)}&salary=${salary}&location=${encodeURIComponent(location)}&company=${encodeURIComponent(company)}&email=${encodeURIComponent(email)}`
  })
  .then(res => res.text())
  .then(data => {
    if (data === 'Job Posted') {
      alert('✅ Job posted successfully!');
      ['title','description','salary','location','company','email'].forEach(id => {
        document.getElementById(id).value = '';
      });
    } else {
      alert('❌ Failed to post job.');
    }
  })
  .catch(() => alert('Failed to post job. Please try again.'));
}

/* ── Load Jobs (jobs.html) ── */
function loadJobs() {
  const container = document.getElementById('jobList');
  if (container) container.innerHTML = '<p class="text-muted small-text" style="padding:20px;">Loading jobs...</p>';

  fetch('http://localhost:8080/jobs')
  .then(res => res.json())
  .then(data => {
    let output = '';
    if (data.length === 0) {
      output = `<div class="empty-state"><div class="empty-icon">💼</div><p>No jobs available at the moment.</p></div>`;
    } else {
      data.forEach(job => {
        output += `
          <div class="card">
            <h3>${job.title}</h3>
            <p><span class="card-label">Company</span><br>${job.company}</p>
            <p><span class="card-label">Location</span><br>${job.location}</p>
            <div class="salary-chip">💰 $${parseFloat(job.salary).toLocaleString()} / yr</div>
            <div style="margin-top:16px;">
              <button onclick="applyJob(${job.id}, '${sessionStorage.getItem('email')}')">Apply Now →</button>
            </div>
          </div>`;
      });
    }
    if (container) container.innerHTML = output;
  })
  .catch(() => {
    if (container) container.innerHTML = '<p style="color:#fca5a5;">Error loading jobs. Please try again.</p>';
  });
}

/* ── Load Applicants (applicants.html) ── */
function loadApplicants() {
  const container = document.getElementById('list');
  if (container) container.innerHTML = '<p class="text-muted small-text" style="padding:20px;">Loading applicants...</p>';

  fetch('http://localhost:8080/applicants')
  .then(res => res.json())
  .then(data => {
    let output = '';
    if (data.length === 0) {
      output = `<div class="empty-state"><div class="empty-icon">👤</div><p>No applicants yet.</p></div>`;
    } else {
      data.forEach(a => {
        output += `
          <div class="card">
            <p><span class="card-label">Candidate</span><br><strong style="color:var(--text-1);">${a.email}</strong></p>
            <p><span class="card-label">Position</span><br>${a.job}</p>
            <p><span class="card-label">Company</span><br>${a.company}</p>
          </div>`;
      });
    }
    if (container) container.innerHTML = output;
  })
  .catch(() => {
    if (container) container.innerHTML = '<p style="color:#fca5a5;">Error loading applicants. Please try again.</p>';
  });
}

/* ── Logout ── */
function logout() {
  sessionStorage.clear();
  window.location.href = 'login.html';
}