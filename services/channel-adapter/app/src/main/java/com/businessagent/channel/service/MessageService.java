package com.businessagent.channel.service;

import com.businessagent.channel.dto.request.ReadReceiptRequest;
import com.businessagent.channel.dto.request.SendMessageRequest;
import com.businessagent.channel.dto.request.TypingRequest;
import com.businessagent.channel.dto.response.SendMessageResponse;

public interface MessageService {
    SendMessageResponse sendMessage(SendMessageRequest request);
    void sendTypingIndicator(TypingRequest request);
    void markAsRead(ReadReceiptRequest request);
}
