package com.safesenai.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sos_events")
public class SOSEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Column(name = "triggered_at", nullable = false)
    private LocalDateTime triggeredAt = LocalDateTime.now();

    @Column(length = 20, nullable = false)
    private String status = "TRIGGERED"; // TRIGGERED, ACTIVE, RESOLVED

    @Column(name = "voice_triggered")
    private boolean voiceTriggered = false;

    @Column(length = 255)
    private String address;

    @Column(length = 500)
    private String notes;

    public SOSEvent() {
    }

    public SOSEvent(User user, double latitude, double longitude, LocalDateTime triggeredAt, String status, boolean voiceTriggered) {
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.triggeredAt = triggeredAt;
        this.status = status;
        this.voiceTriggered = voiceTriggered;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getTriggeredAt() {
        return triggeredAt;
    }

    public void setTriggeredAt(LocalDateTime triggeredAt) {
        this.triggeredAt = triggeredAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isVoiceTriggered() {
        return voiceTriggered;
    }

    public void setVoiceTriggered(boolean voiceTriggered) {
        this.voiceTriggered = voiceTriggered;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}

