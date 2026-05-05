package dao;

import util.DBConnection;
import java.sql.*;
import java.util.*;

public class JobDAO {

    // ================= POST JOB (already exists) =================
    public boolean postJob(model.Job job) {

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "INSERT INTO jobs(title, description, salary, location, company_name, recruiter_email) VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, job.getTitle());
            ps.setString(2, job.getDescription());
            ps.setDouble(3, job.getSalary());
            ps.setString(4, job.getLocation());
            ps.setString(5, job.getCompanyName());
            ps.setString(6, job.getRecruiterEmail());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= GET ALL JOBS =================
    public List<Map<String, String>> getAllJobs() {

        List<Map<String, String>> jobs = new ArrayList<>();

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM jobs";

            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Map<String, String> job = new HashMap<>();

                job.put("id", String.valueOf(rs.getInt("id")));
                job.put("title", rs.getString("title"));
                job.put("description", rs.getString("description"));
                job.put("salary", String.valueOf(rs.getDouble("salary")));
                job.put("location", rs.getString("location"));
                job.put("company", rs.getString("company_name"));

                jobs.add(job);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // ================= SEARCH JOBS BY TITLE =================
    public List<Map<String, String>> searchJobsByTitle(String keyword) {

        List<Map<String, String>> jobs = new ArrayList<>();

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM jobs WHERE LOWER(title) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?) ORDER BY created_at DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Map<String, String> job = new HashMap<>();

                job.put("id", String.valueOf(rs.getInt("id")));
                job.put("title", rs.getString("title"));
                job.put("description", rs.getString("description"));
                job.put("salary", String.valueOf(rs.getDouble("salary")));
                job.put("location", rs.getString("location"));
                job.put("company", rs.getString("company_name"));

                jobs.add(job);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // ================= FILTER JOBS BY LOCATION =================
    public List<Map<String, String>> filterJobsByLocation(String location) {

        List<Map<String, String>> jobs = new ArrayList<>();

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM jobs WHERE LOWER(location) LIKE LOWER(?) ORDER BY created_at DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + location + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Map<String, String> job = new HashMap<>();

                job.put("id", String.valueOf(rs.getInt("id")));
                job.put("title", rs.getString("title"));
                job.put("description", rs.getString("description"));
                job.put("salary", String.valueOf(rs.getDouble("salary")));
                job.put("location", rs.getString("location"));
                job.put("company", rs.getString("company_name"));

                jobs.add(job);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // ================= FILTER JOBS BY SALARY RANGE =================
    public List<Map<String, String>> filterJobsBySalary(double minSalary, double maxSalary) {

        List<Map<String, String>> jobs = new ArrayList<>();

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM jobs WHERE salary BETWEEN ? AND ? ORDER BY salary DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setDouble(1, minSalary);
            ps.setDouble(2, maxSalary);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Map<String, String> job = new HashMap<>();

                job.put("id", String.valueOf(rs.getInt("id")));
                job.put("title", rs.getString("title"));
                job.put("description", rs.getString("description"));
                job.put("salary", String.valueOf(rs.getDouble("salary")));
                job.put("location", rs.getString("location"));
                job.put("company", rs.getString("company_name"));

                jobs.add(job);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // ================= SEARCH AND FILTER COMBINED =================
    public List<Map<String, String>> searchAndFilter(String keyword, String location, Double minSalary, Double maxSalary) {

        List<Map<String, String>> jobs = new ArrayList<>();

        try {
            Connection conn = DBConnection.getConnection();

            StringBuilder sql = new StringBuilder("SELECT * FROM jobs WHERE 1=1");

            if (keyword != null && !keyword.isEmpty()) {
                sql.append(" AND (LOWER(title) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?))");
            }

            if (location != null && !location.isEmpty()) {
                sql.append(" AND LOWER(location) LIKE LOWER(?)");
            }

            if (minSalary != null) {
                sql.append(" AND salary >= ?");
            }

            if (maxSalary != null) {
                sql.append(" AND salary <= ?");
            }

            sql.append(" ORDER BY created_at DESC");

            PreparedStatement ps = conn.prepareStatement(sql.toString());
            int index = 1;

            if (keyword != null && !keyword.isEmpty()) {
                ps.setString(index++, "%" + keyword + "%");
                ps.setString(index++, "%" + keyword + "%");
            }

            if (location != null && !location.isEmpty()) {
                ps.setString(index++, "%" + location + "%");
            }

            if (minSalary != null) {
                ps.setDouble(index++, minSalary);
            }

            if (maxSalary != null) {
                ps.setDouble(index++, maxSalary);
            }

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Map<String, String> job = new HashMap<>();

                job.put("id", String.valueOf(rs.getInt("id")));
                job.put("title", rs.getString("title"));
                job.put("description", rs.getString("description"));
                job.put("salary", String.valueOf(rs.getDouble("salary")));
                job.put("location", rs.getString("location"));
                job.put("company", rs.getString("company_name"));

                jobs.add(job);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }

    // ================= GET JOBS BY RECRUITER =================
    public List<Map<String, String>> getJobsByRecruiter(String recruiterEmail) {

        List<Map<String, String>> jobs = new ArrayList<>();

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT * FROM jobs WHERE recruiter_email = ? ORDER BY created_at DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, recruiterEmail);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Map<String, String> job = new HashMap<>();

                job.put("id", String.valueOf(rs.getInt("id")));
                job.put("title", rs.getString("title"));
                job.put("description", rs.getString("description"));
                job.put("salary", String.valueOf(rs.getDouble("salary")));
                job.put("location", rs.getString("location"));
                job.put("company", rs.getString("company_name"));

                jobs.add(job);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return jobs;
    }
}