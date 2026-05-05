package service;

import dao.JobDAO;
import model.Job;

public class JobService {

    JobDAO dao = new JobDAO();

    public boolean postJob(String title, String description,
                           double salary, String location,
                           String companyName, String recruiterEmail) {

        Job job = new Job(title, description, salary, location, companyName, recruiterEmail);

        return dao.postJob(job);
    }
}