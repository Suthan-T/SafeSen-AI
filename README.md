# SafeSen-AI: Women Safety Analytics – Protecting Women from Safety Threats

[![Java](https://img.shields.io/badge/Java-21%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Leaflet](https://img.shields.io/badge/Leaflet-1.9.4-199900?style=for-the-badge&logo=leaflet&logoColor=white)](https://leafletjs.com/)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)](https://getbootstrap.com/)
[![License](https://img.shields.io/badge/License-Academic-blue?style=for-the-badge)](#)

> **Mini Project – Department of Computer Science and Engineering**  
> **V.S.B. Engineering College, Karur – 639 111** (Affiliated to Anna University, Chennai)  
> **Supervisor:** Mr. K. Senthil Kumar, M.E. | **HOD:** Dr. P. Anbumani, M.E., Ph.D.

---

## 👥 Authors & Contributors

| Contributor | GitHub Profile | Role | Department & Institution |
| :--- | :--- | :--- | :--- |
| **Suthan T** (922524104168) | [![GitHub](https://img.shields.io/badge/GitHub-Suthan--T-181717?style=flat&logo=github)](https://github.com/Suthan-T) | Lead Developer & System Architect | B.E. Computer Science & Engineering, VSBEC |
| **Tamil Iniyan E** (922524104169) | [![GitHub](https://img.shields.io/badge/GitHub-Tamiliniyan77-181717?style=flat&logo=github)](https://github.com/Tamiliniyan77) | Co-Author & Frontend/GIS Engineer | B.E. Computer Science & Engineering, VSBEC |

---

## 📌 Project Overview

**SafeSen-AI** is an AI-driven, proactive women safety analytics platform engineered to safeguard commuters across urban and semi-urban transit corridors. The system combines multi-corridor spatial risk modeling, live GPS tracking, hands-free voice panic recognition, jarring dual-oscillator acoustic sirens, multi-channel distress broadcasts (real-time Gmail SMTP + SMS), and an institutional administration dashboard.

### 🌟 Key Capabilities
1. **Interactive Safe Route Planning Engine (`/safe-route`)**:
   - Compares 3 alternative transit corridors simultaneously (*Recommended Safe*, *Balanced*, *Unsafe Shortcut*).
   - Multi-criteria AI Safety Scoring ($0–100$) evaluating street lighting, police presence, CCTV coverage, and past incident heatmaps.
   - **Live Journey & Movement Simulation:** Interactive start journey mode with animated commuter tracker, live progress bar, speed controls (1x, 3x, 8x, 20x), remaining distance/ETA countdown, and proximity alerts when passing police booths or hospitals.
2. **Emergency Distress SOS Engine (`/dashboard`)**:
   - One-Touch physical button & hands-free Web Speech API keyword listener (*"Help"*, *"Emergency"*, *"Bachao"*).
   - High-penetration acoustic buzzer synthesizer (3100 Hz Sawtooth + 3185 Hz Square Wave with 24 Hz LFO flutter).
   - Instant dual-channel broadcast to all 5 trusted contacts with live clickable Google Maps tracking coordinates.
3. **Nearby Emergency Directory & Geofencing (`/nearby`)**:
   - Real-time discovery of Police Stations, 24/7 Hospitals, Trauma Centers, and Fire Stations with Haversine distance.
   - Interactive search bar, quick jump presets, and click/drag on map to dynamically center geofence radius (5–25 km).
4. **Configurable 5 Trusted Emergency Contacts (`/contacts`)**:
   - Manage and edit saved contacts (Name, Mobile Phone, Email, Relationship, and Priority #1 to #5).
   - One-click live distress test email dispatch to personal inbox.
5. **Institutional Administrator Command Center (`/admin`)**:
   - Safety analytics and incident trends with Chart.js.
   - User account management (suspend / activate commuters).
   - Emergency POI registry and live outbound email dispatch logs.

---

## 🚀 Quick Start (Running the Application)

### Prerequisites
- **Java Development Kit (JDK 21 LTS)**
- **Apache Maven 3.8+**
- Modern Web Browser (Google Chrome, Edge, or Firefox)

### Run via Batch File (Windows)
Double-click `run.bat` in the project root:
```cmd
run.bat
```

### Or Run via Command Line
```bash
# Build with Maven
mvn clean package -DskipTests

# Run the packaged executable JAR
java -jar target/women-safety-analytics-1.0.0.jar
```

Access the application in your browser at:  
👉 **`http://localhost:8080`**

---

## 🔑 Pre-Configured Test Credentials

| Role | Email Address | Password | Permissions |
| :--- | :--- | :--- | :--- |
| **Commuter User** | `preethi@example.com` | `user123` | Route Planning, Emergency Contacts, SOS Panic, Live Journey |
| **Administrator** | `admin@safesen.ai` | `admin123` | Institutional Analytics, POI Registry, User Audit, Dispatched Emails |

*Note: New commuters can also register freely via `/register`.*

---

## 🛠️ Technology Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend Framework** | Spring Boot 3.2.5 (Spring MVC, Spring Security, Spring Data JPA) |
| **Programming Language** | Java 21 (LTS) |
| **Database** | PostgreSQL-compatible H2 Relational Engine (Zero-Configuration persistence) |
| **Frontend & UI** | Thymeleaf 3, Bootstrap 5.3, Bootstrap Icons, Vanilla CSS3 |
| **GIS & Mapping** | Leaflet.js, OpenStreetMap (OSM) Vector Tiles, OSRM Routing Engine |
| **Data Visualization** | Chart.js 4.4 |
| **Audio & Speech** | Web Audio API (Dissonant Siren Synthesizer), Web Speech API |
| **Telecommunications** | JavaMailSender (Live Gmail SMTP), SMS Gateway Dispatcher |

---

## 📄 License & Academic Attribution
Developed as an academic Mini Project for the Department of Computer Science and Engineering, **V.S.B. Engineering College, Karur**. All rights reserved by Suthan T & Tamil Iniyan E.
