package model;

public class Application {

    private int    id;
    private int    jobId;
    private String candidateEmail;
    private String status;

    public Application(int jobId, String candidateEmail) {
        this.jobId          = jobId;
        this.candidateEmail = candidateEmail;
    }

    public int    getId()             { return id; }
    public int    getJobId()          { return jobId; }
    public String getCandidateEmail() { return candidateEmail; }
    public String getStatus()         { return status; }

    public void setId(int id)             { this.id = id; }
    public void setStatus(String status)  { this.status = status; }
}