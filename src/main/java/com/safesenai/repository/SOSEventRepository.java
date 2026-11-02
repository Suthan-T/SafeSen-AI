package com.safesenai.repository;

import com.safesenai.model.SOSEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SOSEventRepository extends JpaRepository<SOSEvent, Long> {
    List<SOSEvent> findByUserIdOrderByTriggeredAtDesc(Long userId);
    List<SOSEvent> findAllByOrderByTriggeredAtDesc();
    long countByStatus(String status);
    List<SOSEvent> findByTriggeredAtAfterOrderByTriggeredAtDesc(LocalDateTime after);
}

