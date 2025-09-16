package se.lexicon.spring_ai_workshop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserRequest(
        @NotNull(message = "Question cannot be null")
        @NotBlank(message = "Question cannot be blank")
        @Size(max = 200, message = "Question cannot exceed 200 characters")
        String question,

        // Expertise level: Beginner, Intermediate, Advanced
        @NotNull(message = "Level cannot be null")
        @NotBlank(message = "Level cannot be blank")
        ExpertiseLevel level
) {
}
