package service;

import dao.ApplicationDAO;
import model.Application;

public class ApplicationService {

    private final ApplicationDAO dao = new ApplicationDAO();

    public boolean applyJob(int jobId, String candidateEmail) {

        if (jobId <= 0 || candidateEmail == null || candidateEmail.trim().isEmpty()) return false;
        return dao.applyJob(new Application(jobId, candidateEmail.trim()));
    }

    public boolean alreadyApplied(int jobId, String candidateEmail) {
        return dao.alreadyApplied(jobId, candidateEmail);
    }

    public boolean updateStatus(int applicationId, String status, String recruiterEmail) {

        // Only allow valid status values
        if (!status.equals("pending") && !status.equals("reviewed")
                && !status.equals("accepted") && !status.equals("rejected")) {
            return false;
        }

        return dao.updateStatus(applicationId, status, recruiterEmail);
    }

    public boolean withdraw(int applicationId, String candidateEmail) {
        return dao.withdraw(applicationId, candidateEmail);
    }
}