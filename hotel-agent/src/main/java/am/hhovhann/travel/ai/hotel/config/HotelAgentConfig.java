package am.hhovhann.travel.ai.hotel.config;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.events.QueueManager;
import io.a2a.server.requesthandlers.DefaultRequestHandler;
import io.a2a.server.requesthandlers.RequestHandler;
import io.a2a.server.tasks.PushNotificationConfigStore;
import io.a2a.server.tasks.PushNotificationSender;
import io.a2a.server.tasks.TaskStore;
import io.a2a.spec.AgentCard;
import io.a2a.transport.jsonrpc.handler.JSONRPCHandler;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;

@Configuration
public class HotelAgentConfig {

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

    @Bean("hotelRequestHandler")
    public RequestHandler hotelRequestHandler(
            @Qualifier("hotelAgentExecutor") AgentExecutor flightAgentExecutor,
            TaskStore taskStore,
            QueueManager queueManager,
            PushNotificationConfigStore pushNotificationConfigStore,
            PushNotificationSender pushNotificationSender,
            @Qualifier("a2aExecutor") Executor executor) {

        return new DefaultRequestHandler(
                flightAgentExecutor,
                taskStore,
                queueManager,
                pushNotificationConfigStore,
                pushNotificationSender,
                executor);
    }

    @Bean("hotelJSONRPCHandler")
    public JSONRPCHandler hotelJSONRPCHandler(
            @Qualifier("hotelAgentCard") AgentCard flightAgentCard,
            @Qualifier("hotelRequestHandler") RequestHandler hotelRequestHandler) {
        return new JSONRPCHandler(flightAgentCard, hotelRequestHandler);
    }
}
