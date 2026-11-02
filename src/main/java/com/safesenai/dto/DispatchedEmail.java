package com.safesenai.dto;

public class DispatchedEmail {
    private String recipient;
    private String subject;
    private String commuterName;
    private String mapsUrl;
    private double latitude;
    private double longitude;
    private String timestamp;
    private String htmlBody;
    private String deliveryStatus;

    public DispatchedEmail() {
    }

    public DispatchedEmail(String recipient, String subject, String commuterName, String mapsUrl,
                           double latitude, double longitude, String timestamp, String htmlBody, String deliveryStatus) {
        this.recipient = recipient;
        this.subject = subject;
        this.commuterName = commuterName;
        this.mapsUrl = mapsUrl;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.htmlBody = htmlBody;
        this.deliveryStatus = deliveryStatus;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCommuterName() {
        return commuterName;
    }

    public void setCommuterName(String commuterName) {
        this.commuterName = commuterName;
    }

    public String getMapsUrl() {
        return mapsUrl;
    }

    public void setMapsUrl(String mapsUrl) {
        this.mapsUrl = mapsUrl;
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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getHtmlBody() {
        return htmlBody;
    }

    public void setHtmlBody(String htmlBody) {
        this.htmlBody = htmlBody;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }
}

