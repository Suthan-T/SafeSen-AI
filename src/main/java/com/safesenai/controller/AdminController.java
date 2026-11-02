package com.safesenai.controller;

import com.safesenai.model.NearbyPlace;
import com.safesenai.model.SOSEvent;
import com.safesenai.model.User;
import com.safesenai.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;
    private final SOSService sosService;
    private final NearbyPlaceService nearbyPlaceService;
    private final AnalyticsService analyticsService;
    private final SmsService smsService;
    private final EmailService emailService;

    @Autowired
    public AdminController(UserService userService,
                           SOSService sosService,
                           NearbyPlaceService nearbyPlaceService,
                           AnalyticsService analyticsService,
                           SmsService smsService,
                           EmailService emailService) {
        this.userService = userService;
        this.sosService = sosService;
        this.nearbyPlaceService = nearbyPlaceService;
        this.analyticsService = analyticsService;
        this.smsService = smsService;
        this.emailService = emailService;
    }

    @GetMapping
    public String showAdminDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User admin = userService.findByEmail(userDetails.getUsername()).orElse(null);
        Map<String, Object> analytics = analyticsService.getDashboardAnalytics();
        List<User> users = userService.findAllUsers();
        List<SOSEvent> sosEvents = sosService.getAllSOSEvents();
        List<NearbyPlace> places = nearbyPlaceService.findAllPlaces();
        List<String> smsLogs = smsService.getRecentSmsAuditLogs();
        List<String> emailLogs = emailService.getRecentEmailAuditLogs();

        model.addAttribute("currentUser", admin);
        model.addAttribute("analytics", analytics);
        model.addAttribute("users", users);
        model.addAttribute("sosEvents", sosEvents);
        model.addAttribute("places", places);
        model.addAttribute("smsLogs", smsLogs);
        model.addAttribute("emailLogs", emailLogs);
        model.addAttribute("dispatchedEmails", emailService.getDispatchedEmailPreviews());
        model.addAttribute("newPlace", new NearbyPlace());

        return "admin";
    }

    @PostMapping("/users/{id}/toggle")
    public String toggleUserStatus(@PathVariable("id") Long userId, RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(userId);
            redirectAttributes.addFlashAttribute("successMessage", "User account status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin#users-section";
    }

    @PostMapping("/sos/{id}/status")
    public String updateSosStatus(@PathVariable("id") Long eventId,
                                  @RequestParam("status") String status,
                                  @RequestParam(value = "notes", required = false) String notes,
                                  RedirectAttributes redirectAttributes) {
        try {
            sosService.updateStatus(eventId, status, notes);
            redirectAttributes.addFlashAttribute("successMessage", "SOS event status changed to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin#sos-section";
    }

    @PostMapping("/places/add")
    public String addEmergencyPlace(@ModelAttribute("newPlace") NearbyPlace place, RedirectAttributes redirectAttributes) {
        try {
            nearbyPlaceService.savePlace(place);
            redirectAttributes.addFlashAttribute("successMessage", "Emergency facility registered into spatial database.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin#places-section";
    }

    @PostMapping("/places/{id}/delete")
    public String deleteEmergencyPlace(@PathVariable("id") Long placeId, RedirectAttributes redirectAttributes) {
        try {
            nearbyPlaceService.deletePlace(placeId);
            redirectAttributes.addFlashAttribute("successMessage", "Emergency facility removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin#places-section";
    }

    @GetMapping("/api/analytics-data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAnalyticsJson() {
        return ResponseEntity.ok(analyticsService.getDashboardAnalytics());
    }
}

