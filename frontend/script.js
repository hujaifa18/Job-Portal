/* ============================================================
   JobPortal — Core Script v4  "Obsidian"
   Full sidebar on every page, Ctrl+K search, backend status,
   role-aware nav, skeleton loading, staggered animations
   ============================================================ */

/* ── API ── */
const API_HOSTS = ['http://localhost:8080','http://localhost:8081','http://localhost:8082'];
let API = API_HOSTS[0], _apiReady = null;

function resolveApi() {
  if (_apiReady) return _apiReady;
  _apiReady = (async () => {
    for (const h of API_HOSTS) {
      try {
        const r = await fetch(`${h}/health`, { signal: AbortSignal.timeout(2000) });
        if (r.ok) { API = h; return h; }
      } catch(_) {}
    }
    throw new Error('Backend offline');
  })();
  return _apiReady;
}
const apiFetch = (path, opts={}) => resolveApi().then(h => fetch(h+path, opts));
const apiUrl   = path => resolveApi().then(h => h+path);
const parseResp = r => {
  const ct = r.headers.get('Content-Type')||'';
  return ct.includes('application/json') ? r.json() : r.text();
};

/* ── Session ── */
const getUser = () => ({
  email: sessionStorage.getItem('email')||'',
  role:  sessionStorage.getItem('role') ||'',
  name:  sessionStorage.getItem('name') ||''
});

/* ================================================================
   SIDEBAR  — builds itself on every page from getUser() state
   ================================================================ */
function buildSidebar(activePage) {
  const sb = document.getElementById('sidebar');
  if (!sb) return;
  const u = getUser();
  const isC = u.role === 'CANDIDATE';
  const isR = u.role === 'RECRUITER';

  sb.innerHTML = `
    <!-- Brand -->
    <div class="sb-brand">
      <div class="sb-logo">💼</div>
      <div class="sb-brand-text">
        <strong>JobPortal</strong>
        <span>Career Platform</span>
      </div>
    </div>

    <!-- Quick search -->
    <div class="sb-search">
      <div class="sb-search-inner" id="sbSearchWrap">
        <span class="sb-search-icon">⌕</span>
        <input id="sbQ" type="text" placeholder="Search jobs…" autocomplete="off">
        <span class="sb-shortcut">⌘K</span>
      </div>
    </div>

    <!-- MAIN -->
    <div class="sb-section">Main</div>
    ${ni('🏠','Home',        'index.html',    activePage==='home')}
    ${ni('🔍','Browse Jobs', 'jobs.html',     activePage==='jobs')}

    ${!u.email ? `
      <div class="sb-section">Account</div>
      ${ni('🔑','Login',    'login.html',    activePage==='login')}
      ${ni('📝','Register', 'register.html', activePage==='register')}
    ` : ''}

    ${isC ? `
      <div class="sb-section">Candidate</div>
      ${ni('📊','Dashboard',  'candidate-dashboard.html', activePage==='dashboard')}
      ${ni('📄','My Resume',  'resume.html',              activePage==='resume')}
    ` : ''}

    ${isR ? `
      <div class="sb-section">Recruiter</div>
      ${ni('📊','Dashboard',  'recruiter-dashboard.html', activePage==='dashboard')}
      ${ni('➕','Post Job',   'postJob.html',             activePage==='postjob')}
      ${ni('👥','Applicants', 'applicants.html',          activePage==='applicants')}
    ` : ''}

    <div class="sb-divider"></div>
    <div class="sb-section">Info</div>
    ${ni('💡','How It Works','index.html#how', false)}
    ${ni('❓','Help',        'index.html#help',false)}

    <!-- Footer -->
    <div class="sb-pinned">
      <div class="sb-status">
        <div class="sb-dot checking" id="sbDot"></div>
        <span id="sbStatusLabel">Connecting…</span>
      </div>
      ${u.email ? `
        <div class="sb-user">
          <div class="sb-avatar">${(u.name||u.email).charAt(0).toUpperCase()}</div>
          <div class="sb-user-info">
            <strong>${esc(u.name||u.email)}</strong>
            <span>${u.role}</span>
          </div>
          <button class="sb-logout" onclick="logout()" title="Logout">⏏</button>
        </div>
      ` : `
        <div class="sb-user" onclick="location='login.html'" style="cursor:pointer">
          <div class="sb-avatar" style="background:rgba(255,255,255,0.07);font-size:1rem">👤</div>
          <div class="sb-user-info">
            <strong style="color:var(--sb-txt)">Not logged in</strong>
            <span>Click to login</span>
          </div>
        </div>
      `}
    </div>`;

  /* Mobile overlay */
  let ov = document.getElementById('sbOv');
  if (!ov) {
    ov = Object.assign(document.createElement('div'), {id:'sbOv',className:'sb-overlay'});
    ov.onclick = closeSb;
    document.body.appendChild(ov);
  }
  const mbtn = document.getElementById('mobileMenuBtn');
  if (mbtn) mbtn.onclick = () => { sb.classList.toggle('open'); ov.classList.toggle('show'); };

  /* Quick search — Enter → jobs.html?keyword=… */
  const qi = document.getElementById('sbQ');
  if (qi) {
    qi.addEventListener('keydown', e => {
      if (e.key==='Enter' && qi.value.trim())
        location.href = `jobs.html?keyword=${encodeURIComponent(qi.value.trim())}`;
    });
  }

  /* Ctrl/Cmd+K */
  document.addEventListener('keydown', e => {
    if ((e.ctrlKey||e.metaKey) && e.key==='k') {
      e.preventDefault();
      const i = document.getElementById('sbQ');
      if (i) { i.focus(); i.select(); }
    }
  });

  /* Escape closes mobile sidebar */
  document.addEventListener('keydown', e => { if (e.key==='Escape') closeSb(); });

  pingBackend();
}

function ni(icon, label, href, active) {
  return `<div class="sb-item${active?' active':''}">
    <a href="${href}"><span class="sb-icon">${icon}</span><span class="sb-label">${label}</span></a>
  </div>`;
}
function closeSb() {
  document.getElementById('sidebar')?.classList.remove('open');
  document.getElementById('sbOv')?.classList.remove('show');
}

function pingBackend() {
  const dot = document.getElementById('sbDot');
  const lbl = document.getElementById('sbStatusLabel');
  if (!dot||!lbl) return;
  resolveApi()
    .then(()  => { dot.className='sb-dot online';  lbl.textContent='Backend online'; })
    .catch(()  => { dot.className='sb-dot offline'; lbl.textContent='Backend offline'; });
}

/* ── Auth ── */
function register() {
  const name=v('name'),email=v('email'),password=v('password'),role=v('role');
  if (!name||!email||!password||!role) return toast('Please fill in all fields.');
  if (password.length<6) return toast('Password must be at least 6 characters.');
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) return toast('Enter a valid email.');
  apiFetch('/register',{
    method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},
    body:`name=${enc(name)}&email=${enc(email)}&password=${enc(password)}&role=${enc(role)}`
  }).then(parseResp).then(d => {
    if (d?.message) { toast('Account created! Redirecting…','success'); setTimeout(()=>location.href='login.html',1100); }
    else toast(d?.error||'Registration failed.','error');
  }).catch(()=>toast('Cannot reach server.','error'));
}

function login() {
  const email=v('email'),password=v('password');
  if (!email||!password) return toast('Please fill in all fields.');
  apiFetch('/login',{
    method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},
    body:`email=${enc(email)}&password=${enc(password)}`
  }).then(parseResp).then(d => {
    if (d?.role) {
      sessionStorage.setItem('email',d.email||email);
      sessionStorage.setItem('role', d.role);
      sessionStorage.setItem('name', d.name||'');
      location.href = d.role==='RECRUITER'?'recruiter-dashboard.html':'candidate-dashboard.html';
    } else toast(d?.error||'Invalid credentials.','error');
  }).catch(()=>toast('Cannot reach server.','error'));
}

function logout() { sessionStorage.clear(); location.href='login.html'; }

/* ── Jobs ── */
function createJobCard(job) {
  const u = getUser();
  const salary = parseFloat(job.salary||0).toLocaleString();
  const applyBtn = u.email
    ? `<button class="btn-sm" onclick="applyJob(${job.id},'${u.email}')">Apply Now</button>`
    : `<a href="login.html" class="btn-sm" style="display:inline-flex;align-items:center;padding:7px 14px;background:var(--gold);color:#0a0d14;border-radius:var(--r-sm);font-weight:700;font-size:.79rem;gap:4px;">Login to Apply</a>`;
  return `
    <div class="job-card">
      <div class="job-card-head">
        <div>
          <div class="job-card-title">${esc(job.title)}</div>
          <div class="job-card-company">${esc(job.company)}</div>
        </div>
        <span class="chip chip-gold">৳${salary}</span>
      </div>
      <div class="job-card-meta">
        <span class="chip chip-gray">📍 ${esc(job.location)}</span>
        <span class="chip chip-teal">Full-time</span>
      </div>
      <p class="text-sm text-muted" style="line-height:1.55">${esc((job.description||'').substring(0,110))}…</p>
      <div class="job-card-footer">
        <span class="text-muted text-sm">${job.applicantCount||0} applicant${job.applicantCount==1?'':'s'}</span>
        ${applyBtn}
      </div>
    </div>`;
}

function applyJob(jobId, email) {
  apiFetch('/apply',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},
    body:`jobId=${jobId}&email=${enc(email)}`})
  .then(parseResp).then(d=>toast(d?.message||d?.error||'Failed.', d?.message?'success':'error'))
  .catch(()=>toast('Failed to apply.','error'));
}

function loadJobs(params={}) {
  const c=document.getElementById('jobList'); if(!c) return;
  showSkeleton(c,6);
  const q=new URLSearchParams();
  if(params.keyword)   q.append('keyword',params.keyword);
  if(params.location)  q.append('location',params.location);
  if(params.minSalary) q.append('minSalary',params.minSalary);
  if(params.maxSalary) q.append('maxSalary',params.maxSalary);
  apiFetch([...q].length?`/search?${q}`:'/jobs').then(parseResp).then(data=>{
    if(!Array.isArray(data)||!data.length){c.innerHTML=empty('💼','No jobs found. Adjust your filters.');return;}
    c.innerHTML=`<div class="grid-3">${data.map(createJobCard).join('')}</div>`;
  }).catch(()=>{c.innerHTML=empty('⚠️','Could not load jobs. Is the backend running?');});
}

function searchJobs() {
  loadJobs({keyword:v('searchKeyword'),location:v('filterLocation'),minSalary:v('filterMinSalary'),maxSalary:v('filterMaxSalary')});
}
function resetSearch() {
  ['searchKeyword','filterLocation','filterMinSalary','filterMaxSalary'].forEach(id=>{const e=document.getElementById(id);if(e)e.value='';});
  loadJobs();
}

function postJob() {
  const title=v('title'),desc=v('description'),salary=v('salary'),location=v('location'),company=v('company');
  const email=v('email')||getUser().email;
  if(!title||!desc||!salary||!location||!company||!email) return toast('Please fill in all fields.');
  if(isNaN(salary)||Number(salary)<=0) return toast('Enter a valid salary.');
  apiFetch('/postjob',{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},
    body:`title=${enc(title)}&description=${enc(desc)}&salary=${salary}&location=${enc(location)}&company=${enc(company)}&email=${enc(email)}`})
  .then(parseResp).then(d=>{
    if(d?.message){
      toast(d.message,'success');
      ['title','description','salary','location','company'].forEach(id=>{const e=document.getElementById(id);if(e)e.value='';});
    } else toast(d?.error||'Failed.','error');
  }).catch(()=>toast('Failed.','error'));
}

function loadApplicants() {
  const c=document.getElementById('list'); if(!c) return;
  const {email,role}=getUser();
  if(!email||role!=='RECRUITER'){c.innerHTML=empty('🔒','Login as recruiter to view applicants.');return;}
  showSkeleton(c,4);
  apiFetch(`/applicants?email=${enc(email)}`).then(parseResp).then(data=>{
    if(!Array.isArray(data)||!data.length){c.innerHTML=empty('👤','No applicants yet.');return;}
    c.innerHTML=`<div class="grid-3">${data.map(a=>`
      <div class="card">
        <div class="text-sm text-muted" style="margin-bottom:3px;letter-spacing:.06em;font-size:.67rem;text-transform:uppercase;">Candidate</div>
        <strong style="display:block;margin-bottom:2px">${esc(a.candidateName||a.email)}</strong>
        <div class="text-sm text-muted">${esc(a.email)}</div>
        <hr class="divider">
        <div class="text-sm"><span class="text-muted">Position</span><br><strong>${esc(a.job)}</strong></div>
        <div class="flex mt-3">
          <span class="badge badge-${(a.status||'pending').toLowerCase()}">${a.status||'PENDING'}</span>
          <span class="text-sm text-muted">${new Date(a.appliedAt).toLocaleDateString()}</span>
        </div>
      </div>`).join('')}</div>`;
  }).catch(()=>{c.innerHTML=empty('⚠️','Error loading applicants.');});
}

/* ── Utilities ── */
const v   = id => document.getElementById(id)?.value.trim()||'';
const enc = s  => encodeURIComponent(s);
const esc = s  => String(s||'').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/"/g,'&quot;');

function toast(msg, type='default') {
  const t=document.createElement('div');
  t.className='toast';
  const colors={success:'var(--green)',error:'var(--red)',default:'var(--gold)'};
  t.style.borderLeftColor=colors[type]||colors.default;
  t.innerHTML=`${type==='success'?'✓ ':type==='error'?'✕ ':''}${msg}`;
  document.body.appendChild(t);
  requestAnimationFrame(()=>t.classList.add('show'));
  setTimeout(()=>{t.classList.remove('show');setTimeout(()=>t.remove(),300);},3600);
}

function showSkeleton(el, n=3) {
  el.innerHTML=`<div class="grid-3">${Array(n).fill(`
    <div class="job-card" style="gap:14px">
      <div class="skeleton" style="height:20px;width:65%"></div>
      <div class="skeleton" style="height:14px;width:40%"></div>
      <div class="skeleton" style="height:12px;width:80%"></div>
      <div class="skeleton" style="height:36px"></div>
    </div>`).join('')}</div>`;
}

function showLoading(el) {
  el.innerHTML=`<div class="spinner-wrap"><div class="spinner"></div><p>Loading…</p></div>`;
}

function empty(icon,text) {
  return `<div class="empty-state"><div class="icon">${icon}</div><p>${text}</p></div>`;
}

function getQueryParams() {
  const p=new URLSearchParams(window.location.search);
  return {keyword:p.get('keyword')||'',location:p.get('location')||'',minSalary:p.get('minSalary')||'',maxSalary:p.get('maxSalary')||''};
}
