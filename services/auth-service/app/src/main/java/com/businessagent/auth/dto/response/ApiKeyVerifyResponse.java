package com.businessagent.auth.dto.response;

import java.io.Serializable;
import java.util.UUID;

public record ApiKeyVerifyResponse(
        boolean valid,
        UUID businessId
) implements Serializable {
}
