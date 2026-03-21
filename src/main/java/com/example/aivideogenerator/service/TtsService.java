package com.example.aivideogenerator.service;

import com.example.aivideogenerator.exception.VideoGenerationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

@Service
public class TtsService {

    // 🔴 ACTION REQUIRED: Add your TTS API key here
    @Value("${tts.api-key:YOUR_ELEVENLABS_API_KEY}")
    private String ttsApiKey;

    // 🔴 ACTION REQUIRED: Add your TTS voice ID here
    @Value("${tts.voice-id:EXAVITQu4vr4xnSDxMaL}")
    private String voiceId;

    @Value("${tts.url:https://api.elevenlabs.io/v1/text-to-speech}")
    private String ttsBaseUrl;

    private final RestTemplate restTemplate;

    public TtsService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Path synthesize(String script, String voiceType, Path outputFile) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(MediaType.parseMediaTypes("audio/mpeg"));
            headers.set("xi-api-key", ttsApiKey);

            Map<String, Object> payload = Map.of(
                    "text", script,
                    "model_id", "eleven_multilingual_v2",
                    "voice_settings", Map.of(
                            "stability", "female".equalsIgnoreCase(voiceType) ? 0.45 : 0.55,
                            "similarity_boost", 0.75
                    )
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            byte[] audioBytes = restTemplate.postForObject(ttsBaseUrl + "/" + voiceId, entity, byte[].class);

            if (audioBytes == null || audioBytes.length == 0) {
                throw new VideoGenerationException("TTS API returned empty audio data");
            }

            Files.write(outputFile, audioBytes);
            return outputFile;
        } catch (Exception ex) {
            throw new VideoGenerationException("Failed to generate audio via TTS API", ex);
        }
    }
}
