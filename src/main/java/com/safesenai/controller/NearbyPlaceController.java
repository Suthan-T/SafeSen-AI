package com.safesenai.controller;

import com.safesenai.model.NearbyPlace;
import com.safesenai.model.User;
import com.safesenai.service.NearbyPlaceService;
import com.safesenai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class NearbyPlaceController {

    private final NearbyPlaceService nearbyPlaceService;
    private final UserService userService;

    @Autowired
    public NearbyPlaceController(NearbyPlaceService nearbyPlaceService, UserService userService) {
        this.nearbyPlaceService = nearbyPlaceService;
        this.userService = userService;
    }

    @GetMapping("/nearby")
    public String showNearbyPage(@AuthenticationPrincipal UserDetails userDetails,
                                 @RequestParam(value = "lat", defaultValue = "10.9574") double lat,
                                 @RequestParam(value = "lon", defaultValue = "78.0818") double lon,
                                 @RequestParam(value = "radius", defaultValue = "15.0") double radius,
                                 @RequestParam(value = "category", defaultValue = "ALL") String category,
                                 Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElse(null);
        List<NearbyPlace> places = nearbyPlaceService.findNearbyEmergencyPlaces(lat, lon, radius, category);

        model.addAttribute("currentUser", user);
        model.addAttribute("places", places);
        model.addAttribute("currentLat", lat);
        model.addAttribute("currentLon", lon);
        model.addAttribute("currentRadius", radius);
        model.addAttribute("currentCategory", category);

        return "nearby";
    }

    @GetMapping("/api/nearby")
    @ResponseBody
    public ResponseEntity<List<NearbyPlace>> getNearbyPlacesJson(
            @RequestParam(value = "lat", defaultValue = "10.9574") double lat,
            @RequestParam(value = "lon", defaultValue = "78.0818") double lon,
            @RequestParam(value = "radius", defaultValue = "15.0") double radius,
            @RequestParam(value = "category", defaultValue = "ALL") String category) {
        List<NearbyPlace> places = nearbyPlaceService.findNearbyEmergencyPlaces(lat, lon, radius, category);
        return ResponseEntity.ok(places);
    }
}

