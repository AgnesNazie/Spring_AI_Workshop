package se.lexicon.spring_ai_workshop.Service;

import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.ImageGeneration;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Service
public class OpenAIServiceImpl implements OpenAIService{

        private final OpenAiChatModel openAiChatModel;
        private final OpenAiImageModel openAiImageModel;

        @Autowired
        public OpenAIServiceImpl(OpenAiChatModel openAiChatModel, OpenAiImageModel openAiImageModel) {
            this.openAiChatModel = openAiChatModel;
            this.openAiImageModel = openAiImageModel;
        }

        @Override
        public String processSimpleChatQuery(String question) {
            validateQuestion(question);
            return openAiChatModel.call(question);
        }

        @Override
        public Flux<String> processSimpleStreamChatQuery(String question) {
            validateQuestion(question);
            return openAiChatModel.stream(question);
        }

        @Override
        public String processSimpleChatQueryWithContext(String question, String level) {
            validateQuestion(question);

            SystemMessage systemMessage = SystemMessage.builder()
                    .text(getSystemMessage(level, false))
                    .build();
            UserMessage userMessage = UserMessage.builder().text(question).build();

            OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                    .model("gpt-4.1-mini")
                    .temperature(0.2)
                    .build();

            Prompt prompt = Prompt.builder()
                    .messages(systemMessage, userMessage)
                    .chatOptions(chatOptions)
                    .build();

            ChatResponse chatResponse = openAiChatModel.call(prompt);
            Generation generation = chatResponse.getResult();
            return generation.getOutput().getText();
        }

        @Override
        public String processImage(MultipartFile file) {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("Image cannot be null or empty.");
            }
            if (file.getContentType() == null) {
                throw new IllegalArgumentException("Invalid file type. File must be an image.");
            }

            SystemMessage systemMessage = SystemMessage.builder()
                    .text("You are a helpful assistant that describes the content of an image.")
                    .build();

            Media media = Media.builder().data(file.getResource()).mimeType(MimeTypeUtils.IMAGE_PNG).build();
            UserMessage userMessage = UserMessage.builder()
                    .text("Describe the content of this image")
                    .media(media)
                    .build();

            OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                    .model("gpt-4.1-mini")
                    .temperature(0.3)
                    .build();

            Prompt prompt = Prompt.builder()
                    .messages(systemMessage, userMessage)
                    .chatOptions(chatOptions)
                    .build();

            ChatResponse chatResponse = openAiChatModel.call(prompt);
            Generation generation = chatResponse.getResult();
            return generation.getOutput().getText();
        }

        @Override
        public String generateImage(String question) {
            validateQuestion(question);

            String systemInstruction = String.format("""
                Create a highly detailed, professional image following these specifications:
                Subject: %s
                Technical Guidelines:
                - Avoid text or writing in the image
                - Ensure family-friendly content
                - Focus on clear, sharp details
                - Use balanced color composition
                """, question);

            OpenAiImageOptions imageOptions = OpenAiImageOptions.builder()
                    .model("dall-e-3")
                    .quality("hd")
                    .N(1)
                    .responseFormat("url")
                    .build();

            ImagePrompt imagePrompt = new ImagePrompt(systemInstruction, imageOptions);
            ImageResponse imageResponse = openAiImageModel.call(imagePrompt);
            List<ImageGeneration> generations = imageResponse.getResults();
            ImageGeneration firstImage = generations.get(0);

            String url = firstImage.getOutput().getUrl();
            try (InputStream in = URI.create(url).toURL().openStream()) {
                Files.copy(in, Paths.get("generated_image_" + System.currentTimeMillis() + ".png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return url;
        }

        /** Helper: Returns system message based on level and context */
        private String getSystemMessage(String level, boolean withContext) {
            String baseMessage = switch (level.toLowerCase()) {
                case "beginner" -> "You are JavaTutor. Explain Java concepts simply with examples.";
                case "intermediate" -> "You are JavaTutor. Provide practical Java code examples and best practices.";
                case "advanced" -> "You are JavaTutor. Include in-depth explanations, performance tips, and trade-offs in Java.";
                default -> "You are JavaTutor. Answer the question clearly.";
            };
            if (withContext) {
                baseMessage += " Use previous context if available.";
            }
            return baseMessage;
        }

        /** Helper: Validates question input */
        private void validateQuestion(String question) {
            if (question == null || question.trim().isEmpty()) {
                throw new IllegalArgumentException("Question cannot be null or empty.");
            }
        }
    }