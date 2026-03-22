package com.example.aivideogenerator.controller;

import com.example.aivideogenerator.model.GenerateVideoResponse;
import com.example.aivideogenerator.model.JobStatus;
import com.example.aivideogenerator.model.StatusResponse;
import com.example.aivideogenerator.service.VideoGenerationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping
@Validated
public class VideoController {

    private final VideoGenerationService videoGenerationService;

    public VideoController(VideoGenerationService videoGenerationService) {
        this.videoGenerationService = videoGenerationService;
    }

    @PostMapping({"/generate-video", "/videos/generate"})
    public GenerateVideoResponse generateVideo(
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam("script") String script,
            @RequestParam(value = "voiceType", defaultValue = "female") String voiceType,
            @RequestParam(value = "duration", defaultValue = "20") @Min(5) @Max(300) int duration,
            @RequestParam(value = "format", defaultValue = "mp4") String format
    ) {
        String jobId = videoGenerationService.generateVideo(images, script, voiceType, duration, format);
        return new GenerateVideoResponse(jobId, "processing");
    }

    @GetMapping("/status/{jobId}")
    public StatusResponse getStatus(@PathVariable String jobId) {
        JobStatus status = videoGenerationService.getStatus(jobId);
        return new StatusResponse(status.getStatus().name().toLowerCase(), status.getVideoUrl(), status.getError());
    }
}
