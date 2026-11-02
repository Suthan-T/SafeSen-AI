package com.safesenai.dto;

import java.util.ArrayList;
import java.util.List;

public class SafeRouteResponse {
    private AIRouteOption recommendedRoute;
    private List<AIRouteOption> allRoutes = new ArrayList<>();
    private String originAddress;
    private String destinationAddress;

    public SafeRouteResponse() {
    }

    public SafeRouteResponse(AIRouteOption recommendedRoute, List<AIRouteOption> allRoutes) {
        this.recommendedRoute = recommendedRoute;
        this.allRoutes = allRoutes != null ? allRoutes : new ArrayList<>();
    }

    public AIRouteOption getRecommendedRoute() {
        return recommendedRoute;
    }

    public void setRecommendedRoute(AIRouteOption recommendedRoute) {
        this.recommendedRoute = recommendedRoute;
    }

    public List<AIRouteOption> getAllRoutes() {
        return allRoutes;
    }

    public void setAllRoutes(List<AIRouteOption> allRoutes) {
        this.allRoutes = allRoutes;
    }

    public String getOriginAddress() {
        return originAddress;
    }

    public void setOriginAddress(String originAddress) {
        this.originAddress = originAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }
}

