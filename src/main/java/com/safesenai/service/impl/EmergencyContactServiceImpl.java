package com.safesenai.service.impl;

import com.safesenai.dto.ContactDto;
import com.safesenai.model.EmergencyContact;
import com.safesenai.model.User;
import com.safesenai.repository.EmergencyContactRepository;
import com.safesenai.service.EmergencyContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmergencyContactServiceImpl implements EmergencyContactService {

    private final EmergencyContactRepository contactRepo;

    @Autowired
    public EmergencyContactServiceImpl(EmergencyContactRepository contactRepo) {
        this.contactRepo = contactRepo;
    }

    @Override
    public List<EmergencyContact> getContactsForUser(Long userId) {
        return contactRepo.findByUserIdOrderByPriorityLevelAsc(userId);
    }

    @Override
    @Transactional
    public EmergencyContact addContact(User user, ContactDto dto) {
        long currentCount = contactRepo.countByUserId(user.getId());
        if (currentCount >= 5) {
            throw new IllegalStateException("Maximum limit of 5 Favorite Trusted Emergency Contacts reached. Please edit or delete an existing contact.");
        }

        EmergencyContact contact = new EmergencyContact();
        contact.setUser(user);
        contact.setContactName(dto.getContactName().trim());
        contact.setContactPhone(dto.getContactPhone().trim());
        contact.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        contact.setRelationship(dto.getRelationship() != null ? dto.getRelationship().trim() : "Family");
        contact.setPriorityLevel(dto.getPriorityLevel() > 0 ? dto.getPriorityLevel() : (int) currentCount + 1);

        return contactRepo.save(contact);
    }

    @Override
    @Transactional
    public EmergencyContact updateContact(Long contactId, User user, ContactDto dto) {
        EmergencyContact contact = contactRepo.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + contactId));

        if (!contact.getUser().getId().equals(user.getId())) {
            throw new SecurityException("Unauthorized to modify this contact.");
        }

        contact.setContactName(dto.getContactName().trim());
        contact.setContactPhone(dto.getContactPhone().trim());
        contact.setEmail(dto.getEmail() != null ? dto.getEmail().trim() : null);
        contact.setRelationship(dto.getRelationship() != null ? dto.getRelationship().trim() : "Family");
        if (dto.getPriorityLevel() > 0) {
            contact.setPriorityLevel(dto.getPriorityLevel());
        }

        return contactRepo.save(contact);
    }

    @Override
    @Transactional
    public void deleteContact(Long contactId, Long userId) {
        EmergencyContact contact = contactRepo.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found: " + contactId));

        if (!contact.getUser().getId().equals(userId)) {
            throw new SecurityException("Unauthorized to delete this contact.");
        }

        contactRepo.delete(contact);
    }

    @Override
    @Transactional
    public void seedSampleContactsIfEmpty(User user) {
        List<EmergencyContact> existing = contactRepo.findByUserIdOrderByPriorityLevelAsc(user.getId());
        if (existing.isEmpty()) {
            contactRepo.save(new EmergencyContact(user, "Suthan T (Primary Emergency Contact)", "+91 922524104168", "suthanthadeus2006@gmail.com", "Self / Primary Contact", 1));
            contactRepo.save(new EmergencyContact(user, "R. Thangavel (Father)", "+91 9443200002", "family.suthan@gmail.com", "Parent", 2));
            contactRepo.save(new EmergencyContact(user, "E. Tamil Iniyan (Project Co-Author)", "+91 9789100003", "tamiliniyan@vsb.ac.in", "Colleague", 3));
            contactRepo.save(new EmergencyContact(user, "VSB Campus Women Security Desk", "1091", "securitydesk@vsb.ac.in", "Institutional Security", 4));
            contactRepo.save(new EmergencyContact(user, "Local Police Control Room", "112", "police.emergency@tn.gov.in", "Police Helpline", 5));
        } else {
            // Ensure contact #1 has the user's active email for instant testing
            EmergencyContact first = existing.get(0);
            if (first.getEmail() == null || first.getEmail().contains("supervisor@vsb.ac.in")) {
                first.setEmail("suthanthadeus2006@gmail.com");
                first.setContactName("Suthan T (Primary Emergency Contact)");
                contactRepo.save(first);
            }
        }
    }
}

