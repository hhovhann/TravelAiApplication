package am.hhovhann.travel.ai.core.util;

import io.a2a.spec.Message;
import io.a2a.spec.TextPart;

import java.util.UUID;

import static io.a2a.spec.Message.Role.USER;

public class A2AMessageBuilder {

    public static Message createTextMessage(String text) {
        return new Message.Builder()
                .role(USER)
                .messageId(UUID.randomUUID().toString())
                .parts(new TextPart(text, null))
                .build();
    }

    public static Message createTextMessage(String text, String messageId) {
        return new Message.Builder()
                .role(USER)
                .messageId(messageId)
                .parts(new TextPart(text, null))
                .build();
    }
}