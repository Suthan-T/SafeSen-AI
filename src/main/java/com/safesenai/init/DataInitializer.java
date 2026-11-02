package com.safesenai.init;

import com.safesenai.model.NearbyPlace;
import com.safesenai.model.Role;
import com.safesenai.model.User;
import com.safesenai.repository.NearbyPlaceRepository;
import com.safesenai.repository.RoleRepository;
import com.safesenai.repository.UserRepository;
import com.safesenai.service.EmergencyContactService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final NearbyPlaceRepository nearbyPlaceRepository;
    private final EmergencyContactService contactService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(RoleRepository roleRepository,
                           UserRepository userRepository,
                           NearbyPlaceRepository nearbyPlaceRepository,
                           EmergencyContactService contactService,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.nearbyPlaceRepository = nearbyPlaceRepository;
        this.contactService = contactService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Initializing SafeSen-AI database master records...");

        // 1. Roles
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER", "Standard Commuter User")));
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN", "System Administrator & Safety Coordinator")));

        // 2. Default Administrator
        if (!userRepository.existsByEmail("admin@safesen.ai")) {
            User admin = new User();
            admin.setEmail("admin@safesen.ai");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFullName("Campus Safety Administrator");
            admin.setPhoneNumber("+91 9444001122");
            admin.setEnabled(true);
            admin.setCreatedAt(LocalDateTime.now());
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            roles.add(adminRole);
            admin.setRoles(roles);
            userRepository.save(admin);
            log.info("Default Administrator created: admin@safesen.ai / admin123");
        }

        // 3. Default Demo User (matching Figure B.1 in Project Report: Preethi S.)
        if (!userRepository.existsByEmail("preethi@example.com")) {
            User user = new User();
            user.setEmail("preethi@example.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setFullName("Preethi S.");
            user.setPhoneNumber("+91 9842104168");
            user.setEnabled(true);
            user.setCreatedAt(LocalDateTime.now());
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);
            User savedUser = userRepository.save(user);
            contactService.seedSampleContactsIfEmpty(savedUser);
            log.info("Demo User created: preethi@example.com / user123 with 5 Favourite Trusted Contacts");
        }

        // 4. Seed Verified Nearby Emergency Infrastructure (Karur / VSB Campus & urban transit hubs)
        if (nearbyPlaceRepository.count() == 0) {
            // Police Stations
            nearbyPlaceRepository.save(new NearbyPlace(
                    "All Women Police Station Karur",
                    "POLICE",
                    10.9574, 78.0818,
                    "04324-255100 / 1091",
                    "Kovai Main Road, Karur, Tamil Nadu 639002"));

            nearbyPlaceRepository.save(new NearbyPlace(
                    "Karur Town Police Station",
                    "POLICE",
                    10.9601, 78.0766,
                    "04324-260100 / 112",
                    "Near Bus Stand, Jawahar Bazaar, Karur 639001"));

            nearbyPlaceRepository.save(new NearbyPlace(
                    "Velayuthampalayam Police Station (VSB Campus Precinct)",
                    "POLICE",
                    11.0542, 78.0055,
                    "04324-270222 / 100",
                    "NH-67, Velayuthampalayam, Karur District"));

            nearbyPlaceRepository.save(new NearbyPlace(
                    "Collectorate Police Outpost & Helpdesk",
                    "POLICE",
                    10.9328, 78.0882,
                    "04324-257500",
                    "District Collectorate Campus, Thanthonimalai, Karur"));

            // Hospitals
            nearbyPlaceRepository.save(new NearbyPlace(
                    "Government Medical College & Hospital (24/7 Casualty)",
                    "HOSPITAL",
                    10.9482, 78.0825,
                    "04324-274200 / 108",
                    "Gandhigramam South, Karur 639004"));

            nearbyPlaceRepository.save(new NearbyPlace(
                    "Apollo Reach Multi-Speciality Hospital",
                    "HOSPITAL",
                    10.9555, 78.0910,
                    "04324-230999 / 1066",
                    "NH-7 Byepass, Karur 639007"));

            nearbyPlaceRepository.save(new NearbyPlace(
                    "Amaravathi Hospital & Trauma Care",
                    "HOSPITAL",
                    10.9612, 78.0789,
                    "04324-240500",
                    "55 Kovai Road, Karur 639002"));

            // Fire and Rescue
            nearbyPlaceRepository.save(new NearbyPlace(
                    "Karur District Fire & Rescue Station",
                    "FIRE_STATION",
                    10.9635, 78.0745,
                    "04324-260101 / 101",
                    "Near Municipal Office, Karur"));

            // 24/7 Emergency Pharmacies
            nearbyPlaceRepository.save(new NearbyPlace(
                    "Apollo 24/7 Pharmacy Care",
                    "PHARMACY",
                    10.9585, 78.0805,
                    "04324-255222",
                    "Opposite Head Post Office, Karur"));

            log.info("Initialized verified emergency places across Karur & campus corridors.");
        }
    }
}

