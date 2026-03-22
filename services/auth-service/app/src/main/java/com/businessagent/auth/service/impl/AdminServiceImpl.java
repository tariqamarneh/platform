package com.businessagent.auth.service.impl;

import com.businessagent.auth.config.JwtProperties;
import com.businessagent.auth.dto.request.LoginRequest;
import com.businessagent.auth.dto.request.UpdateBusinessRequest;
import com.businessagent.auth.dto.response.AdminAuthResponse;
import com.businessagent.auth.dto.response.AdminBusinessResponse;
import com.businessagent.auth.dto.response.AdminStatsResponse;
import com.businessagent.auth.dto.response.AdminUserResponse;
import com.businessagent.auth.exception.InvalidCredentialsException;
import com.businessagent.auth.exception.ResourceNotFoundException;
import com.businessagent.auth.model.Business;
import com.businessagent.auth.model.SuperAdmin;
import com.businessagent.auth.model.User;
import com.businessagent.auth.model.enums.BusinessStatus;
import com.businessagent.auth.model.enums.Plan;
import com.businessagent.auth.repository.BusinessRepository;
import com.businessagent.auth.repository.SuperAdminRepository;
import com.businessagent.auth.repository.UserRepository;
import com.businessagent.auth.security.JwtProvider;
import com.businessagent.auth.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    private final SuperAdminRepository superAdminRepository;
    private final BusinessRepository businessRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional(readOnly = true)
    public AdminAuthResponse login(LoginRequest request) {
        log.info("Admin login attempt for email={}", request.email());

        SuperAdmin admin = superAdminRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Admin login failed: email not found email={}", request.email());
                    return new InvalidCredentialsException("Invalid email or password");
                });

        if (!passwordEncoder.matches(request.password(), admin.getPasswordHash())) {
            log.warn("Admin login failed: invalid password for email={}", request.email());
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String accessToken = jwtProvider.generateAccessToken(admin);
        long expiresIn = jwtProperties.accessTokenExpiry().getSeconds();

        log.info("Admin login successful: adminId={}", admin.getId());
        return new AdminAuthResponse(accessToken, expiresIn);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminBusinessResponse> getBusinesses(Pageable pageable) {
        log.debug("Admin listing businesses: page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        Page<Business> page = businessRepository.findAll(pageable);

        List<UUID> businessIds = page.getContent().stream()
                .map(Business::getId)
                .toList();

        Map<UUID, Long> userCounts = userRepository.countByBusinessIds(businessIds).stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> (Long) row[1]
                ));

        return page.map(business -> toBusinessResponse(business, userCounts.getOrDefault(business.getId(), 0L)));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminBusinessResponse getBusiness(UUID id) {
        log.debug("Admin getting business: businessId={}", id);
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Admin get business failed: not found businessId={}", id);
                    return new ResourceNotFoundException("Business not found");
                });
        return toBusinessResponse(business);
    }

    @Override
    @Transactional
    public AdminBusinessResponse updateBusiness(UUID id, UpdateBusinessRequest request) {
        log.info("Admin updating business: businessId={}", id);
        Business business = businessRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Admin update business failed: not found businessId={}", id);
                    return new ResourceNotFoundException("Business not found");
                });

        if (request.plan() != null) {
            business.setPlan(request.plan());
        }
        if (request.status() != null) {
            business.setStatus(request.status());
        }

        business = businessRepository.save(business);
        log.info("Admin updated business: businessId={}, plan={}, status={}",
                id, business.getPlan(), business.getStatus());
        return toBusinessResponse(business);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserResponse> getBusinessUsers(UUID businessId, Pageable pageable) {
        log.debug("Admin listing users for businessId={}", businessId);
        if (!businessRepository.existsById(businessId)) {
            throw new ResourceNotFoundException("Business not found");
        }
        return userRepository.findByBusinessId(businessId, pageable).map(this::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminStatsResponse getStats() {
        log.debug("Admin fetching platform stats");
        long totalBusinesses = businessRepository.count();
        long activeBusinesses = businessRepository.countByStatus(BusinessStatus.ACTIVE);
        long suspendedBusinesses = businessRepository.countByStatus(BusinessStatus.SUSPENDED);
        long totalUsers = userRepository.count();
        long freeBusinesses = businessRepository.countByPlan(Plan.FREE);
        long paidBusinesses = businessRepository.countByPlan(Plan.PAID);

        return new AdminStatsResponse(
                totalBusinesses, activeBusinesses, suspendedBusinesses,
                totalUsers, freeBusinesses, paidBusinesses
        );
    }

    private AdminBusinessResponse toBusinessResponse(Business business) {
        return toBusinessResponse(business, userRepository.countByBusinessId(business.getId()));
    }

    private AdminBusinessResponse toBusinessResponse(Business business, long userCount) {
        return new AdminBusinessResponse(
                business.getId(),
                business.getName(),
                business.getPlan(),
                business.getStatus(),
                userCount,
                business.getCreatedAt(),
                business.getUpdatedAt()
        );
    }

    private AdminUserResponse toUserResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
