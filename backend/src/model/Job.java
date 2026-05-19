package model;

public class Job {

    private int    id;
    private String title;
    private String description;
    private double salary;
    private String location;
    private String companyName;
    private String recruiterEmail;
    private String createdAt;

    public Job(String title, String description, double salary,
               String location, String companyName, String recruiterEmail) {
        this.title          = title;
        this.description    = description;
        this.salary         = salary;
        this.location       = location;
        this.companyName    = companyName;
        this.recruiterEmail = recruiterEmail;
    }

    public int    getId()             { return id; }
    public String getTitle()          { return title; }
    public String getDescription()    { return description; }
    public double getSalary()         { return salary; }
    public String getLocation()       { return location; }
    public String getCompanyName()    { return companyName; }
    public String getRecruiterEmail() { return recruiterEmail; }
    public String getCreatedAt()      { return createdAt; }

    public void setId(int id)                 { this.id = id; }
    public void setCreatedAt(String c)        { this.createdAt = c; }
}