package com.safesenai.repository;

import com.safesenai.model.EmergencyContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyContactRepository extends JpaRepository<EmergencyContact, Long> {
    List<EmergencyContact> findByUserIdOrderByPriorityLevelAsc(Long userId);
    List<EmergencyContact> findByUserEmailOrderByPriorityLevelAsc(String email);
    long countByUserId(Long userId);
}

