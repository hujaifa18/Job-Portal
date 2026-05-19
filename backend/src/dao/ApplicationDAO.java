package dao;

import model.Application;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ApplicationDAO {

    // ================= APPLY FOR JOB =================
    public boolean applyJob(Application application) {

        // Prevent duplicate applications
        if (alreadyApplied(application.getJobId(), application.getCandidateEmail())) {
            return false;
        }

        String sql = "INSERT INTO applications(job_id, candidate_email) VALUES (?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, application.getJobId());
            ps.setString(2, application.getCandidateEmail());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= DUPLICATE CHECK =================
    public boolean alreadyApplied(int jobId, String candidateEmail) {

        String sql = "SELECT 1 FROM applications WHERE job_id = ? AND candidate_email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, jobId);
            ps.setString(2, candidateEmail);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= GET ALL APPLICATIONS (admin only) =================
    public List<Map<String, String>> getAllApplications() {

        List<Map<String, String>> applications = new ArrayList<>();

        String sql = "SELECT a.id, a.candidate_email, a.status, a.applied_at, " +
                     "j.title AS job, j.company_name AS company " +
                     "FROM applications a " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "ORDER BY a.applied_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Map<String, String> app = new HashMap<>();
                app.put("id",        String.valueOf(rs.getInt("id")));
                app.put("email",     rs.getString("candidate_email"));
                app.put("job",       rs.getString("job"));
                app.put("company",   rs.getString("company"));
                app.put("status",    rs.getString("status"));
                app.put("appliedAt", rs.getTimestamp("applied_at").toString());
                applications.add(app);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return applications;
    }

    // ================= GET APPLICATIONS BY CANDIDATE =================
    public List<Map<String, String>> getApplicationsByCandidate(String candidateEmail) {

        List<Map<String, String>> applications = new ArrayList<>();

        String sql = "SELECT a.id, a.status, a.applied_at, j.id AS job_id, j.title AS job, j.company_name AS company " +
                     "FROM applications a " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "WHERE a.candidate_email = ? " +
                     "ORDER BY a.applied_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, candidateEmail);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> app = new HashMap<>();
                    app.put("id",        String.valueOf(rs.getInt("id")));
                    app.put("jobId",     String.valueOf(rs.getInt("job_id")));
                    app.put("job",       rs.getString("job"));
                    app.put("company",   rs.getString("company"));
                    app.put("status",    rs.getString("status"));
                    app.put("appliedAt", rs.getTimestamp("applied_at").toString());
                    applications.add(app);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return applications;
    }

    // ================= GET APPLICANTS FOR RECRUITER'S JOBS =================
    public List<Map<String, String>> getApplicantsForRecruiter(String recruiterEmail) {

        List<Map<String, String>> applications = new ArrayList<>();

        String sql = "SELECT a.id, a.candidate_email, a.status, a.applied_at, " +
                     "j.title AS job, j.company_name AS company, u.name AS candidate_name " +
                     "FROM applications a " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "LEFT JOIN users u ON u.email = a.candidate_email " +
                     "WHERE j.recruiter_email = ? " +
                     "ORDER BY a.applied_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, recruiterEmail);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> app = new HashMap<>();
                    app.put("id",            String.valueOf(rs.getInt("id")));
                    app.put("email",         rs.getString("candidate_email"));
                    app.put("candidateName", rs.getString("candidate_name") != null ? rs.getString("candidate_name") : "Unknown");
                    app.put("job",           rs.getString("job"));
                    app.put("company",       rs.getString("company"));
                    app.put("status",        rs.getString("status"));
                    app.put("appliedAt",     rs.getTimestamp("applied_at").toString());
                    applications.add(app);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return applications;
    }

    // ================= UPDATE APPLICATION STATUS =================
    // recruiterEmail is required so a recruiter can only update their own job's applications
    public boolean updateStatus(int applicationId, String newStatus, String recruiterEmail) {

        String sql = "UPDATE applications a " +
                     "JOIN jobs j ON a.job_id = j.id " +
                     "SET a.status = ? " +
                     "WHERE a.id = ? AND j.recruiter_email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newStatus);
            ps.setInt(2, applicationId);
            ps.setString(3, recruiterEmail);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= WITHDRAW APPLICATION =================
    public boolean withdraw(int applicationId, String candidateEmail) {

        String sql = "DELETE FROM applications WHERE id = ? AND candidate_email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, applicationId);
            ps.setString(2, candidateEmail);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}