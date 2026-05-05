package service;

import dao.ApplicationDAO;
import model.Application;

public class ApplicationService {

    public boolean applyJob(int jobId, String candidateEmail) {

        Application application = new Application(jobId, candidateEmail);

        return new ApplicationDAO().applyJob(application);
    }
}
