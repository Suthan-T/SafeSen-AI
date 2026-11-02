package com.safesenai.service;

import com.safesenai.dto.ContactDto;
import com.safesenai.model.EmergencyContact;
import com.safesenai.model.User;

import java.util.List;

public interface EmergencyContactService {
    List<EmergencyContact> getContactsForUser(Long userId);
    EmergencyContact addContact(User user, ContactDto dto);
    EmergencyContact updateContact(Long contactId, User user, ContactDto dto);
    void deleteContact(Long contactId, Long userId);
    void seedSampleContactsIfEmpty(User user);
}

