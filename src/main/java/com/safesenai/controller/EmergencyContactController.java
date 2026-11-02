package com.safesenai.controller;

import com.safesenai.dto.ContactDto;
import com.safesenai.model.EmergencyContact;
import com.safesenai.model.User;
import com.safesenai.service.EmergencyContactService;
import com.safesenai.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/contacts")
public class EmergencyContactController {

    private final EmergencyContactService contactService;
    private final UserService userService;
    private final com.safesenai.service.EmailService emailService;

    @Autowired
    public EmergencyContactController(EmergencyContactService contactService,
                                     UserService userService,
                                     com.safesenai.service.EmailService emailService) {
        this.contactService = contactService;
        this.userService = userService;
        this.emailService = emailService;
    }

    @GetMapping
    public String showContacts(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        contactService.seedSampleContactsIfEmpty(user);
        List<EmergencyContact> contacts = contactService.getContactsForUser(user.getId());

        model.addAttribute("currentUser", user);
        model.addAttribute("contacts", contacts);
        model.addAttribute("newContact", new ContactDto());
        model.addAttribute("remainingSlots", 5 - contacts.size());
        model.addAttribute("dispatchedEmails", emailService.getDispatchedEmailPreviews());

        return "contacts";
    }

    @PostMapping("/add")
    public String addContact(@AuthenticationPrincipal UserDetails userDetails,
                             @Valid @ModelAttribute("newContact") ContactDto dto,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please provide a valid name and phone number.");
            return "redirect:/contacts";
        }

        try {
            contactService.addContact(user, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Trusted contact added successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/contacts";
    }

    @PostMapping("/update/{id}")
    public String updateContact(@PathVariable("id") Long contactId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                @Valid @ModelAttribute("contactDto") ContactDto dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid contact name or phone number.");
            return "redirect:/contacts";
        }
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        try {
            contactService.updateContact(contactId, user, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Contact details updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/contacts";
    }

    @PostMapping("/delete/{id}")
    public String deleteContact(@PathVariable("id") Long contactId,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        try {
            contactService.deleteContact(contactId, user.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Emergency contact removed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/contacts";
    }

    @PostMapping("/test-email/{id}")
    public String sendTestEmailToContact(@PathVariable("id") Long contactId,
                                         @AuthenticationPrincipal UserDetails userDetails,
                                         RedirectAttributes redirectAttributes) {
        User user = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        List<EmergencyContact> list = contactService.getContactsForUser(user.getId());
        EmergencyContact target = list.stream().filter(c -> c.getId().equals(contactId)).findFirst().orElse(null);

        if (target == null || target.getEmail() == null || target.getEmail().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Selected contact does not have a registered email address.");
            return "redirect:/contacts";
        }

        String mapsUrl = "https://www.openstreetmap.org/?mlat=10.9574&mlon=78.0818#map=17/10.9574/78.0818";
        emailService.sendDistressEmail(
                target.getEmail().trim(),
                "🚨 DEMO LIVE TEST: Distress Alert for " + user.getFullName(),
                user.getFullName(),
                mapsUrl,
                10.9574,
                78.0818
        );

        redirectAttributes.addFlashAttribute("successMessage", "Distress alert email successfully dispatched to " + target.getContactName() + " (" + target.getEmail() + ")! Check outbox preview below.");
        redirectAttributes.addFlashAttribute("showEmailPreview", true);
        return "redirect:/contacts";
    }
}

