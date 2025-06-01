package com.itau.insurance.infrastructure.persistence.repository;

import com.itau.insurance.infrastructure.persistence.entity.PolicyRequestEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


public interface PolicyRequestRepository extends JpaRepository<PolicyRequestEntity, UUID> {

    @EntityGraph(attributePaths = "history")
    Optional<PolicyRequestEntity> findById(UUID id);

    List<PolicyRequestEntity> findByCustomerId(UUID customerId);
}
