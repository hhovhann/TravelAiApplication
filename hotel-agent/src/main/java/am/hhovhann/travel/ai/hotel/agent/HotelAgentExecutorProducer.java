package am.hhovhann.travel.ai.hotel.agent;

import io.a2a.server.agentexecution.AgentExecutor;
import io.a2a.server.agentexecution.RequestContext;
import io.a2a.server.events.EventQueue;
import io.a2a.server.tasks.TaskUpdater;
import io.a2a.spec.JSONRPCError;
import io.a2a.spec.Message;
import io.a2a.spec.Part;
import io.a2a.spec.Task;
import io.a2a.spec.TaskNotCancelableError;
import io.a2a.spec.TaskState;
import io.a2a.spec.TextPart;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class HotelAgentExecutorProducer {

    @Inject
    HotelAgent hotelAgent;

    @Produces
    public AgentExecutor agentExecutor() {
        return new HotelAgentExecutor(hotelAgent);
    }

    private static class HotelAgentExecutor implements AgentExecutor {
        private final HotelAgent hotelAgent;

        public HotelAgentExecutor(HotelAgent hotelAgent) {
            this.hotelAgent = hotelAgent;
        }

        @Override
        public void execute(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
            TaskUpdater updater = new TaskUpdater(context, eventQueue);

            try {
                if (context.getTask() == null) {
                    updater.submit();
                }
                updater.startWork();

                String userMessage = extractTextFromMessage(context.getMessage());
                String response = hotelAgent.processHotelRequest(userMessage);

                TextPart responsePart = new TextPart(response, null);
                List<Part<?>> parts = List.of(responsePart);

                updater.addArtifact(parts, null, null, null);
                updater.complete();

            } catch (Exception e) {
                TextPart errorPart = new TextPart("I apologize, but I encountered an error processing your hotel request: " + e.getMessage(), null);
                updater.addArtifact(List.of(errorPart), null, null, null);
                updater.fail("Hotel processing failed: " + e.getMessage());
            }
        }

        @Override
        public void cancel(RequestContext context, EventQueue eventQueue) throws JSONRPCError {
            Task task = context.getTask();
            if (task.getStatus().state() == TaskState.CANCELED) {
                throw new TaskNotCancelableError();
            }
            if (task.getStatus().state() == TaskState.COMPLETED) {
                throw new TaskNotCancelableError();
            }

            TaskUpdater updater = new TaskUpdater(context, eventQueue);
            updater.cancel();
        }

        private String extractTextFromMessage(Message message) {
            StringBuilder textBuilder = new StringBuilder();
            if (message.getParts() != null) {
                for (Part<?> part : message.getParts()) {
                    if (part instanceof TextPart textPart) {
                        textBuilder.append(textPart.getText());
                    }
                }
            }
            return textBuilder.toString();
        }
    }
}

