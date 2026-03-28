package com.businessagent.inbox.service;

import com.businessagent.inbox.config.AppProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OutboundQueueService {

    private static final Logger log = LoggerFactory.getLogger(OutboundQueueService.class);
    private final StringRedisTemplate redisTemplate;
    private final AppProperties appProperties;

    public void enqueue(String channelId, String messageId) {
        String queueKey = buildQueueKey(channelId);
        redisTemplate.opsForList().rightPush(queueKey, messageId);
        log.debug("Enqueued message: queue={}, messageId={}", queueKey, messageId);
    }

    public List<String> dequeue(String channelId, int batchSize) {
        String queueKey = buildQueueKey(channelId);
        List<String> messageIds = new ArrayList<>();
        for (int i = 0; i < batchSize; i++) {
            String messageId = redisTemplate.opsForList().leftPop(queueKey);
            if (messageId == null) break;
            messageIds.add(messageId);
        }
        return messageIds;
    }

    public void requeue(String channelId, String messageId) {
        String queueKey = buildQueueKey(channelId);
        redisTemplate.opsForList().leftPush(queueKey, messageId);
        log.debug("Re-queued message: queue={}, messageId={}", queueKey, messageId);
    }

    public Set<String> getActiveQueueKeys() {
        Set<String> keys = new java.util.HashSet<>();
        String pattern = appProperties.outbound().queuePrefix() + ":*";

        try (var cursor = redisTemplate.scan(org.springframework.data.redis.core.ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build())) {
            cursor.forEachRemaining(keys::add);
        }

        return keys;
    }

    private String buildQueueKey(String channelId) {
        return appProperties.outbound().queuePrefix() + ":" + channelId;
    }
}
