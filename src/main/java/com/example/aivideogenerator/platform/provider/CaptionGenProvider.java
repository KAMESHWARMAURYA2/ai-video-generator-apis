package com.example.aivideogenerator.platform.provider;

import java.util.List;

public interface CaptionGenProvider {
    CaptionResult generateCaption(String prompt);

    record CaptionResult(String caption, List<String> hashtags) {
    }
}
