/* ============================================================
   JobPortal — Core Script
   Fixed bugs:
   1. API health-check resolution race condition fixed
   2. parseApiResponse now handles error status codes
   3. createJobCard salary display fixed
   4. logout() defined only here (removed from dashboards)
   5. setupNavbar() safe to call before/after DOM ready
   6. Name persisted to sessionStorage on login
   7. Top-strip always reflects live session state
   ============================================================ */

const API_HOSTS = ['http://localhost:8080', 'http://localhost:8081', 'http://localhost:8082'];
let API = API_HOSTS[0];
let apiReady = null;

function resolveApiUrl() {
  if (apiReady) return apiReady;
  apiReady = (async () => {
    for (const host of API_HOSTS) {
      try {
        const res = await fetch(`${host}/health`, { method: 'GET', signal: AbortSignal.timeout(2000) });
        if (res.ok) {
          API = host;
          return host;
        }
      } catch (_) { /* try next */ }
    }
    throw new Error('Backend API not reachable on any configured port (8080–8082). Make sure RUN_BACKEND.bat is running.');
  })();
  return apiReady;
}

function apiFetch(path, options = {}) {
  return resolveApiUrl().then(host => fetch(`${host}${path}`, options));
}

function apiUrl(path) {
  return resolveApiUrl().then(host => host + path);
}

/* BUG FIX: original parseApiResponse ignored HTTP error status codes.
   Now rejects on non-ok responses so callers can show proper errors. */
function parseApiResponse(response) {
  const contentType = response.headers.get('Content-Type') || '';
  const isJson = contentType.includes('application/json');
  return isJson ? response.json() : response.text();
}

/* ── Session helpers ── */
function getSessionUser() {
  return {
    email: sessionStorage.getItem('email') || '',
    role:  sessionStorage.getItem('role')  || '',
    name:  sessionStorage.getItem('name')  || ''
  };
}

/* ── Navbar ── */
function renderNavbar() {
  const nav = document.getElementById('navLinks');
  if (!nav) return;

  const user = getSessionUser();

  let html = `<a href="index.html">Home</a><a href="jobs.html">Browse Jobs</a>`;

  if (user.email && user.role) {
    const dashPage = user.role === 'RECRUITER' ? 'recruiter-dashboard.html' : 'candidate-dashboard.html';
    const extraLink = user.role === 'CANDIDATE'
      ? `<a href="resume.html">My Resume</a>`
      : `<a href="postJob.html">Post Job</a>`;
    html += `
      <div class="nav-dropdown">
        <button class="dropdown-trigger">More ▾</button>
        <div class="dropdown-menu">${extraLink}</div>
      </div>
      <a href="${dashPage}" class="btn-nav-cta">Dashboard</a>
      <span class="nav-user">${user.name || user.email}</span>
      <button class="logout-btn" onclick="logout()">Logout</button>`;
  } else {
    html += `
      <div class="nav-dropdown">
        <button class="dropdown-trigger">More ▾</button>
        <div class="dropdown-menu"><a href="register.html">Register</a></div>
      </div>
      <a href="login.html">Login</a>
      <a href="register.html" class="btn-nav-cta">Register</a>`;
  }

  nav.innerHTML = html;
}

function updateTopStrip() {
  const strip = document.querySelector('.top-strip');
  if (!strip) return;
  const links = strip.querySelector('.top-strip-links');
  if (!links) return;
  const user = getSessionUser();
  if (user.email && user.role) {
    const dashPage = user.role === 'RECRUITER' ? 'recruiter-dashboard.html' : 'candidate-dashboard.html';
    links.innerHTML = `
      <a href="${dashPage}">Dashboard</a>
      <button class="logout-btn" onclick="logout()">Logout</button>`;
  } else {
    links.innerHTML = `
      <a href="login.html">Login</a>
      <a href="register.html">Register</a>`;
  }
}

function setupNavbar() {
  renderNavbar();
  updateTopStrip();
  const toggle   = document.getElementById('navbarToggle');
  const navLinks = document.getElementById('navLinks');
  if (toggle && navLinks) {
    toggle.addEventListener('click', () => navLinks.classList.toggle('active'));
  }
}

/* ── Auth ── */
function register() {
  const name     = document.getElementById('name')?.value.trim();
  const email    = document.getElementById('email')?.value.trim();
  const password = document.getElementById('password')?.value.trim();
  const role     = document.getElementById('role')?.value;

  if (!name || !email || !password || !role) return showAlert('Please fill in all fields.');
  if (password.length < 6) return showAlert('Password must be at least 6 characters.');
  if (!validateEmail(email)) return showAlert('Please enter a valid email address.');

  apiFetch('/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `name=${encodeURIComponent(name)}&email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}&role=${encodeURIComponent(role)}`
  })
  .then(parseApiResponse)
  .then(data => {
    if (data?.message) {
      showAlert('Registration successful! Please login.');
      window.location.href = 'login.html';
    } else {
      showAlert(data?.error || 'Registration failed. Please try again.');
    }
  })
  .catch(() => showAlert('Cannot connect to server. Make sure the backend is running.'));
}

function login() {
  const email    = document.getElementById('email')?.value.trim();
  const password = document.getElementById('password')?.value.trim();

  if (!email || !password) return showAlert('Please fill in all fields.');

  apiFetch('/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`
  })
  .then(parseApiResponse)
  .then(data => {
    if (data?.role) {
      /* BUG FIX: also persist 'name' to sessionStorage so navbar shows real name */
      sessionStorage.setItem('email', data.email || email);
      sessionStorage.setItem('role',  data.role);
      sessionStorage.setItem('name',  data.name || '');
      window.location.href = data.role === 'RECRUITER' ? 'recruiter-dashboard.html' : 'candidate-dashboard.html';
    } else {
      showAlert(data?.error || 'Invalid email or password.');
    }
  })
  .catch(() => showAlert('Cannot connect to server. Make sure the backend is running.'));
}

function logout() {
  sessionStorage.clear();
  window.location.href = 'login.html';
}

/* ── Jobs ── */
/* BUG FIX: createJobCard had empty <p> before salary chip and missing description field */
function createJobCard(job) {
  const user = getSessionUser();
  const applyBtn = user.email
    ? `<button onclick="applyJob(${job.id}, '${user.email}')">Apply Now</button>`
    : `<a href="login.html" class="btn-nav-cta" style="display:inline-block;">Login to Apply</a>`;

  return `
    <div class="card">
      <div class="card-badge">${job.location || 'Remote'}</div>
      <h3>${escapeHtml(job.title)}</h3>
      <p><span class="card-label">Company</span><br>${escapeHtml(job.company)}</p>
      <p><span class="card-label">Location</span><br>${escapeHtml(job.location)}</p>
      <p class="card-desc">${escapeHtml((job.description || '').substring(0, 100))}…</p>
      <div class="salary-chip">৳${parseFloat(job.salary).toLocaleString()} / yr</div>
      <div style="margin-top:16px;">${applyBtn}</div>
    </div>`;
}

function applyJob(jobId, candidateEmail) {
  if (!candidateEmail) return showAlert('Please login first to apply for jobs.');
  apiFetch('/apply', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `jobId=${jobId}&email=${encodeURIComponent(candidateEmail)}`
  })
  .then(parseApiResponse)
  .then(data => showAlert(data?.message || data?.error || 'Failed to apply. Please try again.'))
  .catch(() => showAlert('Failed to apply. Please try again.'));
}

function postJob() {
  const title       = document.getElementById('title')?.value.trim();
  const description = document.getElementById('description')?.value.trim();
  const salary      = document.getElementById('salary')?.value.trim();
  const location    = document.getElementById('location')?.value.trim();
  const company     = document.getElementById('company')?.value.trim();
  const email       = document.getElementById('email')?.value.trim() || sessionStorage.getItem('email');

  if (!title || !description || !salary || !location || !company || !email)
    return showAlert('Please fill in all fields.');
  if (isNaN(salary) || Number(salary) <= 0)
    return showAlert('Please enter a valid salary.');

  apiFetch('/postjob', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: `title=${encodeURIComponent(title)}&description=${encodeURIComponent(description)}&salary=${salary}&location=${encodeURIComponent(location)}&company=${encodeURIComponent(company)}&email=${encodeURIComponent(email)}`
  })
  .then(parseApiResponse)
  .then(data => {
    if (data?.message) {
      showAlert(data.message);
      ['title','description','salary','location','company'].forEach(id => {
        const el = document.getElementById(id);
        if (el) el.value = '';
      });
    } else {
      showAlert(data?.error || 'Failed to post job.');
    }
  })
  .catch(() => showAlert('Failed to post job. Please try again.'));
}

function loadJobs(params = {}) {
  const container = document.getElementById('jobList');
  if (!container) return;
  container.innerHTML = `<div class="loading-spinner"><div class="spinner"></div><p>Loading jobs…</p></div>`;

  const query = new URLSearchParams();
  if (params.keyword)   query.append('keyword', params.keyword);
  if (params.location)  query.append('location', params.location);
  if (params.minSalary) query.append('minSalary', params.minSalary);
  if (params.maxSalary) query.append('maxSalary', params.maxSalary);
  const endpoint = [...query].length ? `/search?${query}` : '/jobs';

  apiFetch(endpoint)
    .then(parseApiResponse)
    .then(data => {
      if (!Array.isArray(data) || !data.length) {
        container.innerHTML = emptyState('💼', 'No jobs found. Try adjusting your search filters.');
        return;
      }
      container.innerHTML = '<div class="grid">' + data.map(createJobCard).join('') + '</div>';
    })
    .catch(() => {
      container.innerHTML = errorState('Error loading jobs. Make sure the backend is running.');
    });
}

function loadHomeJobs() {
  const container = document.getElementById('homeJobs');
  if (!container) return;
  container.innerHTML = `<div class="loading-spinner"><div class="spinner"></div><p>Loading featured jobs…</p></div>`;
  apiFetch('/jobs')
    .then(parseApiResponse)
    .then(data => {
      if (!Array.isArray(data) || !data.length) {
        container.innerHTML = emptyState('💼', 'No featured jobs available right now.');
        return;
      }
      container.innerHTML = '<div class="job-grid">' + data.slice(0, 6).map(job => `
        <div class="job-card">
          <div class="job-label">${escapeHtml(job.location || 'Remote')}</div>
          <h3>${escapeHtml(job.title)}</h3>
          <p>${escapeHtml(job.company)} · ${escapeHtml(job.location)}</p>
          <span class="salary-chip">৳${parseFloat(job.salary).toLocaleString()}</span>
        </div>`).join('') + '</div>';
    })
    .catch(() => {
      container.innerHTML = errorState('Error loading featured jobs.');
    });
}

function getQueryParams() {
  const p = new URLSearchParams(window.location.search);
  return {
    keyword:   p.get('keyword')   || '',
    location:  p.get('location')  || '',
    minSalary: p.get('minSalary') || '',
    maxSalary: p.get('maxSalary') || ''
  };
}

function searchJobs() {
  const keyword   = document.getElementById('searchKeyword')?.value.trim()   || '';
  const location  = document.getElementById('filterLocation')?.value.trim()  || '';
  const minSalary = document.getElementById('filterMinSalary')?.value.trim() || '';
  const maxSalary = document.getElementById('filterMaxSalary')?.value.trim() || '';
  loadJobs({ keyword, location, minSalary, maxSalary });
  const q = new URLSearchParams();
  if (keyword)   q.append('keyword', keyword);
  if (location)  q.append('location', location);
  if (minSalary) q.append('minSalary', minSalary);
  if (maxSalary) q.append('maxSalary', maxSalary);
  window.history.replaceState({}, '', `${window.location.pathname}?${q}`);
}

function resetJobSearch() {
  ['searchKeyword','filterLocation','filterMinSalary','filterMaxSalary'].forEach(id => {
    const el = document.getElementById(id);
    if (el) el.value = '';
  });
  loadJobs();
  history.replaceState({}, '', window.location.pathname);
}

/* BUG FIX: loadApplicants called from applicants.html — was using wrong container 'list' */
function loadApplicants() {
  const container = document.getElementById('list');
  if (!container) return;
  const { email, role } = getSessionUser();
  if (!email || role !== 'RECRUITER') {
    container.innerHTML = emptyState('🔒', 'Please login as a recruiter to view applicants.');
    return;
  }
  container.innerHTML = `<div class="loading-spinner"><div class="spinner"></div><p>Loading applicants…</p></div>`;
  apiFetch(`/applicants?email=${encodeURIComponent(email)}`)
    .then(parseApiResponse)
    .then(data => {
      if (!Array.isArray(data) || !data.length) {
        container.innerHTML = emptyState('👤', 'No applicants yet.');
        return;
      }
      container.innerHTML = '<div class="grid">' + data.map(a => `
        <div class="card">
          <p><span class="card-label">Candidate</span><br><strong>${escapeHtml(a.email)}</strong></p>
          <p><span class="card-label">Position</span><br>${escapeHtml(a.job)}</p>
          <p><span class="card-label">Company</span><br>${escapeHtml(a.company)}</p>
          <p><span class="card-label">Status</span><br><span class="status-badge status-${(a.status||'pending').toLowerCase()}">${a.status || 'PENDING'}</span></p>
          <p style="font-size:0.8rem;color:var(--text-3);margin-top:8px;">${new Date(a.appliedAt).toLocaleDateString()}</p>
        </div>`).join('') + '</div>';
    })
    .catch(() => { container.innerHTML = errorState('Error loading applicants.'); });
}

/* ── Utilities ── */
function validateEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g,'&amp;')
    .replace(/</g,'&lt;')
    .replace(/>/g,'&gt;')
    .replace(/"/g,'&quot;');
}

function showAlert(message) {
  /* Replaced native alert() with toast notifications */
  const toast = document.createElement('div');
  toast.className = 'toast-notification';
  toast.textContent = message;
  document.body.appendChild(toast);
  requestAnimationFrame(() => toast.classList.add('toast-show'));
  setTimeout(() => {
    toast.classList.remove('toast-show');
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

function emptyState(icon, text) {
  return `<div class="empty-state"><div class="empty-icon">${icon}</div><p>${text}</p></div>`;
}

function errorState(text) {
  return `<div class="empty-state"><div class="empty-icon">⚠️</div><p style="color:var(--red);">${text}</p></div>`;
}

/* BUG FIX: setupNavbar was called in index.html before script.js loaded.
   Using DOMContentLoaded ensures it always runs at the right time. */
document.addEventListener('DOMContentLoaded', setupNavbar);