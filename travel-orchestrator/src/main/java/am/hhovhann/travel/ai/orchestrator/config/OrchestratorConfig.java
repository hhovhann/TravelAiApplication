package am.hhovhann.travel.ai.orchestrator.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrchestratorConfig {

    /**
     * Defines a ChatClient bean.
     * Spring AI's ChatClient requires an underlying ChatModel.
     * If you have a Spring AI starter (e.g., spring-ai-openai-spring-boot-starter)
     * and proper configuration (like 'spring.ai.openai.api-key' in application.properties),
     * Spring Boot will automatically provide a ChatModel bean.
     *
     * @param chatModel The ChatModel bean, automatically injected by Spring.
     * @return A configured ChatClient instance.
     */
    @Bean
    public ChatClient chatClient(ChatModel chatModel) {
        // Use the builder pattern to create a ChatClient instance.
        // The chatModel parameter is automatically injected by Spring,
        // assuming you have the relevant Spring AI provider starter and configuration.
        return ChatClient.builder(chatModel).build();
    }
}
