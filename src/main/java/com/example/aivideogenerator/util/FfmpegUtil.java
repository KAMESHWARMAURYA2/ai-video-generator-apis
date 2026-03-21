package com.example.aivideogenerator.util;

import com.example.aivideogenerator.exception.VideoGenerationException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class FfmpegUtil {

    // 🔴 ACTION REQUIRED: Ensure FFmpeg is installed and accessible in PATH
    public void createVideo(Path workingDir, Path audioFile, Path outputVideo, int durationSeconds, int imageCount) {
        int perImageDuration = Math.max(1, durationSeconds / Math.max(1, imageCount));

        List<String> command = new ArrayList<>();
        command.add("ffmpeg");
        command.add("-y");
        command.add("-framerate");
        command.add("1/" + perImageDuration);
        command.add("-i");
        command.add("frame_%03d.png");
        command.add("-i");
        command.add(audioFile.getFileName().toString());
        command.add("-filter_complex");
        command.add("[0:v]scale=1280:720,zoompan=z='min(zoom+0.0015,1.5)':d=125:s=1280x720:fps=25,format=yuv420p[v]");
        command.add("-map");
        command.add("[v]");
        command.add("-map");
        command.add("1:a");
        command.add("-c:v");
        command.add("libx264");
        command.add("-pix_fmt");
        command.add("yuv420p");
        command.add("-t");
        command.add(String.valueOf(durationSeconds));
        command.add("-shortest");
        command.add(outputVideo.getFileName().toString());

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDir.toFile());
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            String output = readOutput(process);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new VideoGenerationException("FFmpeg failed: " + output);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VideoGenerationException("FFmpeg process was interrupted", e);
        } catch (IOException e) {
            throw new VideoGenerationException("Failed to run FFmpeg command", e);
        }
    }

    private String readOutput(Process process) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }
}
