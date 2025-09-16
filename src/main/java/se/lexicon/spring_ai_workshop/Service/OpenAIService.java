package se.lexicon.spring_ai_workshop.Service;

import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

public interface OpenAIService {
    String processSimpleChatQuery(String question);

    Flux<String> processSimpleStreamChatQuery(String question);

    String processSimpleChatQueryWithContext(String question, String level);

    String processImage(MultipartFile file);

    String generateImage(String question);
}
