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

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "INSERT INTO applications(job_id, candidate_email) VALUES (?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1, application.getJobId());
            ps.setString(2, application.getCandidateEmail());

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= GET ALL APPLICATIONS =================
    public List<Map<String, String>> getAllApplications() {

        List<Map<String, String>> applications = new ArrayList<>();

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT a.candidate_email, j.title as job, j.company_name as company " +
                        "FROM applications a " +
                        "JOIN jobs j ON a.job_id = j.id";

            PreparedStatement ps = conn.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Map<String, String> app = new HashMap<>();

                app.put("email", rs.getString("candidate_email"));
                app.put("job", rs.getString("job"));
                app.put("company", rs.getString("company"));

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

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT a.id, a.status, a.applied_at, j.title as job, j.company_name as company " +
                        "FROM applications a " +
                        "JOIN jobs j ON a.job_id = j.id " +
                        "WHERE a.candidate_email = ? " +
                        "ORDER BY a.applied_at DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, candidateEmail);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Map<String, String> app = new HashMap<>();

                app.put("job", rs.getString("job"));
                app.put("company", rs.getString("company"));
                app.put("status", rs.getString("status"));
                app.put("appliedAt", rs.getTimestamp("applied_at").toString());

                applications.add(app);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return applications;
    }
}
