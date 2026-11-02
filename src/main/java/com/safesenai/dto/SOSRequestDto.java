package com.safesenai.dto;

public class SOSRequestDto {
    private double lat;
    private double lon;
    private boolean voice;
    private String address;

    public SOSRequestDto() {
    }

    public SOSRequestDto(double lat, double lon, boolean voice, String address) {
        this.lat = lat;
        this.lon = lon;
        this.voice = voice;
        this.address = address;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public boolean isVoice() {
        return voice;
    }

    public void setVoice(boolean voice) {
        this.voice = voice;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}

