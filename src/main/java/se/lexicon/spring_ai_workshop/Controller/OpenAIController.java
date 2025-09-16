package se.lexicon.spring_ai_workshop.Controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;
import se.lexicon.spring_ai_workshop.Service.OpenAIService;
import se.lexicon.spring_ai_workshop.dto.ChatResponseDto;
import se.lexicon.spring_ai_workshop.dto.UserRequest;

@RestController
@RequestMapping("/api/chat")
public class OpenAIController {
    private final OpenAIService service;

    @Autowired
    public OpenAIController(OpenAIService service) {
        this.service = service;
    }

    /** Simple welcome endpoint */
    @GetMapping
    public String welcome() {
        return "Welcome to the Java Tutor Chatbot API!";
    }

    /** Ask a normal question (level-based) */
    @PostMapping("/ask")
    public ResponseEntity<ChatResponseDto> askQuestion(@Valid @RequestBody UserRequest request) {
        System.out.println("Received question: " + request.question() + ", level: " + request.level().name());
        String answer = service.processSimpleChatQueryWithContext(request.question(), request.level().name());
        return ResponseEntity.ok(new ChatResponseDto(answer));
    }

    /** Ask a single simple question and get a normal response */
    @GetMapping("/messages")
    public String ask(
            @RequestParam
            @NotNull @NotBlank @Size(max = 200)
            String question,
            @RequestParam(defaultValue = "Beginner") String level
    ) {
        System.out.println("Received question: " + question + ", level: " + level);
        return service.processSimpleChatQuery(question);
    }

    /** Ask a simple question and get a streaming response */
    @GetMapping(value = "/messages/stream", produces = MediaType.TEXT_MARKDOWN_VALUE)
    public Flux<String> askStream(
            @RequestParam
            @NotNull @NotBlank @Size(max = 200)
            String question,
            @RequestParam(defaultValue = "Beginner") String level
    ) {
        System.out.println("Received streaming question: " + question + ", level: " + level);
        return service.processSimpleStreamChatQuery(question);
    }

    /** Ask a simple question with context and level awareness */
    @GetMapping("/messages/simple/context")
    public String askSimpleWithContext(
            @RequestParam
            @NotNull @NotBlank @Size(max = 200)
            String question,
            @RequestParam(defaultValue = "Beginner") String level
    ) {
        System.out.println("Received simple context question: " + question + ", level: " + level);
        return service.processSimpleChatQueryWithContext(question, level);
    }

    /** Upload an image and get a description */
    @PostMapping("/image")
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        System.out.println("Received image upload: " + file.getOriginalFilename());
        return service.processImage(file);
    }

    /** Generate an image from a prompt */
    @PostMapping("/image/generate")
    public String generateImage(@RequestParam("prompt") String prompt) {
        System.out.println("Generating image for prompt: " + prompt);
        return service.generateImage(prompt);
    }
}