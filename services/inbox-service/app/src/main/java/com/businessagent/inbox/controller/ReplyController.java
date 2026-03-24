package com.businessagent.inbox.controller;

import com.businessagent.inbox.dto.request.ReplyRequest;
import com.businessagent.inbox.dto.request.TypingRequest;
import com.businessagent.inbox.dto.response.ReplyResult;
import com.businessagent.inbox.service.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Reply", description = "AI engine reply endpoints")
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping("/reply")
    @Operation(summary = "Send AI response to a conversation")
    public ResponseEntity<ReplyResult> sendReply(@Valid @RequestBody ReplyRequest request) {
        ReplyResult result = replyService.sendReply(request);
        return result.success() ? ResponseEntity.ok(result) : ResponseEntity.status(500).body(result);
    }

    @PostMapping("/typing")
    @Operation(summary = "Set typing indicator for a conversation")
    public ResponseEntity<Void> setTypingIndicator(@Valid @RequestBody TypingRequest request) {
        replyService.setTypingIndicator(request);
        return ResponseEntity.ok().build();
    }
}
