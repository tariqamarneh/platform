package com.businessagent.auth.controller;

import com.businessagent.auth.dto.request.LoginRequest;
import com.businessagent.auth.dto.request.UpdateBusinessRequest;
import com.businessagent.auth.dto.response.AdminAuthResponse;
import com.businessagent.auth.dto.response.AdminBusinessResponse;
import com.businessagent.auth.dto.response.AdminStatsResponse;
import com.businessagent.auth.dto.response.AdminUserResponse;
import com.businessagent.auth.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Super Admin", description = "Platform administration endpoints")
public class AdminController {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("createdAt", "name", "plan", "status");
    private static final int MAX_PAGE_SIZE = 100;

    private final AdminService adminService;

    @PostMapping("/login")
    @Operation(summary = "Super admin login")
    @SecurityRequirements
    public ResponseEntity<AdminAuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(adminService.login(request));
    }

    @GetMapping("/businesses")
    @Operation(summary = "List all businesses (paginated)")
    public ResponseEntity<Page<AdminBusinessResponse>> getBusinesses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        String safeSortBy = ALLOWED_SORT_FIELDS.contains(sortBy) ? sortBy : "createdAt";
        Sort.Direction safeDirection = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(safeDirection, safeSortBy));
        return ResponseEntity.ok(adminService.getBusinesses(pageable));
    }

    @GetMapping("/businesses/{id}")
    @Operation(summary = "Get business details")
    public ResponseEntity<AdminBusinessResponse> getBusiness(@PathVariable UUID id) {
        return ResponseEntity.ok(adminService.getBusiness(id));
    }

    @PatchMapping("/businesses/{id}")
    @Operation(summary = "Update business plan or status")
    public ResponseEntity<AdminBusinessResponse> updateBusiness(
            @PathVariable UUID id,
            @RequestBody UpdateBusinessRequest request) {
        return ResponseEntity.ok(adminService.updateBusiness(id, request));
    }

    @GetMapping("/businesses/{id}/users")
    @Operation(summary = "List users for a business (paginated)")
    public ResponseEntity<Page<AdminUserResponse>> getBusinessUsers(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        page = Math.max(0, page);
        size = Math.max(1, Math.min(size, MAX_PAGE_SIZE));
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminService.getBusinessUsers(id, pageable));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get platform-wide statistics")
    public ResponseEntity<AdminStatsResponse> getStats() {
        return ResponseEntity.ok(adminService.getStats());
    }
}
