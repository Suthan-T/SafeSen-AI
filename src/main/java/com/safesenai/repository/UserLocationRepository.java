package com.safesenai.repository;

import com.safesenai.model.UserLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, Long> {
    List<UserLocation> findByUserIdOrderByRecordedAtDesc(Long userId);
    Optional<UserLocation> findFirstByUserIdOrderByRecordedAtDesc(Long userId);
}

