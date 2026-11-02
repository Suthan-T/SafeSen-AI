package com.safesenai.dto;

import com.safesenai.model.NearbyPlace;
import java.util.ArrayList;
import java.util.List;

public class AIRouteOption {
    private String id;
    private String title;
    private String routeType; // RECOMMENDED, BALANCED, FASTEST
    private String geometry; // GeoJSON geometry object string
    private double distanceKm;
    private double durationMin;
    private double safetyScore; // 0 - 100
    private String riskLevel; // LOW RISK, MODERATE RISK, HIGH RISK
    private double confidencePercent;
    private String lightingCondition;
    private long policeCount;
    private long hospitalCount;
    private List<NearbyPlace> emergencyPois = new ArrayList<>();

    public AIRouteOption() {
    }

    public AIRouteOption(String id, String title, String routeType, String geometry, double distanceKm, double durationMin,
                         double safetyScore, String riskLevel, double confidencePercent, String lightingCondition,
                         long policeCount, long hospitalCount, List<NearbyPlace> emergencyPois) {
        this.id = id;
        this.title = title;
        this.routeType = routeType;
        this.geometry = geometry;
        this.distanceKm = distanceKm;
        this.durationMin = durationMin;
        this.safetyScore = safetyScore;
        this.riskLevel = riskLevel;
        this.confidencePercent = confidencePercent;
        this.lightingCondition = lightingCondition;
        this.policeCount = policeCount;
        this.hospitalCount = hospitalCount;
        this.emergencyPois = emergencyPois != null ? emergencyPois : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRouteType() {
        return routeType;
    }

    public void setRouteType(String routeType) {
        this.routeType = routeType;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(double distanceKm) {
        this.distanceKm = distanceKm;
    }

    public double getDurationMin() {
        return durationMin;
    }

    public void setDurationMin(double durationMin) {
        this.durationMin = durationMin;
    }

    public double getSafetyScore() {
        return safetyScore;
    }

    public void setSafetyScore(double safetyScore) {
        this.safetyScore = safetyScore;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public double getConfidencePercent() {
        return confidencePercent;
    }

    public void setConfidencePercent(double confidencePercent) {
        this.confidencePercent = confidencePercent;
    }

    public String getLightingCondition() {
        return lightingCondition;
    }

    public void setLightingCondition(String lightingCondition) {
        this.lightingCondition = lightingCondition;
    }

    public long getPoliceCount() {
        return policeCount;
    }

    public void setPoliceCount(long policeCount) {
        this.policeCount = policeCount;
    }

    public long getHospitalCount() {
        return hospitalCount;
    }

    public void setHospitalCount(long hospitalCount) {
        this.hospitalCount = hospitalCount;
    }

    public List<NearbyPlace> getEmergencyPois() {
        return emergencyPois;
    }

    public void setEmergencyPois(List<NearbyPlace> emergencyPois) {
        this.emergencyPois = emergencyPois;
    }
}

