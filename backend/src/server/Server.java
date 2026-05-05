package server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Filter;

import dao.UserDAO;
import dao.JobDAO;
import dao.ApplicationDAO;
import dao.ResumesDAO;

import service.UserService;
import service.ApplicationService;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Server {

    static class CorsFilter extends Filter {
        @Override
        public String description() {
            return "Adds CORS headers";
        }

        @Override
        public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");
            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            chain.doFilter(exchange);
        }
    }

    public static void main(String[] args) throws Exception {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // ================= REGISTER =================
        server.createContext("/register", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("POST")) {

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody())
                );

                String data = br.readLine();
                String[] parts = data.split("&");

                String name = URLDecoder.decode(parts[0].split("=")[1], "UTF-8");
                String email = URLDecoder.decode(parts[1].split("=")[1], "UTF-8");
                String password = URLDecoder.decode(parts[2].split("=")[1], "UTF-8");
                String role = URLDecoder.decode(parts[3].split("=")[1], "UTF-8");

                UserService service = new UserService();
                boolean success = service.register(name, email, password, role);

                String response = success ? "Registered Successfully" : "Failed";

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }).getFilters().add(new CorsFilter());

        // ================= LOGIN =================
        server.createContext("/login", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("POST")) {

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody())
                );

                String data = br.readLine();
                String[] parts = data.split("&");

                String email = URLDecoder.decode(parts[0].split("=")[1], "UTF-8");
                String password = URLDecoder.decode(parts[1].split("=")[1], "UTF-8");

                UserDAO dao = new UserDAO();
                String role = dao.login(email, password);

                String response = (role != null && !role.isEmpty()) ? role : "Invalid";

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }).getFilters().add(new CorsFilter());

        // ================= POST JOB =================
        server.createContext("/postjob", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("POST")) {

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody())
                );

                String data = br.readLine();
                String[] p = data.split("&");

                String title = URLDecoder.decode(p[0].split("=")[1], "UTF-8");
                String desc = URLDecoder.decode(p[1].split("=")[1], "UTF-8");
                double salary = Double.parseDouble(URLDecoder.decode(p[2].split("=")[1], "UTF-8"));
                String location = URLDecoder.decode(p[3].split("=")[1], "UTF-8");
                String company = URLDecoder.decode(p[4].split("=")[1], "UTF-8");
                String email = URLDecoder.decode(p[5].split("=")[1], "UTF-8");

                JobDAO dao = new JobDAO();
                boolean success = dao.postJob(
                        new model.Job(title, desc, salary, location, company, email)
                );

                String response = success ? "Job Posted" : "Failed";

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }).getFilters().add(new CorsFilter());

        // ================= GET JOBS =================
        server.createContext("/jobs", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("GET")) {

                JobDAO dao = new JobDAO();
                List<Map<String, String>> jobs = dao.getAllJobs();

                StringBuilder json = new StringBuilder();
                json.append("[");

                for (int i = 0; i < jobs.size(); i++) {

                    Map<String, String> job = jobs.get(i);

                    json.append("{")
                            .append("\"id\":\"").append(job.get("id")).append("\",")
                            .append("\"title\":\"").append(job.get("title")).append("\",")
                            .append("\"company\":\"").append(job.get("company")).append("\",")
                            .append("\"location\":\"").append(job.get("location")).append("\",")
                            .append("\"salary\":\"").append(job.get("salary")).append("\"")
                            .append("}");

                    if (i < jobs.size() - 1) json.append(",");
                }

                json.append("]");

                String response = json.toString();

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());

                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }).getFilters().add(new CorsFilter());

        // ================= APPLY JOB =================
        server.createContext("/apply", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("POST")) {

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody())
                );

                String data = br.readLine();
                String[] parts = data.split("&");

                int jobId = Integer.parseInt(URLDecoder.decode(parts[0].split("=")[1], "UTF-8"));
                String email = URLDecoder.decode(parts[1].split("=")[1], "UTF-8");

                ApplicationService service = new ApplicationService();
                boolean success = service.applyJob(jobId, email);

                String response = success ? "Applied Successfully" : "Failed";

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }).getFilters().add(new CorsFilter());

        // ================= GET APPLICANTS =================
        server.createContext("/applicants", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("GET")) {

                ApplicationDAO dao = new ApplicationDAO();
                List<Map<String, String>> list = dao.getAllApplications();

                StringBuilder json = new StringBuilder();
                json.append("[");

                for (int i = 0; i < list.size(); i++) {

                    Map<String, String> a = list.get(i);

                    json.append("{")
                            .append("\"email\":\"").append(a.get("email")).append("\",")
                            .append("\"job\":\"").append(a.get("job")).append("\",")
                            .append("\"company\":\"").append(a.get("company")).append("\"")
                            .append("}");

                    if (i < list.size() - 1) json.append(",");
                }

                json.append("]");

                String response = json.toString();

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());

                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }).getFilters().add(new CorsFilter());

        // ================= UPLOAD RESUME =================
        server.createContext("/upload-resume", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("POST")) {

                try {
                    String contentType = exchange.getRequestHeaders().getFirst("Content-Type");
                    
                    if (contentType != null && contentType.contains("multipart/form-data")) {
                        
                        // Parse multipart form data
                        byte[] buffer = new byte[8192];
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        InputStream is = exchange.getRequestBody();
                        int bytesRead;
                        
                        while ((bytesRead = is.read(buffer)) != -1) {
                            baos.write(buffer, 0, bytesRead);
                        }
                        
                        String body = baos.toString();
                        String boundary = contentType.split("boundary=")[1];
                        
                        // Extract email and file from multipart data
                        String email = extractMultipartField(body, "email", boundary);
                        byte[] fileData = extractMultipartFile(body, "file", boundary);
                        String fileName = extractMultipartFileName(body, boundary);
                        
                        if (email != null && fileData != null && fileName != null) {
                            
                            // Save file to uploads directory
                            String uploadDir = "uploads/";
                            new File(uploadDir).mkdirs();
                            
                            String filePath = uploadDir + System.currentTimeMillis() + "_" + fileName;
                            Files.write(Paths.get(filePath), fileData);
                            
                            // Save to database
                            ResumesDAO dao = new ResumesDAO();
                            boolean success = dao.uploadResume(email, fileName, filePath, fileData.length);
                            
                            String response = success ? "Resume uploaded successfully" : "Failed to save resume";
                            
                            exchange.sendResponseHeaders(200, response.length());
                            OutputStream os = exchange.getResponseBody();
                            os.write(response.getBytes());
                            os.close();
                        } else {
                            exchange.sendResponseHeaders(400, 15);
                            OutputStream os = exchange.getResponseBody();
                            os.write("Invalid request".getBytes());
                            os.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        exchange.sendResponseHeaders(500, 21);
                        OutputStream os = exchange.getResponseBody();
                        os.write("Error uploading file".getBytes());
                        os.close();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
            }
        }).getFilters().add(new CorsFilter());

        // ================= GET RESUMES =================
        server.createContext("/resumes", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("GET")) {

                String query = exchange.getRequestURI().getQuery();
                String email = null;
                
                if (query != null) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] kv = param.split("=");
                        if (kv[0].equals("email")) {
                            email = URLDecoder.decode(kv[1], "UTF-8");
                        }
                    }
                }
                
                ResumesDAO dao = new ResumesDAO();
                List<Map<String, Object>> resumes = dao.getResumesByEmail(email);
                
                StringBuilder json = new StringBuilder();
                json.append("[");
                
                for (int i = 0; i < resumes.size(); i++) {
                    Map<String, Object> r = resumes.get(i);
                    json.append("{")
                            .append("\"id\":").append(r.get("id")).append(",")
                            .append("\"fileName\":\"").append(r.get("fileName")).append("\",")
                            .append("\"fileSize\":").append(r.get("fileSize")).append(",")
                            .append("\"uploadedAt\":\"").append(r.get("uploadedAt")).append("\",")
                            .append("\"isPrimary\":").append(r.get("isPrimary"))
                            .append("}");
                    
                    if (i < resumes.size() - 1) json.append(",");
                }
                
                json.append("]");
                
                String response = json.toString();
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }).getFilters().add(new CorsFilter());

        // ================= DOWNLOAD RESUME =================
        server.createContext("/download-resume", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("GET")) {

                String query = exchange.getRequestURI().getQuery();
                int resumeId = -1;
                
                if (query != null) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] kv = param.split("=");
                        if (kv[0].equals("id")) {
                            resumeId = Integer.parseInt(kv[1]);
                        }
                    }
                }
                
                ResumesDAO dao = new ResumesDAO();
                Map<String, Object> resume = dao.getResumeById(resumeId);
                
                if (resume != null) {
                    String filePath = (String) resume.get("filePath");
                    String fileName = (String) resume.get("fileName");
                    
                    try {
                        byte[] fileData = Files.readAllBytes(Paths.get(filePath));
                        
                        exchange.getResponseHeaders().add("Content-Type", "application/pdf");
                        exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
                        exchange.sendResponseHeaders(200, fileData.length);
                        
                        OutputStream os = exchange.getResponseBody();
                        os.write(fileData);
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        exchange.sendResponseHeaders(404, 9);
                        OutputStream os = exchange.getResponseBody();
                        os.write("Not found".getBytes());
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).getFilters().add(new CorsFilter());

        // ================= SET PRIMARY RESUME =================
        server.createContext("/set-primary-resume", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("POST")) {

                BufferedReader br = new BufferedReader(
                        new InputStreamReader(exchange.getRequestBody())
                );

                String data = br.readLine();
                String[] parts = data.split("&");

                int resumeId = Integer.parseInt(parts[0].split("=")[1]);
                String email = URLDecoder.decode(parts[1].split("=")[1], "UTF-8");

                ResumesDAO dao = new ResumesDAO();
                boolean success = dao.setPrimaryResume(resumeId, email);

                String response = success ? "Primary resume set" : "Failed";

                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }).getFilters().add(new CorsFilter());

        // ================= SEARCH & FILTER JOBS =================
        server.createContext("/search", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("GET")) {

                String query = exchange.getRequestURI().getQuery();
                String keyword = null;
                String location = null;
                Double minSalary = null;
                Double maxSalary = null;

                if (query != null) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] kv = param.split("=");
                        if (kv.length == 2) {
                            String key = kv[0];
                            String value = URLDecoder.decode(kv[1], "UTF-8");

                            if (key.equals("keyword")) keyword = value;
                            else if (key.equals("location")) location = value;
                            else if (key.equals("minSalary") && !value.isEmpty()) minSalary = Double.parseDouble(value);
                            else if (key.equals("maxSalary") && !value.isEmpty()) maxSalary = Double.parseDouble(value);
                        }
                    }
                }

                JobDAO dao = new JobDAO();
                List<Map<String, String>> jobs = dao.searchAndFilter(keyword, location, minSalary, maxSalary);

                StringBuilder json = new StringBuilder();
                json.append("[");

                for (int i = 0; i < jobs.size(); i++) {

                    Map<String, String> job = jobs.get(i);

                    json.append("{")
                            .append("\"id\":\"").append(job.get("id")).append("\",")
                            .append("\"title\":\"").append(job.get("title")).append("\",")
                            .append("\"description\":\"").append(job.get("description")).append("\",")
                            .append("\"company\":\"").append(job.get("company")).append("\",")
                            .append("\"location\":\"").append(job.get("location")).append("\",")
                            .append("\"salary\":\"").append(job.get("salary")).append("\"")
                            .append("}");

                    if (i < jobs.size() - 1) json.append(",");
                }

                json.append("]");

                String response = json.toString();

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());

                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        }).getFilters().add(new CorsFilter());

        // ================= GET RECRUITER JOBS =================
        server.createContext("/recruiter-jobs", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("GET")) {

                String query = exchange.getRequestURI().getQuery();
                String email = null;

                if (query != null) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] kv = param.split("=");
                        if (kv[0].equals("email")) {
                            email = URLDecoder.decode(kv[1], "UTF-8");
                        }
                    }
                }

                if (email != null) {
                    JobDAO dao = new JobDAO();
                    List<Map<String, String>> jobs = dao.getJobsByRecruiter(email);

                    StringBuilder json = new StringBuilder();
                    json.append("[");

                    for (int i = 0; i < jobs.size(); i++) {

                        Map<String, String> job = jobs.get(i);

                        json.append("{")
                                .append("\"id\":\"").append(job.get("id")).append("\",")
                                .append("\"title\":\"").append(job.get("title")).append("\",")
                                .append("\"description\":\"").append(job.get("description")).append("\",")
                                .append("\"company\":\"").append(job.get("company")).append("\",")
                                .append("\"location\":\"").append(job.get("location")).append("\",")
                                .append("\"salary\":\"").append(job.get("salary")).append("\"")
                                .append("}");

                        if (i < jobs.size() - 1) json.append(",");
                    }

                    json.append("]");

                    String response = json.toString();

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.length());

                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    exchange.sendResponseHeaders(400, 15);
                    OutputStream os = exchange.getResponseBody();
                    os.write("Invalid request".getBytes());
                    os.close();
                }
            }
        }).getFilters().add(new CorsFilter());

        // ================= GET CANDIDATE APPLICATIONS =================
        server.createContext("/my-applications", (HttpExchange exchange) -> {

            if (exchange.getRequestMethod().equals("GET")) {

                String query = exchange.getRequestURI().getQuery();
                String email = null;

                if (query != null) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        String[] kv = param.split("=");
                        if (kv[0].equals("email")) {
                            email = URLDecoder.decode(kv[1], "UTF-8");
                        }
                    }
                }

                if (email != null) {
                    ApplicationDAO dao = new ApplicationDAO();
                    List<Map<String, String>> applications = dao.getApplicationsByCandidate(email);

                    StringBuilder json = new StringBuilder();
                    json.append("[");

                    for (int i = 0; i < applications.size(); i++) {

                        Map<String, String> app = applications.get(i);

                        json.append("{")
                                .append("\"job\":\"").append(app.get("job")).append("\",")
                                .append("\"company\":\"").append(app.get("company")).append("\",")
                                .append("\"status\":\"").append(app.get("status")).append("\",")
                                .append("\"appliedAt\":\"").append(app.get("appliedAt")).append("\"")
                                .append("}");

                        if (i < applications.size() - 1) json.append(",");
                    }

                    json.append("]");

                    String response = json.toString();

                    exchange.getResponseHeaders().add("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, response.length());

                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                } else {
                    exchange.sendResponseHeaders(400, 15);
                    OutputStream os = exchange.getResponseBody();
                    os.write("Invalid request".getBytes());
                    os.close();
                }
            }
        }).getFilters().add(new CorsFilter());

        // ================= START SERVER =================
        server.start();
        System.out.println("Server running at http://localhost:8080 🚀");
    }

    // ================= HELPER METHODS FOR MULTIPART =================
    private static String extractMultipartField(String body, String fieldName, String boundary) {
        String pattern = "name=\"" + fieldName + "\"";
        int start = body.indexOf(pattern);
        if (start == -1) return null;
        
        start = body.indexOf("\r\n\r\n", start) + 4;
        int end = body.indexOf("\r\n--" + boundary, start);
        
        return body.substring(start, end).trim();
    }

    private static String extractMultipartFileName(String body, String boundary) {
        String pattern = "filename=\"";
        int start = body.indexOf(pattern);
        if (start == -1) return null;
        
        start += pattern.length();
        int end = body.indexOf("\"", start);
        
        return body.substring(start, end);
    }

    private static byte[] extractMultipartFile(String body, String fieldName, String boundary) throws UnsupportedEncodingException {
        String pattern = "name=\"" + fieldName + "\"";
        int start = body.indexOf(pattern);
        if (start == -1) return null;
        
        start = body.indexOf("\r\n\r\n", start) + 4;
        int end = body.indexOf("\r\n--" + boundary, start);
        
        String fileContent = body.substring(start, end);
        return fileContent.getBytes("ISO-8859-1");
    }
}