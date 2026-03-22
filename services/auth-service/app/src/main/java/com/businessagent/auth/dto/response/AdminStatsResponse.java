package com.businessagent.auth.dto.response;

public record AdminStatsResponse(
        long totalBusinesses,
        long activeBusinesses,
        long suspendedBusinesses,
        long totalUsers,
        long freeBusinesses,
        long paidBusinesses
) {}
