package com.safesenai.controller;

import com.safesenai.dto.LocationUpdateDto;
import com.safesenai.dto.SOSRequestDto;
import com.safesenai.dto.SOSResponseDto;
import com.safesenai.model.User;
import com.safesenai.service.SOSService;
import com.safesenai.service.UserLocationService;
import com.safesenai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class SOSController {

    private final SOSService sosService;
    private final UserService userService;
    private final UserLocationService userLocationService;
    private final com.safesenai.service.EmailService emailService;

    @Autowired
    public SOSController(SOSService sosService,
                         UserService userService,
                         UserLocationService userLocationService,
                         com.safesenai.service.EmailService emailService) {
        this.sosService = sosService;
        this.userService = userService;
        this.userLocationService = userLocationService;
        this.emailService = emailService;
    }

    @PostMapping("/sos/trigger")
    public ResponseEntity<SOSResponseDto> triggerEmergency(@AuthenticationPrincipal UserDetails userDetails,
                                                           @RequestBody SOSRequestDto request) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found: " + userDetails.getUsername()));

        SOSResponseDto response = sosService.triggerSOS(
                user,
                request.getLat(),
                request.getLon(),
                request.isVoice(),
                request.getAddress()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/location/update")
    public ResponseEntity<String> updateLocationHeartbeat(@AuthenticationPrincipal UserDetails userDetails,
                                                          @RequestBody LocationUpdateDto updateDto) {
        if (userDetails != null) {
            userService.findByEmail(userDetails.getUsername())
                    .ifPresent(user -> userLocationService.recordLocation(user, updateDto.getLatitude(), updateDto.getLongitude()));
        }
        return ResponseEntity.ok("Location heartbeat recorded");
    }

    @GetMapping("/email/latest")
    public ResponseEntity<com.safesenai.dto.DispatchedEmail> getLatestEmail() {
        return emailService.getLatestDispatchedEmail()
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/email/all")
    public ResponseEntity<java.util.List<com.safesenai.dto.DispatchedEmail>> getAllDispatchedEmails() {
        return ResponseEntity.ok(emailService.getDispatchedEmailPreviews());
    }

    @RequestMapping(value = "/email/send-test", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<java.util.Map<String, Object>> sendManualTestEmail(@AuthenticationPrincipal UserDetails userDetails,
                                                                             @RequestParam(value = "recipient", required = false) String recipient) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        String target = (recipient != null && !recipient.isBlank()) ? recipient.trim() : user.getEmail();
        String mapsUrl = "https://www.openstreetmap.org/?mlat=10.9574&mlon=78.0818#map=17/10.9574/78.0818";
        
        emailService.sendDistressEmail(target, "🚨 DEMO TEST: Emergency Distress Notification", user.getFullName(), mapsUrl, 10.9574, 78.0818);

        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("success", true);
        resp.put("recipient", target);
        resp.put("message", "Test emergency distress alert email successfully triggered!");
        emailService.getLatestDispatchedEmail().ifPresent(email -> resp.put("emailPreview", email));
        return ResponseEntity.ok(resp);
    }
}

