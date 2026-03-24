package com.businessagent.inbox.worker;

import com.businessagent.inbox.config.AppProperties;
import com.businessagent.inbox.service.OutboundQueueService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OutboundWorker {

    private static final Logger log = LoggerFactory.getLogger(OutboundWorker.class);
    private final OutboundQueueService queueService;
    private final OutboundMessageProcessor messageProcessor;
    private final AppProperties appProperties;

    @Scheduled(fixedDelayString = "${app.outbound.poll-interval-ms}")
    public void pollQueues() {
        Set<String> keys = queueService.getActiveQueueKeys();

        for (String queueKey : keys) {
            String channelId = queueKey.substring(queueKey.lastIndexOf(':') + 1);
            processQueue(channelId);
        }
    }

    private void processQueue(String channelId) {
        List<String> messageIds = queueService.dequeue(channelId, appProperties.outbound().batchSize());

        for (String messageId : messageIds) {
            messageProcessor.processMessage(channelId, messageId);
        }
    }
}
