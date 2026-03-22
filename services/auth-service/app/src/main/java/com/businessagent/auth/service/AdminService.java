package com.businessagent.auth.service;

import com.businessagent.auth.dto.request.LoginRequest;
import com.businessagent.auth.dto.request.UpdateBusinessRequest;
import com.businessagent.auth.dto.response.AdminAuthResponse;
import com.businessagent.auth.dto.response.AdminBusinessResponse;
import com.businessagent.auth.dto.response.AdminStatsResponse;
import com.businessagent.auth.dto.response.AdminUserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AdminService {
    AdminAuthResponse login(LoginRequest request);
    Page<AdminBusinessResponse> getBusinesses(Pageable pageable);
    AdminBusinessResponse getBusiness(UUID id);
    AdminBusinessResponse updateBusiness(UUID id, UpdateBusinessRequest request);
    Page<AdminUserResponse> getBusinessUsers(UUID businessId, Pageable pageable);
    AdminStatsResponse getStats();
}
