package com.businessagent.auth.config;

import com.businessagent.auth.model.SuperAdmin;
import com.businessagent.auth.repository.SuperAdminRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class SuperAdminSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminSeeder.class);

    private final AdminProperties adminProperties;
    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminProperties.email() == null || adminProperties.email().isBlank()) {
            log.info("No ADMIN_EMAIL configured, skipping super admin seeding");
            return;
        }
        if (adminProperties.password() == null || adminProperties.password().isBlank()) {
            log.warn("ADMIN_EMAIL set but ADMIN_PASSWORD is empty, skipping super admin seeding");
            return;
        }
        if (superAdminRepository.findByEmail(adminProperties.email()).isPresent()) {
            log.info("Super admin already exists, skipping seeding");
            return;
        }

        try {
            SuperAdmin admin = new SuperAdmin();
            admin.setEmail(adminProperties.email());
            admin.setPasswordHash(passwordEncoder.encode(adminProperties.password()));
            superAdminRepository.save(admin);
            log.info("Super admin seeded: email={}", adminProperties.email());
        } catch (DataIntegrityViolationException e) {
            log.info("Super admin already created by another instance, skipping");
        }
    }
}
