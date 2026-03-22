package com.businessagent.auth.repository;

import com.businessagent.auth.model.Business;
import com.businessagent.auth.model.enums.BusinessStatus;
import com.businessagent.auth.model.enums.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BusinessRepository extends JpaRepository<Business, UUID> {

    long countByStatus(BusinessStatus status);

    long countByPlan(Plan plan);
}
