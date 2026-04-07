package com.sentinel.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

@Component
@Slf4j
public class MessageConsumer {

    private final XmppHandler xmppHandler;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageConsumer(XmppHandler xmppHandler) {
        this.xmppHandler = xmppHandler;
    }

    @KafkaListener(topics = "messages-raw", groupId = "messaging-group")
    public void consume(String messageJson) {
        log.info("Consumed message from Kafka: {}", messageJson);
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> data = objectMapper.readValue(messageJson, Map.class);
            String receiverId = data.get("to");
            String content = data.get("content");
            String senderId = data.get("from");

            // Forward to Netty channel
            String formattedMsg = String.format("MSG_FROM:%s:%s", senderId, content);
            xmppHandler.sendMessageToUser(receiverId, formattedMsg);
            
        } catch (Exception e) {
            log.error("Error parsing message JSON", e);
        }
    }
}
