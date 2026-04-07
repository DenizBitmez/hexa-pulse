package com.sentinel.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface MessageRepository extends ElasticsearchRepository<MessageIndex, String> {
    List<MessageIndex> findByContentContaining(String content);
    List<MessageIndex> findBySenderIdOrReceiverId(String senderId, String receiverId);
}
