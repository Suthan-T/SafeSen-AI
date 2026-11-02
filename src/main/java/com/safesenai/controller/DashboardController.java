package com.safesenai.controller;

import com.safesenai.model.EmergencyContact;
import com.safesenai.model.User;
import com.safesenai.service.EmergencyContactService;
import com.safesenai.service.NearbyPlaceService;
import com.safesenai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final UserService userService;
    private final EmergencyContactService contactService;
    private final NearbyPlaceService nearbyPlaceService;

    @Autowired
    public DashboardController(UserService userService,
                               EmergencyContactService contactService,
                               NearbyPlaceService nearbyPlaceService) {
        this.userService = userService;
        this.contactService = contactService;
        this.nearbyPlaceService = nearbyPlaceService;
    }

    @GetMapping({"/", "/dashboard"})
    public String showDashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }

        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found: " + userDetails.getUsername()));

        // Ensure user has their contacts initialized
        contactService.seedSampleContactsIfEmpty(user);
        List<EmergencyContact> contacts = contactService.getContactsForUser(user.getId());

        model.addAttribute("currentUser", user);
        model.addAttribute("contacts", contacts);
        model.addAttribute("contactCount", contacts.size());
        model.addAttribute("policeCount", 4);
        model.addAttribute("hospitalCount", 5);
        model.addAttribute("safetyScore", 94);

        return "dashboard";
    }
}

