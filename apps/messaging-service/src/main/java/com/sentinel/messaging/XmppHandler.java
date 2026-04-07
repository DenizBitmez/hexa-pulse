package com.sentinel.messaging;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ChannelHandler.Sharable
@Slf4j
public class XmppHandler extends SimpleChannelInboundHandler<String> {

    private final org.springframework.data.redis.core.StringRedisTemplate redisTemplate;

    public XmppHandler(org.springframework.data.redis.core.StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Map to keep track of Online users (User ID -> Netty Channel)
    private static final Map<String, Channel> userChannels = new ConcurrentHashMap<>();

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        log.info("Received from client: {}", msg);
        if (msg.startsWith("ID:")) {
            String userId = msg.substring(3).trim();
            userChannels.put(userId, ctx.channel());
            
            // Update Redis status
            redisTemplate.opsForValue().set("status:" + userId, "ONLINE");
            
            log.info("User {} is now online", userId);
            ctx.writeAndFlush("ACK:REGISTERED\n");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Netty handler error", cause);
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        userChannels.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(ctx.channel())) {
                redisTemplate.delete("status:" + entry.getKey());
                log.info("User {} disconnected", entry.getKey());
                return true;
            }
            return false;
        });
    }

    public void sendMessageToUser(String userId, String message) {
        Channel channel = userChannels.get(userId);
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message + "\n");
        } else {
            log.warn("User {} is offline, cannot deliver message", userId);
        }
    }
}
