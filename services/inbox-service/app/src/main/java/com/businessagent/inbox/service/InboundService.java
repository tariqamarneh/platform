package com.businessagent.inbox.service;

import com.businessagent.inbox.dto.request.InboundMessageRequest;
import com.businessagent.inbox.dto.response.InboundResult;

public interface InboundService {
    InboundResult processInbound(InboundMessageRequest request, String apiKey);
}
