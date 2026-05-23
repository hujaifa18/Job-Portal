const API = 'http://localhost:8080';

function parseApiResponse(response) {
  const contentType = response.headers.get('Content-Type') || '';
  if (contentType.includes('application/json')) {
    return response.json();
  }
  return response.text();
}

function getSessionUser() {
  return {
    email: sessionStorage.getItem('email') || '',
    role: sessionStorage.getItem('role') || ''
  };
}

function renderNavbar() {
  const nav = document.getElementById('navLinks');
  if (!nav) return;

  const user = getSessionUser();
  const baseLinks = [
    { href: 'index.html', label: 'Home' },
    { href: 'jobs.html', label: 'Browse Jobs' }
  ];

  let html = baseLinks.map(link => `<a href="${link.href}">${link.label}</a>`).join('');
  html += `
    <div class="nav-dropdown">
      <button class="dropdown-trigger">More ▾</button>
      <div class="dropdown-menu">
        ${user.role === 'CANDIDATE' ? '<a href="resume.html">My Resume</a>' : ''}
        ${user.role === 'RECRUITER' ? '<a href="postJob.html">Post Job</a>' : ''}
        ${!user.email ? '<a href="register.html">Register</a>' : ''}
      </div>
    </div>`;

  if (user.email && user.role) {
    const dashboardPage = user.role === 'RECRUITER' ? 'recruiter-dashboard.html' : 'candidate-dashboard.html';
    html += `
      <a href="${dashboardPage}" class="btn-nav-cta">Dashboard</a>
      <span class="text-muted small-text nav-user">${user.email}</span>
      <button class="logout-btn" onclick="logout()">Logout</button>`;
  } else {
    html += `
      <a href="login.html">Login</a>
      <a href="register.html" class="btn-nav-cta">Register</a>`;
  }

  nav.innerHTML = html;
}

function setupNavbar() {
  renderNavbar();
  const toggle = document.getElementById('navbarToggle');
  const navLinks = document.getElementById('navLinks');
  if (toggle && navLinks) {
    toggle.addEventListener('click', () => {
      navLinks.classList.toggle('active');
    });
  }
}

function showAlert(message) {
  alert(message);
}

function register() {
  const name = document.getElementById('name')?.value.trim();
  const email = document.getElementById('email')?.value.trim();
  const password = document.getElementById('password')?.value.trim();
  const role = document.getElementById('role')?.value;

  if (!name || !email || !password || !role) {
    return showAlert('Please fill in all fields.');
  }
  if (password.length < 6) {
    return showAlert('Password must be at least 6 characters.');
  }
  if (!validateEmail(email)) {
    return showAlert('Please enter a valid email address.');
  }

  fetch(`${API}/register`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `name=${encodeURIComponent(name)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}&role=${encodeURIComponent(role)}`
  })
  .then(parseApiResponse)
  .then(data => {
    if (data && data.message) {
      showAlert('Registration successful! Please login.');
      window.location.href = 'login.html';
    } else if (data && data.error) {
      showAlert(data.error);
    } else {
      showAlert('Registration failed. Please try again.');
    }
  })
  .catch(() => showAlert('Registration failed. Please try again.'));
}

function login() {
  const email = document.getElementById('email')?.value.trim();
  const password = document.getElementById('password')?.value.trim();

  if (!email || !password) {
    return showAlert('Please fill in all fields.');
  }

  fetch(`${API}/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
  })
  .then(parseApiResponse)
  .then(data => {
    if (data && data.role) {
      sessionStorage.setItem('email', data.email || email);
      sessionStorage.setItem('role', data.role);
      window.location.href = data.role === 'RECRUITER' ? 'recruiter-dashboard.html' : 'candidate-dashboard.html';
    } else if (data && data.error) {
      showAlert(data.error);
    } else {
      showAlert('Invalid email or password.');
    }
  })
  .catch(() => showAlert('Login failed. Please try again.'));
}

function applyJob(jobId, candidateEmail) {
  if (!candidateEmail) { return showAlert('Please login first to apply for jobs.'); }
  fetch(`${API}/apply`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `jobId=${jobId}&email=${encodeURIComponent(candidateEmail)}`
  })
  .then(parseApiResponse)
  .then(data => {
    if (data && data.message) {
      showAlert(data.message);
    } else if (data && data.error) {
      showAlert(data.error);
    } else {
      showAlert('Failed to apply. Please try again.');
    }
  })
  .catch(() => showAlert('Failed to apply. Please try again.'));
}

function postJob() {
  const title = document.getElementById('title')?.value.trim();
  const description = document.getElementById('description')?.value.trim();
  const salary = document.getElementById('salary')?.value.trim();
  const location = document.getElementById('location')?.value.trim();
  const company = document.getElementById('company')?.value.trim();
  const email = document.getElementById('email')?.value.trim() || sessionStorage.getItem('email');

  if (!title || !description || !salary || !location || !company || !email) {
    return showAlert('Please fill in all fields.');
  }
  if (isNaN(salary) || Number(salary) <= 0) {
    return showAlert('Please enter a valid salary.');
  }

  fetch(`${API}/postjob`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `title=${encodeURIComponent(title)}&description=${encodeURIComponent(description)}&salary=${salary}&location=${encodeURIComponent(location)}&company=${encodeURIComponent(company)}&email=${encodeURIComponent(email)}`
  })
  .then(parseApiResponse)
  .then(data => {
    if (data && data.message) {
      showAlert(data.message);
      ['title','description','salary','location','company','email'].forEach(id => {
        const element = document.getElementById(id);
        if (element) element.value = '';
      });
    } else if (data && data.error) {
      showAlert(data.error);
    } else {
      showAlert('Failed to post job.');
    }
  })
  .catch(() => showAlert('Failed to post job. Please try again.'));
}

function createJobCard(job) {
  return `
    <div class="card">
      <h3>${job.title}</h3>
      <p><span class="card-label">Company</span><br>${job.company}</p>
      <p><span class="card-label">Location</span><br>${job.location}</p>
      <p><span class="card-label">Salary</span><br></p>
      <div class="salary-chip">$${parseFloat(job.salary).toLocaleString()} / yr</div>
      <div style="margin-top:16px;">
        <button onclick="applyJob(${job.id}, '${sessionStorage.getItem('email') || ''}')">Apply Now</button>
      </div>
    </div>`;
}

function loadJobs(params = {}) {
  const container = document.getElementById('jobList');
  if (!container) return;
  container.innerHTML = '<p class="text-muted small-text" style="padding:20px;">Loading jobs...</p>';

  let url = `${API}/jobs`;
  const query = new URLSearchParams();
  if (params.keyword) query.append('keyword', params.keyword);
  if (params.location) query.append('location', params.location);
  if (params.minSalary) query.append('minSalary', params.minSalary);
  if (params.maxSalary) query.append('maxSalary', params.maxSalary);
  if ([...query].length) url = `${API}/search?${query.toString()}`;

  fetch(url)
    .then(parseApiResponse)
    .then(data => {
      if (!Array.isArray(data) || !data.length) {
        container.innerHTML = `<div class="empty-state"><div class="empty-icon">💼</div><p>No jobs available at the moment.</p></div>`;
        return;
      }
      container.innerHTML = '<div class="grid">' + data.map(createJobCard).join('') + '</div>';
    })
    .catch(() => { container.innerHTML = '<p style="color:#fca5a5;">Error loading jobs. Please try again.</p>'; });
}

function loadHomeJobs() {
  const container = document.getElementById('homeJobs');
  if (!container) return;
  container.innerHTML = '<p class="text-muted small-text" style="padding:20px;">Loading featured jobs...</p>';
  fetch(`${API}/jobs`)
    .then(parseApiResponse)
    .then(data => {
      if (!Array.isArray(data) || !data.length) {
        container.innerHTML = `<div class="empty-state"><div class="empty-icon">💼</div><p>No featured jobs available right now.</p></div>`;
        return;
      }
      const jobs = data.slice(0, 6);
      container.innerHTML = '<div class="job-grid">' + jobs.map(job => `
        <div class="job-card">
          <div class="job-label">${job.location || 'Remote'}</div>
          <h3>${job.title}</h3>
          <p>${job.company} · ${job.location}</p>
          <span class="salary-chip">৳${parseFloat(job.salary).toLocaleString()}</span>
        </div>`).join('') + '</div>';
    })
    .catch(() => {
      container.innerHTML = '<p style="color:#fca5a5;">Error loading featured jobs.</p>';
    });
}

function getQueryParams() {
  const params = new URLSearchParams(window.location.search);
  return {
    keyword: params.get('keyword') || '',
    location: params.get('location') || '',
    minSalary: params.get('minSalary') || '',
    maxSalary: params.get('maxSalary') || ''
  };
}

function searchJobs() {
  const keyword = document.getElementById('searchKeyword')?.value.trim() || '';
  const location = document.getElementById('filterLocation')?.value.trim() || '';
  const minSalary = document.getElementById('filterMinSalary')?.value.trim() || '';
  const maxSalary = document.getElementById('filterMaxSalary')?.value.trim() || '';

  loadJobs({ keyword, location, minSalary, maxSalary });
  const query = new URLSearchParams();
  if (keyword) query.append('keyword', keyword);
  if (location) query.append('location', location);
  if (minSalary) query.append('minSalary', minSalary);
  if (maxSalary) query.append('maxSalary', maxSalary);
  window.history.replaceState({}, '', `${window.location.pathname}?${query.toString()}`);
}

function resetJobSearch() {
  const keywordInput = document.getElementById('searchKeyword');
  const locationInput = document.getElementById('filterLocation');
  const minSalaryInput = document.getElementById('filterMinSalary');
  const maxSalaryInput = document.getElementById('filterMaxSalary');
  if (keywordInput) keywordInput.value = '';
  if (locationInput) locationInput.value = '';
  if (minSalaryInput) minSalaryInput.value = '';
  if (maxSalaryInput) maxSalaryInput.value = '';
  loadJobs();
  history.replaceState({}, '', window.location.pathname);
}

function loadApplicants() {
  const container = document.getElementById('list');
  if (!container) return;
  const email = sessionStorage.getItem('email');
  const role = sessionStorage.getItem('role');

  if (!email || role !== 'RECRUITER') {
    container.innerHTML = `<div class="empty-state"><div class="empty-icon">🚫</div><p>Please login as a recruiter to view applicants.</p></div>`;
    return;
  }

  container.innerHTML = '<p class="text-muted small-text" style="padding:20px;">Loading applicants...</p>';
  fetch(`${API}/applicants?email=${encodeURIComponent(email)}`)
    .then(parseApiResponse)
    .then(data => {
      if (!Array.isArray(data) || !data.length) {
        container.innerHTML = `<div class="empty-state"><div class="empty-icon">👤</div><p>No applicants yet.</p></div>`;
        return;
      }
      container.innerHTML = '<div class="grid">' + data.map(a => `
        <div class="card">
          <p><span class="card-label">Candidate</span><br><strong style="color:var(--text-1);">${a.email}</strong></p>
          <p><span class="card-label">Position</span><br>${a.job}</p>
          <p><span class="card-label">Company</span><br>${a.company}</p>
          <p><span class="card-label">Status</span><br><span class="status-badge status-${(a.status||'pending').toLowerCase()}">${a.status || 'PENDING'}</span></p>
          <p style="font-size:0.8rem;color:var(--text-3);margin-top:8px;">${new Date(a.appliedAt).toLocaleDateString()}</p>
        </div>`).join('') + '</div>';
    })
    .catch(() => { container.innerHTML = '<p style="color:#fca5a5;">Error loading applicants.</p>'; });
}

function logout() {
  sessionStorage.clear();
  window.location.href = 'login.html';
}

function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

window.addEventListener('DOMContentLoaded', setupNavbar);
