package com.sentinel.search;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/search")
public class SearchController {

    private final MessageRepository messageRepository;

    public SearchController(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @GetMapping("/messages")
    public List<MessageIndex> search(@RequestParam String query) {
        return messageRepository.findByContentContaining(query);
    }
    
    @GetMapping("/history/{userId}")
    public List<MessageIndex> getHistory(@PathVariable String userId) {
        return messageRepository.findBySenderIdOrReceiverId(userId, userId);
    }
}
