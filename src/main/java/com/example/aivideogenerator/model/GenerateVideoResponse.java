package com.example.aivideogenerator.model;

public class GenerateVideoResponse {
    private String jobId;
    private String status;

    public GenerateVideoResponse(String jobId, String status) {
        this.jobId = jobId;
        this.status = status;
    }

    public String getJobId() {
        return jobId;
    }

    public String getStatus() {
        return status;
    }
}
