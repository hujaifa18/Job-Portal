package dao;

import util.DBConnection;
import java.sql.*;
import java.util.*;

public class ResumesDAO {

    // ================= UPLOAD RESUME =================
    public boolean uploadResume(String email, String fileName, String filePath, int fileSize) {

        String countSql = "SELECT COUNT(*) FROM resumes WHERE candidate_email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement countPs = conn.prepareStatement(countSql)) {

            countPs.setString(1, email);

            boolean isFirst;
            try (ResultSet countRs = countPs.executeQuery()) {
                countRs.next();
                isFirst = countRs.getInt(1) == 0;
            }

            String sql = "INSERT INTO resumes(candidate_email, file_name, file_path, file_size, is_primary) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                ps.setString(2, fileName);
                ps.setString(3, filePath);
                ps.setInt(4, fileSize);
                ps.setBoolean(5, isFirst);

                return ps.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= GET ALL RESUMES FOR USER =================
    public List<Map<String, Object>> getResumesByEmail(String email) {

        List<Map<String, Object>> resumes = new ArrayList<>();

        String sql = "SELECT id, file_name, file_path, file_size, uploaded_at, is_primary " +
                     "FROM resumes WHERE candidate_email = ? " +
                     "ORDER BY is_primary DESC, uploaded_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> resume = new HashMap<>();
                    resume.put("id",         rs.getInt("id"));
                    resume.put("fileName",   rs.getString("file_name"));
                    resume.put("filePath",   rs.getString("file_path"));
                    resume.put("fileSize",   rs.getInt("file_size"));
                    resume.put("uploadedAt", rs.getTimestamp("uploaded_at"));
                    resume.put("isPrimary",  rs.getBoolean("is_primary"));
                    resumes.add(resume);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resumes;
    }

    // ================= GET RESUME BY ID =================
    public Map<String, Object> getResumeById(int resumeId) {

        String sql = "SELECT id, file_name, file_path, candidate_email, file_size FROM resumes WHERE id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, resumeId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> resume = new HashMap<>();
                    resume.put("id",             rs.getInt("id"));
                    resume.put("fileName",        rs.getString("file_name"));
                    resume.put("filePath",        rs.getString("file_path"));
                    resume.put("candidateEmail",  rs.getString("candidate_email"));
                    resume.put("fileSize",        rs.getInt("file_size"));
                    return resume;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // ================= SET PRIMARY RESUME =================
    public boolean setPrimaryResume(int resumeId, String email) {

        String clearSql = "UPDATE resumes SET is_primary = FALSE WHERE candidate_email = ?";
        String setSql   = "UPDATE resumes SET is_primary = TRUE  WHERE id = ? AND candidate_email = ?";

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            try (PreparedStatement clearPs = conn.prepareStatement(clearSql)) {
                clearPs.setString(1, email);
                clearPs.executeUpdate();
            }

            int updated;
            try (PreparedStatement setPs = conn.prepareStatement(setSql)) {
                setPs.setInt(1, resumeId);
                setPs.setString(2, email);
                updated = setPs.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            return updated > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= DELETE RESUME =================
    // candidateEmail ensures a user can only delete their own resume
    public boolean deleteResume(int resumeId, String candidateEmail) {

        // First retrieve the file path so caller can clean up the file from disk
        String sql = "DELETE FROM resumes WHERE id = ? AND candidate_email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, resumeId);
            ps.setString(2, candidateEmail);

            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ================= GET FILE PATH FOR DELETION =================
    public String getFilePath(int resumeId, String candidateEmail) {

        String sql = "SELECT file_path FROM resumes WHERE id = ? AND candidate_email = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, resumeId);
            ps.setString(2, candidateEmail);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("file_path");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}