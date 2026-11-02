package com.safesenai.service;

import com.safesenai.dto.SafeRouteRequest;
import com.safesenai.dto.SafeRouteResponse;

public interface SafeRouteService {
    SafeRouteResponse calculateSafeRoute(SafeRouteRequest request);
}

