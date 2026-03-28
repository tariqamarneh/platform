package com.businessagent.inbox.service;

import com.businessagent.inbox.dto.request.ReplyRequest;
import com.businessagent.inbox.dto.request.TypingRequest;
import com.businessagent.inbox.dto.response.ReplyResult;

public interface ReplyService {
    ReplyResult sendReply(ReplyRequest request);
    void setTypingIndicator(TypingRequest request);
}
