package dao;

import util.DBConnection;
import java.sql.*;
import java.util.*;

public class ResumesDAO {

    // ================= UPLOAD RESUME =================
    public boolean uploadResume(String email, String fileName, String filePath, int fileSize) {

        try {
            Connection conn = DBConnection.getConnection();

            // Set all other resumes as non-primary if this is the first one
            String countSql = "SELECT COUNT(*) FROM resumes WHERE candidate_email = ?";
            PreparedStatement countPs = conn.prepareStatement(countSql);
            countPs.setString(1, email);
            ResultSet countRs = countPs.executeQuery();
            countRs.next();
            boolean isFirst = countRs.getInt(1) == 0;

            String sql = "INSERT INTO resumes(candidate_email, file_name, file_path, file_size, is_primary) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, fileName);
            ps.setString(3, filePath);
            ps.setInt(4, fileSize);
            ps.setBoolean(5, isFirst); // First resume is primary by default

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= GET ALL RESUMES FOR USER =================
    public List<Map<String, Object>> getResumesByEmail(String email) {

        List<Map<String, Object>> resumes = new ArrayList<>();

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT id, file_name, file_path, file_size, uploaded_at, is_primary FROM resumes WHERE candidate_email = ? ORDER BY is_primary DESC, uploaded_at DESC";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Map<String, Object> resume = new HashMap<>();

                resume.put("id", rs.getInt("id"));
                resume.put("fileName", rs.getString("file_name"));
                resume.put("filePath", rs.getString("file_path"));
                resume.put("fileSize", rs.getInt("file_size"));
                resume.put("uploadedAt", rs.getTimestamp("uploaded_at"));
                resume.put("isPrimary", rs.getBoolean("is_primary"));

                resumes.add(resume);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resumes;
    }

    // ================= GET RESUME BY ID =================
    public Map<String, Object> getResumeById(int resumeId) {

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "SELECT id, file_name, file_path, candidate_email FROM resumes WHERE id = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, resumeId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Map<String, Object> resume = new HashMap<>();

                resume.put("id", rs.getInt("id"));
                resume.put("fileName", rs.getString("file_name"));
                resume.put("filePath", rs.getString("file_path"));
                resume.put("candidateEmail", rs.getString("candidate_email"));

                return resume;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= SET PRIMARY RESUME =================
    public boolean setPrimaryResume(int resumeId, String email) {

        try {
            Connection conn = DBConnection.getConnection();

            // Set all resumes for this user as non-primary
            String updateSql = "UPDATE resumes SET is_primary = FALSE WHERE candidate_email = ?";
            PreparedStatement updatePs = conn.prepareStatement(updateSql);
            updatePs.setString(1, email);
            updatePs.executeUpdate();

            // Set the selected resume as primary
            String setPrimary = "UPDATE resumes SET is_primary = TRUE WHERE id = ? AND candidate_email = ?";
            PreparedStatement setPrimaryPs = conn.prepareStatement(setPrimary);
            setPrimaryPs.setInt(1, resumeId);
            setPrimaryPs.setString(2, email);

            return setPrimaryPs.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    // ================= DELETE RESUME =================
    public boolean deleteResume(int resumeId) {

        try {
            Connection conn = DBConnection.getConnection();

            String sql = "DELETE FROM resumes WHERE id = ?";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, resumeId);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}
