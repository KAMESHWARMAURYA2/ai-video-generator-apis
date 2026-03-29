package com.example.aivideogenerator.platform.api;

import com.example.aivideogenerator.platform.domain.GenerationOutputType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record GenerateContentRequest(
        @NotBlank String prompt,
        @NotEmpty List<GenerationOutputType> outputs
) {
}
