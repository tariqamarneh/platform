package com.businessagent.inbox.controller;

import com.businessagent.inbox.dto.response.ConversationResponse;
import com.businessagent.inbox.dto.response.MessageResponse;
import com.businessagent.inbox.exception.ConversationNotFoundException;
import com.businessagent.inbox.service.ConversationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/conversations")
@RequiredArgsConstructor
@Tag(name = "Conversations", description = "Conversation and message history")
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/{id}")
    @Operation(summary = "Get conversation details")
    public ResponseEntity<ConversationResponse> getConversation(
            @PathVariable UUID id,
            HttpServletRequest httpRequest) {
        UUID businessId = (UUID) httpRequest.getAttribute("businessId");
        ConversationResponse conv = conversationService.getConversation(id);
        if (!conv.businessId().equals(businessId)) {
            throw new ConversationNotFoundException("Conversation not found");
        }
        return ResponseEntity.ok(conv);
    }

    @GetMapping("/{id}/messages")
    @Operation(summary = "Get conversation message history")
    public ResponseEntity<Page<MessageResponse>> getMessages(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            HttpServletRequest httpRequest) {
        UUID businessId = (UUID) httpRequest.getAttribute("businessId");
        ConversationResponse conv = conversationService.getConversation(id);
        if (!conv.businessId().equals(businessId)) {
            throw new ConversationNotFoundException("Conversation not found");
        }
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, 100));
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(conversationService.getConversationMessages(id, pageable));
    }
}
