package com.businessagent.auth.dto.request;

import com.businessagent.auth.model.enums.BusinessStatus;
import com.businessagent.auth.model.enums.Plan;

public record UpdateBusinessRequest(
        Plan plan,
        BusinessStatus status
) {}
