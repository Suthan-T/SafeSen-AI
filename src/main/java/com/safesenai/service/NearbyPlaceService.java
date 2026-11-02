package com.safesenai.service;

import com.safesenai.model.NearbyPlace;
import java.util.List;

public interface NearbyPlaceService {
    List<NearbyPlace> findNearbyEmergencyPlaces(double lat, double lon, double radiusKm, String category);
    List<NearbyPlace> findAllPlaces();
    NearbyPlace findById(Long id);
    NearbyPlace savePlace(NearbyPlace place);
    void deletePlace(Long id);
    double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2);
}

