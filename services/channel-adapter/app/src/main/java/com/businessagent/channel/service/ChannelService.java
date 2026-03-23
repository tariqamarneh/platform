package com.businessagent.channel.service;

import com.businessagent.channel.dto.request.CreateChannelRequest;
import com.businessagent.channel.dto.request.UpdateChannelRequest;
import com.businessagent.channel.dto.response.ChannelCreatedResponse;
import com.businessagent.channel.dto.response.ChannelResponse;

import java.util.List;
import java.util.UUID;

public interface ChannelService {
    ChannelCreatedResponse createChannel(CreateChannelRequest request);
    ChannelResponse getChannel(UUID id);
    List<ChannelResponse> getChannelsByBusiness(UUID businessId);
    ChannelResponse updateChannel(UUID id, UpdateChannelRequest request);
    void deactivateChannel(UUID id);
}
