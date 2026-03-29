package com.example.aivideogenerator.platform.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.UUID;

@Configuration
public class OpenSourceProviderConfig {

    @Bean
    public ImageGenProvider imageGenProvider() {
        return prompt -> "s3://generated/free/images/" + UUID.randomUUID() + ".png";
    }

    @Bean
    public VideoGenProvider videoGenProvider() {
        return prompt -> "s3://generated/free/videos/" + UUID.randomUUID() + ".mp4";
    }

    @Bean
    public CaptionGenProvider captionGenProvider() {
        return prompt -> new CaptionGenProvider.CaptionResult(
                "Generated (free-tier) caption for: " + prompt,
                List.of("#ai", "#automation", "#content")
        );
    }

    @Bean
    public PublisherProvider publisherProvider() {
        return (socialAccountId, caption, assetUrl) -> "ext_" + UUID.randomUUID();
    }
}
