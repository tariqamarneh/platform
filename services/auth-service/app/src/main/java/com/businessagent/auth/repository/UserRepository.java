package com.businessagent.auth.repository;

import com.businessagent.auth.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByBusinessId(UUID businessId, Pageable pageable);

    long countByBusinessId(UUID businessId);

    @Query("SELECT u.businessId, COUNT(u) FROM User u WHERE u.businessId IN :ids GROUP BY u.businessId")
    List<Object[]> countByBusinessIds(@Param("ids") Collection<UUID> ids);
}
