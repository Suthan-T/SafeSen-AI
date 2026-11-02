package com.safesenai.service;

import com.safesenai.dto.DispatchedEmail;
import java.util.List;
import java.util.Optional;

public interface EmailService {
    boolean sendDistressEmail(String recipientEmail, String subject, String commuterName, String mapsUrl, double lat, double lon);
    List<String> getRecentEmailAuditLogs();
    List<DispatchedEmail> getDispatchedEmailPreviews();
    Optional<DispatchedEmail> getLatestDispatchedEmail();
}
