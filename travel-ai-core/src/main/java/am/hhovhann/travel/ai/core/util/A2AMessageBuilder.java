package am.hhovhann.travel.ai.core.util;

import io.a2a.spec.Message;
import io.a2a.spec.TextPart;

import java.util.UUID;

public class A2AMessageBuilder {

    public static Message createTextMessage(String text) {
        return new Message.Builder()
                .id(UUID.randomUUID().toString())
                .addPart(new TextPart(text, null))
                .build();
    }

    public static Message createTextMessage(String text, String messageId) {
        return new Message.Builder()
                .id(messageId)
                .addPart(new TextPart(text, null))
                .build();
    }
}