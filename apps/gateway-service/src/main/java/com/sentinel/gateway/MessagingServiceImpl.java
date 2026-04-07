package com.sentinel.gateway;

import com.sentinel.grpc.MessageRequest;
import com.sentinel.grpc.MessageResponse;
import com.sentinel.grpc.MessagingServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.UUID;

@GrpcService
@Slf4j
public class MessagingServiceImpl extends MessagingServiceGrpc.MessagingServiceImplBase {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final StringRedisTemplate redisTemplate;
    private final EncryptionService encryptionService;

    public MessagingServiceImpl(KafkaTemplate<String, String> kafkaTemplate,
                                StringRedisTemplate redisTemplate,
                                EncryptionService encryptionService) {
        this.kafkaTemplate = kafkaTemplate;
        this.redisTemplate = redisTemplate;
        this.encryptionService = encryptionService;
    }

    @Override
    public void sendMessage(MessageRequest request, StreamObserver<MessageResponse> responseObserver) {
        log.info("Received message from {} to {}", request.getSenderId(), request.getReceiverId());

        String messageId = java.util.UUID.randomUUID().toString();

        // 1. Redis: Update user session/last activity
        redisTemplate.opsForValue().set("last_activity:" + request.getSenderId(), String.valueOf(System.currentTimeMillis()));

        // 2. E2EE Simulation: Encrypt content
        String encryptedContent = encryptionService.encrypt(request.getContent());

        // 3. Kafka: Stream message to broker (Event-Driven)
        String messageJson = String.format("{\"id\": \"%s\", \"from\": \"%s\", \"to\": \"%s\", \"content\": \"%s\"}",
                messageId, request.getSenderId(), request.getReceiverId(), encryptedContent);
        kafkaTemplate.send("messages-raw", request.getReceiverId(), messageJson);

        MessageResponse response = MessageResponse.newBuilder()
                .setMessageId(messageId)
                .setStatus("SENT_TO_BROKER")
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
