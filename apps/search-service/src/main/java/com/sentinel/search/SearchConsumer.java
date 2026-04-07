package com.sentinel.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class SearchConsumer {

    private final MessageRepository messageRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public SearchConsumer(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @KafkaListener(topics = "messages-raw", groupId = "search-group")
    public void consume(String messageJson) {
        log.info("Search Service consuming message for indexing: {}", messageJson);
        try {
            @SuppressWarnings("unchecked")
            Map<String, String> data = objectMapper.readValue(messageJson, Map.class);
            
            MessageIndex messageIndex = MessageIndex.builder()
                    .id(data.get("id"))
                    .senderId(data.get("from"))
                    .receiverId(data.get("to"))
                    .content(data.get("content"))
                    .timestamp(System.currentTimeMillis())
                    .build();

            messageRepository.save(messageIndex);
            log.info("Message indexed successfully: {}", messageIndex.getId());
            
        } catch (Exception e) {
            log.error("Error indexing message to ElasticSearch", e);
        }
    }
}
