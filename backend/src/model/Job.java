package model;

public class Job {

    private String title;
    private String description;
    private double salary;
    private String location;
    private String companyName;
    private String recruiterEmail;

    public Job(String title, String description, double salary,
               String location, String companyName, String recruiterEmail) {
        this.title = title;
        this.description = description;
        this.salary = salary;
        this.location = location;
        this.companyName = companyName;
        this.recruiterEmail = recruiterEmail;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getSalary() { return salary; }
    public String getLocation() { return location; }
    public String getCompanyName() { return companyName; }
    public String getRecruiterEmail() { return recruiterEmail; }
}