package com.example.aivideogenerator.service;

import com.example.aivideogenerator.exception.VideoGenerationException;
import com.example.aivideogenerator.model.JobState;
import com.example.aivideogenerator.model.JobStatus;
import com.example.aivideogenerator.util.FfmpegUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

@Service
public class VideoGenerationService {

    private final Map<String, JobStatus> jobStatusMap = new ConcurrentHashMap<>();
    private final ExecutorService executorService;
    private final TtsService ttsService;
    private final FfmpegUtil ffmpegUtil;

    @Value("${video.temp-root:/tmp}")
    private String tempRoot;

    @Value("${video.output-dir:./videos}")
    private String outputDir;

    @Value("${server.port:8080}")
    private String serverPort;

    public VideoGenerationService(ExecutorService executorService, TtsService ttsService, FfmpegUtil ffmpegUtil) {
        this.executorService = executorService;
        this.ttsService = ttsService;
        this.ffmpegUtil = ffmpegUtil;
    }

    public String generateVideo(List<MultipartFile> images, String script, String voiceType, int duration, String format) {
        if (images == null || images.isEmpty()) {
            throw new VideoGenerationException("At least one image is required");
        }
        if (script == null || script.isBlank()) {
            throw new VideoGenerationException("Script cannot be empty");
        }
        if (!"mp4".equalsIgnoreCase(format)) {
            throw new VideoGenerationException("Only mp4 format is supported");
        }

        String jobId = UUID.randomUUID().toString();
        jobStatusMap.put(jobId, new JobStatus(JobState.PROCESSING, null, null));

        executorService.submit(() -> processJob(jobId, images, script, voiceType, duration));
        return jobId;
    }

    public JobStatus getStatus(String jobId) {
        JobStatus status = jobStatusMap.get(jobId);
        if (status == null) {
            return new JobStatus(JobState.FAILED, null, "Job not found");
        }
        return status;
    }

    private void processJob(String jobId, List<MultipartFile> images, String script, String voiceType, int duration) {
        Path jobTempDir = Path.of(tempRoot, jobId);
        Path outDir = Path.of(outputDir);
        try {
            Files.createDirectories(jobTempDir);
            Files.createDirectories(outDir);

            saveImages(images, jobTempDir);
            Path audioFile = ttsService.synthesize(script, voiceType, jobTempDir.resolve("audio.mp3"));
            Path finalVideoTemp = jobTempDir.resolve("final.mp4");

            ffmpegUtil.createVideo(jobTempDir, audioFile, finalVideoTemp, duration, images.size());

            Path finalOutput = outDir.resolve(jobId + ".mp4");
            Files.copy(finalVideoTemp, finalOutput, StandardCopyOption.REPLACE_EXISTING);

            String videoUrl = "http://localhost:" + serverPort + "/videos/" + jobId + ".mp4";
            jobStatusMap.put(jobId, new JobStatus(JobState.COMPLETED, videoUrl, null));
        } catch (Exception ex) {
            jobStatusMap.put(jobId, new JobStatus(JobState.FAILED, null, ex.getMessage()));
        } finally {
            cleanup(jobTempDir);
        }
    }

    private void saveImages(List<MultipartFile> images, Path jobTempDir) {
        for (int i = 0; i < images.size(); i++) {
            MultipartFile multipartFile = images.get(i);
            if (multipartFile.isEmpty()) {
                throw new VideoGenerationException("Uploaded image is empty: index " + i);
            }
            try {
                BufferedImage bufferedImage = ImageIO.read(multipartFile.getInputStream());
                if (bufferedImage == null) {
                    throw new VideoGenerationException("Invalid image format at index: " + i);
                }
                Path out = jobTempDir.resolve(String.format("frame_%03d.png", i + 1));
                ImageIO.write(bufferedImage, "png", out.toFile());
            } catch (IOException e) {
                throw new VideoGenerationException("Failed to save uploaded image at index: " + i, e);
            }
        }
    }

    private void cleanup(Path dir) {
        try {
            if (Files.exists(dir)) {
                Files.walk(dir)
                        .sorted((a, b) -> b.getNameCount() - a.getNameCount())
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (IOException ignored) {
                            }
                        });
            }
        } catch (IOException ignored) {
        }
    }
}
