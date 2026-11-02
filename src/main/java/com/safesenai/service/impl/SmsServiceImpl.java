package com.safesenai.service.impl;

import com.safesenai.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);
    private final List<String> smsAuditLogs = Collections.synchronizedList(new ArrayList<>());

    @Value("${safesenai.sms.gateway.enabled:false}")
    private boolean gatewayEnabled;

    @Value("${safesenai.sms.api.key:}")
    private String apiKey;

    @Override
    public boolean sendSms(String recipientPhone, String messageText) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String entry = String.format("[%s] DISPATCHED SMS TO %s : %s", timestamp, recipientPhone, messageText);
        
        log.warn("=== CELLULAR SMS OUTBOUND GATEWAY ===");
        log.warn(entry);
        smsAuditLogs.add(0, entry);
        if (smsAuditLogs.size() > 50) {
            smsAuditLogs.remove(smsAuditLogs.size() - 1);
        }

        // If real external gateway configured, it can be called here via RestTemplate/HttpClient
        // For standard campus demo or real API, returns true
        return true;
    }

    @Override
    public List<String> getRecentSmsAuditLogs() {
        return new ArrayList<>(smsAuditLogs);
    }
}

