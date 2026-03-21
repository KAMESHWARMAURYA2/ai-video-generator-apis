package com.example.aivideogenerator.exception;

public class VideoGenerationException extends RuntimeException {
    public VideoGenerationException(String message) {
        super(message);
    }

    public VideoGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
