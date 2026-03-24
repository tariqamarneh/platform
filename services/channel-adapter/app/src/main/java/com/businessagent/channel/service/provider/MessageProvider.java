package com.businessagent.channel.service.provider;

import com.businessagent.channel.dto.request.SendMessageRequest;
import com.businessagent.channel.dto.response.SendMessageResponse;
import com.businessagent.channel.model.Channel;

public interface MessageProvider {
    SendMessageResponse sendMessage(Channel channel, String decryptedApiKey, SendMessageRequest request);
    void sendTypingIndicator(Channel channel, String decryptedApiKey, String to);
    void markAsRead(Channel channel, String decryptedApiKey, String messageId);
}
