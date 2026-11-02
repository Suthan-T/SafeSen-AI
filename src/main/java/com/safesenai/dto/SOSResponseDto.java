package com.safesenai.dto;

import java.time.LocalDateTime;

public class SOSResponseDto {
    private Long id;
    private String status;
    private String message;
    private String mapsUrl;
    private int alertsSentCount;
    private LocalDateTime timestamp;
    private boolean voiceTriggered;

    public SOSResponseDto() {
    }

    public SOSResponseDto(Long id, String status, String message, String mapsUrl, int alertsSentCount, LocalDateTime timestamp, boolean voiceTriggered) {
        this.id = id;
        this.status = status;
        this.message = message;
        this.mapsUrl = mapsUrl;
        this.alertsSentCount = alertsSentCount;
        this.timestamp = timestamp;
        this.voiceTriggered = voiceTriggered;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMapsUrl() {
        return mapsUrl;
    }

    public void setMapsUrl(String mapsUrl) {
        this.mapsUrl = mapsUrl;
    }

    public int getAlertsSentCount() {
        return alertsSentCount;
    }

    public void setAlertsSentCount(int alertsSentCount) {
        this.alertsSentCount = alertsSentCount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isVoiceTriggered() {
        return voiceTriggered;
    }

    public void setVoiceTriggered(boolean voiceTriggered) {
        this.voiceTriggered = voiceTriggered;
    }
}

