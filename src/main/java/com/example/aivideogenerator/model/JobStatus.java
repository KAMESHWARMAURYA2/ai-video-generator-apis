package com.example.aivideogenerator.model;

public class JobStatus {
    private JobState status;
    private String videoUrl;
    private String error;

    public JobStatus() {
    }

    public JobStatus(JobState status, String videoUrl, String error) {
        this.status = status;
        this.videoUrl = videoUrl;
        this.error = error;
    }

    public JobState getStatus() {
        return status;
    }

    public void setStatus(JobState status) {
        this.status = status;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
