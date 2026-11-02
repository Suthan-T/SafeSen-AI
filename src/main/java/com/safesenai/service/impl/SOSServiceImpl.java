package com.safesenai.service.impl;

import com.safesenai.dto.SOSResponseDto;
import com.safesenai.model.EmergencyContact;
import com.safesenai.model.SOSEvent;
import com.safesenai.model.User;
import com.safesenai.repository.EmergencyContactRepository;
import com.safesenai.repository.SOSEventRepository;
import com.safesenai.service.EmailService;
import com.safesenai.service.SOSService;
import com.safesenai.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

@Service
public class SOSServiceImpl implements SOSService {

    private static final Logger log = LoggerFactory.getLogger(SOSServiceImpl.class);

    private final SOSEventRepository sosRepo;
    private final EmergencyContactRepository contactRepo;
    private final SmsService smsService;
    private final EmailService emailService;

    @Autowired
    public SOSServiceImpl(SOSEventRepository sosRepo,
                          EmergencyContactRepository contactRepo,
                          SmsService smsService,
                          EmailService emailService) {
        this.sosRepo = sosRepo;
        this.contactRepo = contactRepo;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public SOSResponseDto triggerSOS(User user, double lat, double lon, boolean voiceTriggered, String address) {
        SOSEvent event = new SOSEvent();
        event.setUser(user);
        event.setLatitude(lat);
        event.setLongitude(lon);
        event.setTriggeredAt(LocalDateTime.now());
        event.setStatus("TRIGGERED");
        event.setVoiceTriggered(voiceTriggered);
        event.setAddress(address != null && !address.trim().isEmpty() ? address : "Live Commuter Coordinates (" + lat + ", " + lon + ")");

        SOSEvent saved = sosRepo.save(event);

        List<EmergencyContact> contacts = contactRepo.findByUserIdOrderByPriorityLevelAsc(user.getId());
        String mapsUrl = String.format(Locale.US, "https://www.openstreetmap.org/?mlat=%.6f&mlon=%.6f#map=17/%.6f/%.6f", lat, lon, lat, lon);
        String triggerSource = voiceTriggered ? "VOICE PANIC DETECTION (Hands-Free)" : "ONE-TOUCH SOS BUTTON";
        String alertMsg = String.format("EMERGENCY SOS ALERT! %s triggered distress alert via %s! Live GPS: %s",
                user.getFullName(), triggerSource, mapsUrl);

        // High-Speed Asynchronous Notification Execution
        CompletableFuture.runAsync(() -> {
            log.warn("Broadcasting distress alerts to {} emergency contacts for user {}", contacts.size(), user.getEmail());
            for (EmergencyContact contact : contacts) {
                // SMS dispatch
                smsService.sendSms(contact.getContactPhone(), alertMsg);
                // Email dispatch
                if (contact.getEmail() != null && !contact.getEmail().isBlank()) {
                    emailService.sendDistressEmail(contact.getEmail(),
                            "URGENT DISTRESS ALERT: " + user.getFullName(),
                            user.getFullName(),
                            mapsUrl,
                            lat,
                            lon);
                }
            }
        });

        return new SOSResponseDto(
                saved.getId(),
                saved.getStatus(),
                "Distress signal registered and broadcasted to " + contacts.size() + " trusted emergency contacts.",
                mapsUrl,
                contacts.size(),
                saved.getTriggeredAt(),
                voiceTriggered
        );
    }

    @Override
    public List<SOSEvent> getUserSOSEvents(Long userId) {
        return sosRepo.findByUserIdOrderByTriggeredAtDesc(userId);
    }

    @Override
    public List<SOSEvent> getAllSOSEvents() {
        return sosRepo.findAllByOrderByTriggeredAtDesc();
    }

    @Override
    @Transactional
    public SOSEvent updateStatus(Long eventId, String status, String notes) {
        SOSEvent event = sosRepo.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("SOS Event not found: " + eventId));
        event.setStatus(status);
        if (notes != null) {
            event.setNotes(notes);
        }
        return sosRepo.save(event);
    }

    @Override
    public long countActiveAlerts() {
        return sosRepo.countByStatus("TRIGGERED") + sosRepo.countByStatus("ACTIVE");
    }

    @Override
    public long countTotalAlerts() {
        return sosRepo.count();
    }
}

