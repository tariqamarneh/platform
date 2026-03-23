package com.businessagent.channel.controller;

import com.businessagent.channel.dto.request.CreateChannelRequest;
import com.businessagent.channel.dto.request.UpdateChannelRequest;
import com.businessagent.channel.dto.response.ChannelResponse;
import com.businessagent.channel.service.ChannelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/channels")
@RequiredArgsConstructor
@Tag(name = "Channels", description = "Channel configuration management")
public class ChannelController {

    private final ChannelService channelService;

    @PostMapping
    @Operation(summary = "Register a new WhatsApp channel")
    public ResponseEntity<ChannelResponse> createChannel(@Valid @RequestBody CreateChannelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(channelService.createChannel(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get channel details")
    public ResponseEntity<ChannelResponse> getChannel(@PathVariable UUID id) {
        return ResponseEntity.ok(channelService.getChannel(id));
    }

    @GetMapping("/business/{businessId}")
    @Operation(summary = "List channels for a business")
    public ResponseEntity<List<ChannelResponse>> getChannelsByBusiness(@PathVariable UUID businessId) {
        return ResponseEntity.ok(channelService.getChannelsByBusiness(businessId));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update channel configuration")
    public ResponseEntity<ChannelResponse> updateChannel(@PathVariable UUID id, @RequestBody UpdateChannelRequest request) {
        return ResponseEntity.ok(channelService.updateChannel(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate a channel")
    public ResponseEntity<Void> deactivateChannel(@PathVariable UUID id) {
        channelService.deactivateChannel(id);
        return ResponseEntity.noContent().build();
    }
}
