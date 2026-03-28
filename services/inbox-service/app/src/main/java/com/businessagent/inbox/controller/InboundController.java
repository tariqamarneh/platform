package com.businessagent.inbox.controller;

import com.businessagent.inbox.dto.request.InboundMessageRequest;
import com.businessagent.inbox.dto.response.InboundResult;
import com.businessagent.inbox.service.InboundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Inbound", description = "Receive messages from channel-adapter")
public class InboundController {

    private final InboundService inboundService;

    @PostMapping("/inbound")
    @Operation(summary = "Process inbound message from channel-adapter")
    public ResponseEntity<InboundResult> processInbound(
            @Valid @RequestBody InboundMessageRequest request,
            @RequestHeader("X-API-Key") String apiKey) {
        InboundResult result = inboundService.processInbound(request, apiKey);
        return result.success() ? ResponseEntity.ok(result) : ResponseEntity.status(500).body(result);
    }
}
