package com.safesenai.service.impl;

import com.safesenai.model.User;
import com.safesenai.model.UserLocation;
import com.safesenai.repository.UserLocationRepository;
import com.safesenai.service.UserLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserLocationServiceImpl implements UserLocationService {

    private final UserLocationRepository locationRepo;

    @Autowired
    public UserLocationServiceImpl(UserLocationRepository locationRepo) {
        this.locationRepo = locationRepo;
    }

    @Override
    @Transactional
    public UserLocation recordLocation(User user, double lat, double lon) {
        UserLocation loc = new UserLocation();
        loc.setUser(user);
        loc.setLatitude(lat);
        loc.setLongitude(lon);
        loc.setRecordedAt(LocalDateTime.now());
        loc.setActive(true);
        return locationRepo.save(loc);
    }

    @Override
    public Optional<UserLocation> getLatestLocation(Long userId) {
        return locationRepo.findFirstByUserIdOrderByRecordedAtDesc(userId);
    }
}

