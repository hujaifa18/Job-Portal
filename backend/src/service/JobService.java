package service;

import dao.JobDAO;
import model.Job;

public class JobService {

    private final JobDAO dao = new JobDAO();

    public boolean postJob(String title, String description,
                           double salary, String location,
                           String companyName, String recruiterEmail) {

        if (title == null || title.trim().isEmpty()) return false;
        if (recruiterEmail == null || recruiterEmail.trim().isEmpty()) return false;
        if (salary < 0) return false;

        return dao.postJob(new Job(title.trim(), description, salary, location, companyName, recruiterEmail));
    }

    public boolean updateJob(int jobId, String title, String description,
                             double salary, String location,
                             String companyName, String recruiterEmail) {

        if (title == null || title.trim().isEmpty()) return false;
        if (jobId <= 0) return false;

        return dao.updateJob(jobId, title.trim(), description, salary, location, companyName, recruiterEmail);
    }

    public boolean deleteJob(int jobId, String recruiterEmail) {
        if (jobId <= 0 || recruiterEmail == null) return false;
        return dao.deleteJob(jobId, recruiterEmail);
    }
}