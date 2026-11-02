package com.safesenai.service;

import com.safesenai.model.User;
import com.safesenai.model.UserLocation;

import java.util.Optional;

public interface UserLocationService {
    UserLocation recordLocation(User user, double lat, double lon);
    Optional<UserLocation> getLatestLocation(Long userId);
}

