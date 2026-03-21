package com.example.aivideogenerator.model;

public class StatusResponse {
    private String status;
    private String videoUrl;
    private String error;

    public StatusResponse(String status, String videoUrl, String error) {
        this.status = status;
        this.videoUrl = videoUrl;
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getError() {
        return error;
    }
}
