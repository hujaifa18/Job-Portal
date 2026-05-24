package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Filter;

import dao.ApplicationDAO;
import dao.JobDAO;
import dao.ResumesDAO;
import dao.UserDAO;
import service.ApplicationService;
import service.JobService;
import service.UserService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;

public class Server {

    // ========================= CORS FILTER =========================
    static class CorsFilter extends Filter {

        @Override
        public String description() { return "Adds CORS headers"; }

        @Override
        public void doFilter(HttpExchange ex, Chain chain) throws IOException {
            ex.getResponseHeaders().add("Access-Control-Allow-Origin",  "*");
            ex.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            ex.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if ("OPTIONS".equalsIgnoreCase(ex.getRequestMethod())) {
                ex.sendResponseHeaders(204, -1);
                return;
            }
            chain.doFilter(ex);
        }
    }

    // ========================= MAIN =========================
    public static void main(String[] args) throws Exception {

        HttpServer server = createServer();
        // Thread pool so concurrent requests don't block each other
        server.setExecutor(Executors.newFixedThreadPool(20));

        // ==================== REGISTER ====================
        server.createContext("/register", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("POST")) { send405(ex); return; }

            try {
                Map<String, String> p = parseBody(ex);

                String name     = p.getOrDefault("name", "").trim();
                String email    = p.getOrDefault("email", "").trim();
                String password = p.getOrDefault("password", "");
                String role     = p.getOrDefault("role", "").trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"All fields are required\"}");
                    return;
                }

                UserService svc = new UserService();

                if (new UserDAO().emailExists(email)) {
                    sendJson(ex, 409, "{\"error\":\"Email already registered\"}");
                    return;
                }

                boolean ok = svc.register(name, email, password, role);
                sendJson(ex, ok ? 200 : 500, ok
                        ? "{\"message\":\"Registered successfully\"}"
                        : "{\"error\":\"Registration failed\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== LOGIN ====================
        server.createContext("/login", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("POST")) { send405(ex); return; }

            try {
                Map<String, String> p = parseBody(ex);

                String email    = p.getOrDefault("email", "").trim();
                String password = p.getOrDefault("password", "");

                if (email.isEmpty() || password.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"Email and password required\"}");
                    return;
                }

                String role = new UserDAO().login(email, password);

                if (role != null) {
                    Map<String, String> user = new UserDAO().getUserByEmail(email);
                    String name = user != null ? escape(user.get("name")) : "";
                    sendJson(ex, 200, "{\"role\":\"" + role + "\",\"email\":\"" + escape(email) + "\",\"name\":\"" + name + "\"}");
                } else {
                    sendJson(ex, 401, "{\"error\":\"Invalid email or password\"}");
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== GET PROFILE ====================
        server.createContext("/profile", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("GET")) { send405(ex); return; }

            try {
                String email = getQueryParam(ex, "email");

                if (email == null || email.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"Email required\"}");
                    return;
                }

                Map<String, String> user = new UserDAO().getUserByEmail(email);

                if (user == null) {
                    sendJson(ex, 404, "{\"error\":\"User not found\"}");
                } else {
                    sendJson(ex, 200, "{" +
                            "\"id\":\""    + escape(user.get("id"))    + "\"," +
                            "\"name\":\""  + escape(user.get("name"))  + "\"," +
                            "\"email\":\"" + escape(user.get("email")) + "\"," +
                            "\"role\":\""  + escape(user.get("role"))  + "\"" +
                            "}");
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== UPDATE PROFILE ====================
        server.createContext("/update-profile", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("POST")) { send405(ex); return; }

            try {
                Map<String, String> p = parseBody(ex);

                String email    = p.getOrDefault("email", "").trim();
                String name     = p.getOrDefault("name", "").trim();
                String password = p.getOrDefault("password", ""); // optional

                if (email.isEmpty() || name.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"Email and name required\"}");
                    return;
                }

                boolean ok = new UserService().updateProfile(email, name, password.isEmpty() ? null : password);
                sendJson(ex, ok ? 200 : 500, ok
                        ? "{\"message\":\"Profile updated\"}"
                        : "{\"error\":\"Update failed\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== POST JOB ====================
        server.createContext("/postjob", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("POST")) { send405(ex); return; }

            try {
                Map<String, String> p = parseBody(ex);

                String title    = p.getOrDefault("title", "").trim();
                String desc     = p.getOrDefault("description", "");
                String salaryS  = p.getOrDefault("salary", "0");
                String location = p.getOrDefault("location", "").trim();
                String company  = p.getOrDefault("company", "").trim();
                String email    = p.getOrDefault("email", "").trim();

                if (title.isEmpty() || email.isEmpty() || company.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"title, company and email are required\"}");
                    return;
                }

                double salary;
                try { salary = Double.parseDouble(salaryS); } catch (NumberFormatException e) { salary = 0; }

                boolean ok = new JobService().postJob(title, desc, salary, location, company, email);
                sendJson(ex, ok ? 200 : 500, ok
                        ? "{\"message\":\"Job posted\"}"
                        : "{\"error\":\"Failed to post job\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== UPDATE JOB ====================
        server.createContext("/update-job", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("POST")) { send405(ex); return; }

            try {
                Map<String, String> p = parseBody(ex);

                int    jobId    = intParam(p, "jobId");
                String title    = p.getOrDefault("title", "").trim();
                String desc     = p.getOrDefault("description", "");
                double salary   = doubleParam(p, "salary");
                String location = p.getOrDefault("location", "");
                String company  = p.getOrDefault("company", "");
                String email    = p.getOrDefault("email", "").trim();

                if (jobId <= 0 || title.isEmpty() || email.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"jobId, title and email are required\"}");
                    return;
                }

                boolean ok = new JobService().updateJob(jobId, title, desc, salary, location, company, email);
                sendJson(ex, ok ? 200 : 500, ok
                        ? "{\"message\":\"Job updated\"}"
                        : "{\"error\":\"Update failed — job not found or not yours\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== DELETE JOB ====================
        server.createContext("/delete-job", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("POST")) { send405(ex); return; }

            try {
                Map<String, String> p = parseBody(ex);

                int    jobId = intParam(p, "jobId");
                String email = p.getOrDefault("email", "").trim();

                if (jobId <= 0 || email.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"jobId and email required\"}");
                    return;
                }

                boolean ok = new JobService().deleteJob(jobId, email);
                sendJson(ex, ok ? 200 : 500, ok
                        ? "{\"message\":\"Job deleted\"}"
                        : "{\"error\":\"Delete failed — job not found or not yours\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== GET ALL JOBS ====================
        server.createContext("/jobs", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("GET")) { send405(ex); return; }

            try {
                List<Map<String, String>> jobs = new JobDAO().getAllJobs();
                sendJson(ex, 200, jobsToJson(jobs));

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== GET SINGLE JOB ====================
        server.createContext("/job", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("GET")) { send405(ex); return; }

            try {
                String idStr = getQueryParam(ex, "id");

                if (idStr == null) { sendJson(ex, 400, "{\"error\":\"id required\"}"); return; }

                Map<String, String> job = new JobDAO().getJobById(Integer.parseInt(idStr));

                if (job == null) {
                    sendJson(ex, 404, "{\"error\":\"Job not found\"}");
                } else {
                    sendJson(ex, 200, jobToJson(job));
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== SEARCH + FILTER JOBS ====================
        server.createContext("/search", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("GET")) { send405(ex); return; }

            try {
                Map<String, String> q = parseQueryString(ex);

                String keyword  = q.get("keyword");
                String location = q.get("location");

                Double minSalary = null;
                Double maxSalary = null;

                if (q.containsKey("minSalary") && !q.get("minSalary").isEmpty()) {
                    try { minSalary = Double.parseDouble(q.get("minSalary")); } catch (NumberFormatException ignored) {}
                }
                if (q.containsKey("maxSalary") && !q.get("maxSalary").isEmpty()) {
                    try { maxSalary = Double.parseDouble(q.get("maxSalary")); } catch (NumberFormatException ignored) {}
                }

                List<Map<String, String>> jobs = new JobDAO().searchAndFilter(keyword, location, minSalary, maxSalary);
                sendJson(ex, 200, jobsToJson(jobs));

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== GET RECRUITER'S JOBS ====================
        server.createContext("/recruiter-jobs", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("GET")) { send405(ex); return; }

            try {
                String email = getQueryParam(ex, "email");

                if (email == null || email.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"email required\"}");
                    return;
                }

                List<Map<String, String>> jobs = new JobDAO().getJobsByRecruiter(email);
                sendJson(ex, 200, jobsToJson(jobs));

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== APPLY FOR JOB ====================
        server.createContext("/apply", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("POST")) { send405(ex); return; }

            try {
                Map<String, String> p = parseBody(ex);

                int    jobId = intParam(p, "jobId");
                String email = p.getOrDefault("email", "").trim();

                if (jobId <= 0 || email.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"jobId and email required\"}");
                    return;
                }

                ApplicationService svc = new ApplicationService();

                if (svc.alreadyApplied(jobId, email)) {
                    sendJson(ex, 409, "{\"error\":\"Already applied to this job\"}");
                    return;
                }

                boolean ok = svc.applyJob(jobId, email);
                sendJson(ex, ok ? 200 : 500, ok
                        ? "{\"message\":\"Applied successfully\"}"
                        : "{\"error\":\"Application failed\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== WITHDRAW APPLICATION ====================
        server.createContext("/withdraw", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("POST")) { send405(ex); return; }

            try {
                Map<String, String> p = parseBody(ex);

                int    appId = intParam(p, "applicationId");
                String email = p.getOrDefault("email", "").trim();

                if (appId <= 0 || email.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"applicationId and email required\"}");
                    return;
                }

                boolean ok = new ApplicationService().withdraw(appId, email);
                sendJson(ex, ok ? 200 : 500, ok
                        ? "{\"message\":\"Application withdrawn\"}"
                        : "{\"error\":\"Withdraw failed\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== MY APPLICATIONS (candidate) ====================
        server.createContext("/my-applications", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("GET")) { send405(ex); return; }

            try {
                String email = getQueryParam(ex, "email");

                if (email == null || email.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"email required\"}");
                    return;
                }

                List<Map<String, String>> apps = new ApplicationDAO().getApplicationsByCandidate(email);

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < apps.size(); i++) {
                    Map<String, String> a = apps.get(i);
                    json.append("{")
                        .append("\"id\":\"").append(escape(a.get("id"))).append("\",")
                        .append("\"jobId\":\"").append(escape(a.get("jobId"))).append("\",")
                        .append("\"job\":\"").append(escape(a.get("job"))).append("\",")
                        .append("\"company\":\"").append(escape(a.get("company"))).append("\",")
                        .append("\"status\":\"").append(escape(a.get("status"))).append("\",")
                        .append("\"appliedAt\":\"").append(escape(a.get("appliedAt"))).append("\"")
                        .append("}");
                    if (i < apps.size() - 1) json.append(",");
                }
                json.append("]");

                sendJson(ex, 200, json.toString());

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== APPLICANTS (recruiter view) ====================
        server.createContext("/applicants", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("GET")) { send405(ex); return; }

            try {
                String recruiterEmail = getQueryParam(ex, "email");

                if (recruiterEmail == null || recruiterEmail.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"Recruiter email required\"}");
                    return;
                }

                List<Map<String, String>> apps = new ApplicationDAO().getApplicantsForRecruiter(recruiterEmail);

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < apps.size(); i++) {
                    Map<String, String> a = apps.get(i);
                    json.append("{")
                        .append("\"id\":\"").append(escape(a.get("id"))).append("\",")
                        .append("\"email\":\"").append(escape(a.get("email"))).append("\",")
                        .append("\"candidateName\":\"").append(escape(a.get("candidateName"))).append("\",")
                        .append("\"job\":\"").append(escape(a.get("job"))).append("\",")
                        .append("\"company\":\"").append(escape(a.get("company"))).append("\",")
                        .append("\"status\":\"").append(escape(a.get("status"))).append("\",")
                        .append("\"appliedAt\":\"").append(escape(a.get("appliedAt"))).append("\"")
                        .append("}");
                    if (i < apps.size() - 1) json.append(",");
                }
                json.append("]");

                sendJson(ex, 200, json.toString());

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== UPDATE APPLICATION STATUS ====================
        server.createContext("/update-status", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("POST")) { send405(ex); return; }

            try {
                Map<String, String> p = parseBody(ex);

                int    appId         = intParam(p, "applicationId");
                String status        = p.getOrDefault("status", "").trim();
                String recruiterEmail = p.getOrDefault("recruiterEmail", "").trim();

                if (appId <= 0 || status.isEmpty() || recruiterEmail.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"applicationId, status and recruiterEmail required\"}");
                    return;
                }

                boolean ok = new ApplicationService().updateStatus(appId, status, recruiterEmail);
                sendJson(ex, ok ? 200 : 400, ok
                        ? "{\"message\":\"Status updated\"}"
                        : "{\"error\":\"Update failed — invalid status or not authorised\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== UPLOAD RESUME ====================
        server.createContext("/upload-resume", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("POST")) { send405(ex); return; }

            try {
                String contentType = ex.getRequestHeaders().getFirst("Content-Type");

                if (contentType == null || !contentType.contains("multipart/form-data")) {
                    sendJson(ex, 400, "{\"error\":\"Must be multipart/form-data\"}");
                    return;
                }

                String boundary = contentType.split("boundary=")[1].trim();

                // Read raw bytes to avoid corrupting binary PDF data
                byte[] rawBody = ex.getRequestBody().readAllBytes();

                String email    = extractFieldFromBytes(rawBody, "email", boundary);
                String fileName = extractFileNameFromBytes(rawBody, boundary);
                byte[] fileData = extractFileDataFromBytes(rawBody, "file", boundary);

                if (email == null || fileData == null || fileName == null) {
                    sendJson(ex, 400, "{\"error\":\"email, file and filename are required\"}");
                    return;
                }

                // Validate file type — only PDF allowed
                if (!fileName.toLowerCase().endsWith(".pdf")) {
                    sendJson(ex, 400, "{\"error\":\"Only PDF files are allowed\"}");
                    return;
                }

                // Max 5 MB
                if (fileData.length > 5 * 1024 * 1024) {
                    sendJson(ex, 400, "{\"error\":\"File exceeds 5MB limit\"}");
                    return;
                }

                String uploadDir = "uploads/";
                new File(uploadDir).mkdirs();

                String filePath = uploadDir + System.currentTimeMillis() + "_" + fileName;
                Files.write(Paths.get(filePath), fileData);

                boolean ok = new ResumesDAO().uploadResume(email, fileName, filePath, fileData.length);
                sendJson(ex, ok ? 200 : 500, ok
                        ? "{\"message\":\"Resume uploaded\"}"
                        : "{\"error\":\"Failed to save resume\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== GET RESUMES ====================
        server.createContext("/resumes", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("GET")) { send405(ex); return; }

            try {
                String email = getQueryParam(ex, "email");

                if (email == null || email.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"email required\"}");
                    return;
                }

                List<Map<String, Object>> resumes = new ResumesDAO().getResumesByEmail(email);

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < resumes.size(); i++) {
                    Map<String, Object> r = resumes.get(i);
                    json.append("{")
                        .append("\"id\":").append(r.get("id")).append(",")
                        .append("\"fileName\":\"").append(escape(String.valueOf(r.get("fileName")))).append("\",")
                        .append("\"fileSize\":").append(r.get("fileSize")).append(",")
                        .append("\"uploadedAt\":\"").append(escape(String.valueOf(r.get("uploadedAt")))).append("\",")
                        .append("\"isPrimary\":").append(r.get("isPrimary"))
                        .append("}");
                    if (i < resumes.size() - 1) json.append(",");
                }
                json.append("]");

                sendJson(ex, 200, json.toString());

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== DOWNLOAD RESUME ====================
        server.createContext("/download-resume", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("GET")) { send405(ex); return; }

            try {
                String idStr = getQueryParam(ex, "id");

                if (idStr == null) { sendJson(ex, 400, "{\"error\":\"id required\"}"); return; }

                Map<String, Object> resume = new ResumesDAO().getResumeById(Integer.parseInt(idStr));

                if (resume == null) {
                    sendJson(ex, 404, "{\"error\":\"Resume not found\"}");
                    return;
                }

                String filePath = (String) resume.get("filePath");
                String fileName = (String) resume.get("fileName");

                byte[] fileData = Files.readAllBytes(Paths.get(filePath));

                ex.getResponseHeaders().add("Content-Type", "application/pdf");
                ex.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                ex.sendResponseHeaders(200, fileData.length);

                try (OutputStream os = ex.getResponseBody()) {
                    os.write(fileData);
                }

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== SET PRIMARY RESUME ====================
        server.createContext("/set-primary-resume", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("POST")) { send405(ex); return; }

            try {
                Map<String, String> p = parseBody(ex);

                int    resumeId = intParam(p, "resumeId");
                String email    = p.getOrDefault("email", "").trim();

                if (resumeId <= 0 || email.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"resumeId and email required\"}");
                    return;
                }

                boolean ok = new ResumesDAO().setPrimaryResume(resumeId, email);
                sendJson(ex, ok ? 200 : 500, ok
                        ? "{\"message\":\"Primary resume set\"}"
                        : "{\"error\":\"Failed\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== DELETE RESUME ====================
        server.createContext("/delete-resume", (HttpExchange ex) -> {
            if (!ex.getRequestMethod().equals("POST")) { send405(ex); return; }

            try {
                Map<String, String> p = parseBody(ex);

                int    resumeId = intParam(p, "resumeId");
                String email    = p.getOrDefault("email", "").trim();

                if (resumeId <= 0 || email.isEmpty()) {
                    sendJson(ex, 400, "{\"error\":\"resumeId and email required\"}");
                    return;
                }

                ResumesDAO dao  = new ResumesDAO();
                String filePath = dao.getFilePath(resumeId, email);

                boolean ok = dao.deleteResume(resumeId, email);

                if (ok && filePath != null) {
                    // Best-effort file deletion
                    try { new File(filePath).delete(); } catch (Exception ignored) {}
                }

                sendJson(ex, ok ? 200 : 500, ok
                        ? "{\"message\":\"Resume deleted\"}"
                        : "{\"error\":\"Delete failed — not found or not yours\"}");

            } catch (Exception e) {
                e.printStackTrace();
                sendJson(ex, 500, "{\"error\":\"Server error\"}");
            }
        }).getFilters().add(new CorsFilter());

        // ==================== HEALTH CHECK ====================
        server.createContext("/health", (HttpExchange ex) -> {
            sendJson(ex, 200, "{\"status\":\"ok\",\"server\":\"Job Portal API\"}");
        }).getFilters().add(new CorsFilter());

        server.start();
        System.out.println("Server running at http://localhost:" + server.getAddress().getPort());
    }

    private static HttpServer createServer() throws IOException {
        int[] ports = {8080, 8081, 8082};
        IOException lastException = null;

        for (int port : ports) {
            try {
                HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
                System.out.println("Started backend on port " + port);
                return server;
            } catch (IOException e) {
                lastException = e;
                System.out.println("Port " + port + " unavailable: " + e.getMessage());
            }
        }

        throw lastException != null ? lastException : new IOException("Unable to bind server to any configured port");
    }

    // ========================= HELPERS =========================

    // Parse application/x-www-form-urlencoded body safely
    private static Map<String, String> parseBody(HttpExchange ex) throws IOException {
        byte[] raw = ex.getRequestBody().readAllBytes();
        String body = new String(raw, StandardCharsets.UTF_8);
        return parseFormEncoded(body);
    }

    // Parse query string
    private static Map<String, String> parseQueryString(HttpExchange ex) throws UnsupportedEncodingException {
        String query = ex.getRequestURI().getQuery();
        if (query == null || query.isEmpty()) return new HashMap<>();
        return parseFormEncoded(query);
    }

    // Handles keys/values that themselves contain '=' (e.g. base64 passwords)
    private static Map<String, String> parseFormEncoded(String data) throws UnsupportedEncodingException {
        Map<String, String> map = new LinkedHashMap<>();
        if (data == null || data.isEmpty()) return map;

        String[] pairs = data.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf('=');
            if (idx < 0) continue;
            String key   = URLDecoder.decode(pair.substring(0, idx), "UTF-8");
            String value = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
            map.put(key, value);
        }

        return map;
    }

    // Get a single query param by name
    private static String getQueryParam(HttpExchange ex, String name) throws UnsupportedEncodingException {
        return parseQueryString(ex).get(name);
    }

    // Send JSON response — uses byte length, not char length
    private static void sendJson(HttpExchange ex, int code, String json) throws IOException {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "application/json; charset=UTF-8");
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void send405(HttpExchange ex) throws IOException {
        sendJson(ex, 405, "{\"error\":\"Method not allowed\"}");
    }

    // Safe int parse from map
    private static int intParam(Map<String, String> map, String key) {
        try { return Integer.parseInt(map.getOrDefault(key, "0")); }
        catch (NumberFormatException e) { return 0; }
    }

    // Safe double parse from map
    private static double doubleParam(Map<String, String> map, String key) {
        try { return Double.parseDouble(map.getOrDefault(key, "0")); }
        catch (NumberFormatException e) { return 0; }
    }

    // Escape special chars for safe JSON string embedding
    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    // Build JSON for a list of jobs
    private static String jobsToJson(List<Map<String, String>> jobs) {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < jobs.size(); i++) {
            json.append(jobToJson(jobs.get(i)));
            if (i < jobs.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }

    // Build JSON for a single job map
    private static String jobToJson(Map<String, String> j) {
        return "{" +
               "\"id\":\""             + escape(j.get("id"))             + "\"," +
               "\"title\":\""          + escape(j.get("title"))          + "\"," +
               "\"description\":\""    + escape(j.get("description"))    + "\"," +
               "\"salary\":\""         + escape(j.get("salary"))         + "\"," +
               "\"location\":\""       + escape(j.get("location"))       + "\"," +
               "\"company\":\""        + escape(j.get("company"))        + "\"," +
               "\"recruiterEmail\":\"" + escape(j.get("recruiterEmail")) + "\"," +
               "\"applicantCount\":\"" + escape(j.get("applicantCount")) + "\"," +
               "\"createdAt\":\""      + escape(j.get("createdAt"))      + "\"" +
               "}";
    }

    // ========================= MULTIPART HELPERS (binary-safe) =========================

    private static final byte[] CRLF = "\r\n".getBytes(StandardCharsets.ISO_8859_1);

    // Find a field value in multipart body without corrupting binary parts
    private static String extractFieldFromBytes(byte[] body, String fieldName, String boundary) {
        String header = "name=\"" + fieldName + "\"";
        String bodyStr = new String(body, StandardCharsets.ISO_8859_1);

        int pos = bodyStr.indexOf(header);
        if (pos == -1) return null;

        int dataStart = bodyStr.indexOf("\r\n\r\n", pos);
        if (dataStart == -1) return null;
        dataStart += 4;

        int dataEnd = bodyStr.indexOf("\r\n--" + boundary, dataStart);
        if (dataEnd == -1) return null;

        return bodyStr.substring(dataStart, dataEnd).trim();
    }

    // Extract the filename from Content-Disposition header in multipart body
    private static String extractFileNameFromBytes(byte[] body, String boundary) {
        String bodyStr = new String(body, StandardCharsets.ISO_8859_1);
        String marker  = "filename=\"";
        int start = bodyStr.indexOf(marker);
        if (start == -1) return null;
        start += marker.length();
        int end = bodyStr.indexOf("\"", start);
        if (end == -1) return null;
        return bodyStr.substring(start, end);
    }

    // Extract raw file bytes from multipart body — binary-safe
    private static byte[] extractFileDataFromBytes(byte[] body, String fieldName, String boundary) {
        String bodyStr = new String(body, StandardCharsets.ISO_8859_1);

        // Find the part containing the file field
        String header   = "name=\"" + fieldName + "\"";
        int headerPos   = bodyStr.indexOf(header);
        if (headerPos == -1) return null;

        // Skip past the blank line that separates headers from data
        int dataStart = bodyStr.indexOf("\r\n\r\n", headerPos);
        if (dataStart == -1) return null;
        dataStart += 4;

        // Find the closing boundary
        String closingBoundary = "\r\n--" + boundary;
        int dataEnd = bodyStr.indexOf(closingBoundary, dataStart);
        if (dataEnd == -1) return null;

        // Copy raw bytes out of the original byte array
        byte[] fileData = new byte[dataEnd - dataStart];
        System.arraycopy(body, dataStart, fileData, 0, fileData.length);
        return fileData;
    }
}