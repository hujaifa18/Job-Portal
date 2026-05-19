package dao;

import util.DBConnection;
import java.sql.*;
import java.util.*;

public class JobDAO {

    // ================= POST JOB =================
    public boolean postJob(model.Job job) {

        String sql = "INSERT INTO jobs(title, description, salary, location, company_name, recruiter_email) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, job.getTitle());
            ps.setString(2, job.getDescription());
            ps.setDouble(3, job.getSalary());
            ps.setString(4, job.getLocation());
            ps.setString(5, job.getCompanyName());
            ps.setString(6, job.getRecruiterEmail());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= UPDATE JOB =================
    public boolean updateJob(int jobId, String title, String description,
                             double salary, String location, String company,
                             String recruiterEmail) {

        // Recruiter can only update their own job
        String sql = "UPDATE jobs SET title=?, description=?, salary=?, location=?, company_name=? " +
                     "WHERE id=? AND recruiter_email=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, title);
            ps.setString(2, description);
            ps.setDouble(3, salary);
            ps.setString(4, location);
            ps.setString(5, company);
            ps.setInt(6, jobId);
            ps.setString(7, recruiterEmail);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= DELETE JOB =================
    public boolean deleteJob(int jobId, String recruiterEmail) {

        String sql = "DELETE FROM jobs WHERE id = ? AND recruiter_email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, jobId);
            ps.setString(2, recruiterEmail);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= GET ALL JOBS =================
    public List<Map<String, String>> getAllJobs() {

        List<Map<String, String>> jobs = new ArrayList<>();

        String sql = "SELECT j.*, COUNT(a.id) AS applicant_count " +
                     "FROM jobs j " +
                     "LEFT JOIN applications a ON j.id = a.job_id " +
                     "GROUP BY j.id " +
                     "ORDER BY j.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                jobs.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // ================= GET SINGLE JOB =================
    public Map<String, String> getJobById(int jobId) {

        String sql = "SELECT j.*, COUNT(a.id) AS applicant_count " +
                     "FROM jobs j " +
                     "LEFT JOIN applications a ON j.id = a.job_id " +
                     "WHERE j.id = ? " +
                     "GROUP BY j.id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, jobId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= SEARCH BY TITLE / DESCRIPTION =================
    public List<Map<String, String>> searchJobsByTitle(String keyword) {

        List<Map<String, String>> jobs = new ArrayList<>();

        String sql = "SELECT j.*, COUNT(a.id) AS applicant_count " +
                     "FROM jobs j " +
                     "LEFT JOIN applications a ON j.id = a.job_id " +
                     "WHERE LOWER(j.title) LIKE LOWER(?) OR LOWER(j.description) LIKE LOWER(?) " +
                     "GROUP BY j.id " +
                     "ORDER BY j.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) jobs.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // ================= FILTER BY LOCATION =================
    public List<Map<String, String>> filterJobsByLocation(String location) {

        List<Map<String, String>> jobs = new ArrayList<>();

        String sql = "SELECT j.*, COUNT(a.id) AS applicant_count " +
                     "FROM jobs j " +
                     "LEFT JOIN applications a ON j.id = a.job_id " +
                     "WHERE LOWER(j.location) LIKE LOWER(?) " +
                     "GROUP BY j.id " +
                     "ORDER BY j.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + location + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) jobs.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // ================= FILTER BY SALARY RANGE =================
    public List<Map<String, String>> filterJobsBySalary(double minSalary, double maxSalary) {

        List<Map<String, String>> jobs = new ArrayList<>();

        String sql = "SELECT j.*, COUNT(a.id) AS applicant_count " +
                     "FROM jobs j " +
                     "LEFT JOIN applications a ON j.id = a.job_id " +
                     "WHERE j.salary BETWEEN ? AND ? " +
                     "GROUP BY j.id " +
                     "ORDER BY j.salary DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, minSalary);
            ps.setDouble(2, maxSalary);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) jobs.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // ================= COMBINED SEARCH + FILTER =================
    public List<Map<String, String>> searchAndFilter(String keyword, String location,
                                                     Double minSalary, Double maxSalary) {

        List<Map<String, String>> jobs = new ArrayList<>();

        StringBuilder sql = new StringBuilder(
            "SELECT j.*, COUNT(a.id) AS applicant_count " +
            "FROM jobs j " +
            "LEFT JOIN applications a ON j.id = a.job_id " +
            "WHERE 1=1"
        );

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (LOWER(j.title) LIKE LOWER(?) OR LOWER(j.description) LIKE LOWER(?))");
        }
        if (location != null && !location.trim().isEmpty()) {
            sql.append(" AND LOWER(j.location) LIKE LOWER(?)");
        }
        if (minSalary != null) {
            sql.append(" AND j.salary >= ?");
        }
        if (maxSalary != null) {
            sql.append(" AND j.salary <= ?");
        }

        sql.append(" GROUP BY j.id ORDER BY j.created_at DESC");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String like = "%" + keyword + "%";
                ps.setString(idx++, like);
                ps.setString(idx++, like);
            }
            if (location != null && !location.trim().isEmpty()) {
                ps.setString(idx++, "%" + location + "%");
            }
            if (minSalary != null) {
                ps.setDouble(idx++, minSalary);
            }
            if (maxSalary != null) {
                ps.setDouble(idx++, maxSalary);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) jobs.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // ================= JOBS BY RECRUITER =================
    public List<Map<String, String>> getJobsByRecruiter(String recruiterEmail) {

        List<Map<String, String>> jobs = new ArrayList<>();

        String sql = "SELECT j.*, COUNT(a.id) AS applicant_count " +
                     "FROM jobs j " +
                     "LEFT JOIN applications a ON j.id = a.job_id " +
                     "WHERE j.recruiter_email = ? " +
                     "GROUP BY j.id " +
                     "ORDER BY j.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, recruiterEmail);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) jobs.add(mapRow(rs));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // ================= SHARED ROW MAPPER =================
    private Map<String, String> mapRow(ResultSet rs) throws SQLException {
        Map<String, String> job = new HashMap<>();
        job.put("id",             String.valueOf(rs.getInt("id")));
        job.put("title",          rs.getString("title"));
        job.put("description",    rs.getString("description"));
        job.put("salary",         String.valueOf(rs.getDouble("salary")));
        job.put("location",       rs.getString("location"));
        job.put("company",        rs.getString("company_name"));
        job.put("recruiterEmail", rs.getString("recruiter_email"));
        job.put("applicantCount", String.valueOf(rs.getInt("applicant_count")));
        Timestamp ts = rs.getTimestamp("created_at");
        job.put("createdAt", ts != null ? ts.toString() : "");
        return job;
    }
}