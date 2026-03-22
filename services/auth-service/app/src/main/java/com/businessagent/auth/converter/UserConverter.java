package com.businessagent.auth.converter;

import com.businessagent.auth.dto.response.UserResponse;
import com.businessagent.auth.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.getBusinessId()
        );
    }
}
