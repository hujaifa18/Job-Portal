package model;

public class Application {

    private int jobId;
    private String candidateEmail;

    public Application(int jobId, String candidateEmail) {
        this.jobId = jobId;
        this.candidateEmail = candidateEmail;
    }

    public int getJobId() {
        return jobId;
    }

    public String getCandidateEmail() {
        return candidateEmail;
    }
}