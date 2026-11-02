package com.safesenai.service;

import java.util.List;

public interface SmsService {
    boolean sendSms(String recipientPhone, String messageText);
    List<String> getRecentSmsAuditLogs();
}

