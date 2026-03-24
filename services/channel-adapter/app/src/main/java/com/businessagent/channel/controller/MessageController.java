package com.businessagent.channel.controller;

import com.businessagent.channel.dto.request.ReadReceiptRequest;
import com.businessagent.channel.dto.request.SendMessageRequest;
import com.businessagent.channel.dto.request.TypingRequest;
import com.businessagent.channel.dto.response.SendMessageResponse;
import com.businessagent.channel.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Send messages, typing indicators, and read receipts")
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/send")
    @Operation(summary = "Send a message via WhatsApp")
    public ResponseEntity<SendMessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(messageService.sendMessage(request));
    }

    @PostMapping("/typing")
    @Operation(summary = "Send typing indicator")
    public ResponseEntity<Void> sendTypingIndicator(@Valid @RequestBody TypingRequest request) {
        messageService.sendTypingIndicator(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/read")
    @Operation(summary = "Mark message as read")
    public ResponseEntity<Void> markAsRead(@Valid @RequestBody ReadReceiptRequest request) {
        messageService.markAsRead(request);
        return ResponseEntity.ok().build();
    }
}
