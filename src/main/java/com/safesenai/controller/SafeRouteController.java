package com.safesenai.controller;

import com.safesenai.dto.SafeRouteRequest;
import com.safesenai.dto.SafeRouteResponse;
import com.safesenai.model.User;
import com.safesenai.service.SafeRouteService;
import com.safesenai.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class SafeRouteController {

    private final SafeRouteService safeRouteService;
    private final UserService userService;

    @Autowired
    public SafeRouteController(SafeRouteService safeRouteService, UserService userService) {
        this.safeRouteService = safeRouteService;
        this.userService = userService;
    }

    @GetMapping("/safe-route")
    public String showSafeRoutePage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElse(null);
        model.addAttribute("currentUser", user);
        return "safe-route";
    }

    @PostMapping("/api/routes/calculate")
    @ResponseBody
    public ResponseEntity<SafeRouteResponse> calculateRoute(@RequestBody SafeRouteRequest request) {
        SafeRouteResponse response = safeRouteService.calculateSafeRoute(request);
        return ResponseEntity.ok(response);
    }
}

