package com.businessagent.auth.converter;

import com.businessagent.auth.dto.response.AdminBusinessResponse;
import com.businessagent.auth.dto.response.AdminUserResponse;
import com.businessagent.auth.model.Business;
import com.businessagent.auth.model.User;
import org.springframework.stereotype.Component;

@Component
public class AdminConverter {

    public AdminBusinessResponse toBusinessResponse(Business business, long userCount) {
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

    public AdminUserResponse toUserResponse(User user) {
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
