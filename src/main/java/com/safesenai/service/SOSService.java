package com.safesenai.service;

import com.safesenai.dto.SOSResponseDto;
import com.safesenai.model.SOSEvent;
import com.safesenai.model.User;

import java.util.List;

public interface SOSService {
    SOSResponseDto triggerSOS(User user, double lat, double lon, boolean voiceTriggered, String address);
    List<SOSEvent> getUserSOSEvents(Long userId);
    List<SOSEvent> getAllSOSEvents();
    SOSEvent updateStatus(Long eventId, String status, String notes);
    long countActiveAlerts();
    long countTotalAlerts();
}

