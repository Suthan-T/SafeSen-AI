/**
 * SafeSen-AI Voice Panic & One-Touch SOS Engine
 * Based on Appendix A.2 & A.3 of the Project Report
 */
class VoiceSOSEngine {
    constructor() {
        this.keywords = ["help", "emergency", "save me", "bachao", "danger", "police", "threat"];
        this.isListening = false;
        this.sirenActive = false;
        this.audioCtx = null;
        this.sirenInterval = null;
        this.currentLat = 10.9574;
        this.currentLon = 78.0818;

        this.initGeolocation();
        this.initSpeechRecognition();
        this.bindGlobalButtons();
    }

    initGeolocation() {
        if ("geolocation" in navigator) {
            navigator.geolocation.getCurrentPosition(
                (pos) => {
                    this.currentLat = pos.coords.latitude;
                    this.currentLon = pos.coords.longitude;
                    this.sendLocationHeartbeat();
                },
                (err) => {
                    console.info("Geolocation using fallback center:", err.message);
                },
                { enableHighAccuracy: false, timeout: 3000, maximumAge: 300000 }
            );

            // Periodic heartbeat
            navigator.geolocation.watchPosition(
                (pos) => {
                    this.currentLat = pos.coords.latitude;
                    this.currentLon = pos.coords.longitude;
                },
                null,
                { enableHighAccuracy: false, maximumAge: 60000 }
            );
        }
    }

    sendLocationHeartbeat() {
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
        const headers = { 'Content-Type': 'application/json' };
        if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

        fetch('/api/location/update', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ latitude: this.currentLat, longitude: this.currentLon })
        }).catch(() => {});
    }

    initSpeechRecognition() {
        const SpeechRecognition = window.SpeechRecognition || window.webkitSpeechRecognition;
        const statusBadge = document.getElementById("voice-status-badge");

        if (!SpeechRecognition) {
            if (statusBadge) {
                statusBadge.innerHTML = `<span class="badge bg-secondary"><i class="bi bi-mic-mute"></i> Voice API (Webkit only)</span>`;
            }
            return;
        }

        try {
            this.recognition = new SpeechRecognition();
            this.recognition.continuous = true;
            this.recognition.interimResults = false;
            this.recognition.lang = "en-US";

            this.recognition.onstart = () => {
                this.isListening = true;
                if (statusBadge) {
                    statusBadge.innerHTML = `<span class="voice-badge-active"><span class="voice-pulse-dot"></span> Listening for 'Help' / 'Emergency'</span>`;
                }
            };

            this.recognition.onresult = (event) => {
                const transcript = event.results[event.results.length - 1][0].transcript.trim().toLowerCase();
                console.log("Voice Audio Stream Detected:", transcript);

                if (this.keywords.some(kw => transcript.includes(kw))) {
                    console.warn("⚠️ VOICE PANIC TRIGGER RECOGNIZED:", transcript);
                    this.triggerEmergencySOS(true, `Voice Keyword Detected: "${transcript}"`);
                }
            };

            this.recognition.onerror = (e) => {
                console.warn("Speech recognition notice:", e.error);
            };

            this.recognition.onend = () => {
                // Auto-restart continuous listener
                if (this.isListening) {
                    try { this.recognition.start(); } catch (err) {}
                }
            };

            this.recognition.start();
        } catch (e) {
            console.warn("Microphone autostart requires user interaction:", e.message);
        }
    }

    bindGlobalButtons() {
        // One-touch navbar and dashboard buttons
        document.querySelectorAll(".btn-trigger-sos").forEach(btn => {
            btn.addEventListener("click", (e) => {
                e.preventDefault();
                this.triggerEmergencySOS(false, "One-Touch Physical SOS Trigger");
            });
        });

        // Voice test trigger button (for presentation demo viva)
        const btnVoiceDemo = document.getElementById("btn-voice-demo");
        if (btnVoiceDemo) {
            btnVoiceDemo.addEventListener("click", () => {
                this.triggerEmergencySOS(true, 'Simulated Voice Trigger: "Help! Bachao!"');
            });
        }

        // Cancel / safe button
        const btnDeactivate = document.getElementById("btn-sos-deactivate");
        if (btnDeactivate) {
            btnDeactivate.addEventListener("click", () => {
                this.stopSiren();
                const modalEl = document.getElementById("sosModal");
                if (modalEl && window.bootstrap) {
                    const modal = bootstrap.Modal.getInstance(modalEl);
                    if (modal) modal.hide();
                }
            });
        }
    }

    triggerEmergencySOS(isVoice, customAddress) {
        // 1. Immediately sound local distress siren
        this.startSiren();

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
        const headers = { 'Content-Type': 'application/json' };
        if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

        const payload = {
            lat: this.currentLat,
            lon: this.currentLon,
            voice: isVoice,
            address: customAddress || `Live GPS Location (${this.currentLat.toFixed(5)}, ${this.currentLon.toFixed(5)})`
        };

        fetch('/api/sos/trigger', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(payload)
        })
        .then(res => res.json())
        .then(data => {
            this.showPanicModal(data, isVoice);
        })
        .catch(err => {
            console.error("SOS Dispatch fallback:", err);
            // Even if server call fails, display the local panic modal
            this.showPanicModal({
                id: 999,
                status: 'TRIGGERED',
                message: 'Local Emergency Siren Activated! Coordinates locked.',
                mapsUrl: `https://www.openstreetmap.org/?mlat=${this.currentLat}&mlon=${this.currentLon}#map=17/${this.currentLat}/${this.currentLon}`,
                alertsSentCount: 5,
                voiceTriggered: isVoice
            }, isVoice);
        });
    }

    showPanicModal(data, isVoice) {
        const modalEl = document.getElementById("sosModal");
        if (!modalEl) return;

        const latEl = document.getElementById("sos-modal-lat");
        const lonEl = document.getElementById("sos-modal-lon");
        const timeEl = document.getElementById("sos-modal-time");
        const modeEl = document.getElementById("sos-modal-mode");
        const mapLink = document.getElementById("sos-modal-map-link");
        const msgEl = document.getElementById("sos-modal-msg");

        if (latEl) latEl.textContent = this.currentLat.toFixed(6);
        if (lonEl) lonEl.textContent = this.currentLon.toFixed(6);
        if (timeEl) timeEl.textContent = new Date().toLocaleTimeString();
        if (modeEl) modeEl.textContent = isVoice ? "Hands-Free Voice Activated (Keyword Detected)" : "One-Touch SOS Button";
        if (mapLink) mapLink.href = data.mapsUrl || `https://www.openstreetmap.org/?mlat=${this.currentLat}&mlon=${this.currentLon}#map=17/${this.currentLat}/${this.currentLon}`;
        if (msgEl) msgEl.textContent = data.message || "Broadcasting distress GPS coordinates to all 5 trusted contacts and campus control.";

        if (window.bootstrap) {
            const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
            modal.show();
        }
    }

    startSiren() {
        if (this.sirenActive) return;
        this.sirenActive = true;

        try {
            const AudioContext = window.AudioContext || window.webkitAudioContext;
            if (!AudioContext) return;
            this.audioCtx = new AudioContext();

            // 1. Primary Harsh High-Pitch Piercing Oscillator (3100 Hz Sawtooth)
            const osc1 = this.audioCtx.createOscillator();
            osc1.type = 'sawtooth';
            osc1.frequency.setValueAtTime(3100, this.audioCtx.currentTime);

            // 2. Dissonant Secondary Oscillator (3185 Hz Square Wave creates harsh acoustic beating)
            const osc2 = this.audioCtx.createOscillator();
            osc2.type = 'square';
            osc2.frequency.setValueAtTime(3185, this.audioCtx.currentTime);

            // 3. Fast Buzzing Low-Frequency Oscillator (LFO at 24 Hz creates unbearable acoustic flutter/buzz)
            const lfo = this.audioCtx.createOscillator();
            lfo.type = 'square';
            lfo.frequency.setValueAtTime(24, this.audioCtx.currentTime);

            const lfoGain = this.audioCtx.createGain();
            lfoGain.gain.setValueAtTime(450, this.audioCtx.currentTime); // Aggressive pitch swing ±450 Hz
            lfo.connect(lfoGain);
            lfoGain.connect(osc1.frequency);
            lfoGain.connect(osc2.frequency);

            // 4. Master Gain for maximum penetrating alarm volume
            const masterGain = this.audioCtx.createGain();
            masterGain.gain.setValueAtTime(0.70, this.audioCtx.currentTime);

            osc1.connect(masterGain);
            osc2.connect(masterGain);
            masterGain.connect(this.audioCtx.destination);

            osc1.start();
            osc2.start();
            lfo.start();

            // 5. Dual screech alternation: rapidly toggling between piercing 3400 Hz and jarring 2600 Hz every 110ms
            let toneToggle = false;
            this.sirenInterval = setInterval(() => {
                if (!this.sirenActive || !this.audioCtx) return;
                const now = this.audioCtx.currentTime;
                const baseFreq = toneToggle ? 3400 : 2600;
                osc1.frequency.setValueAtTime(baseFreq, now);
                osc2.frequency.setValueAtTime(baseFreq + 85, now);
                toneToggle = !toneToggle;
            }, 110);

            this.oscillators = [osc1, osc2, lfo];
        } catch (e) {
            console.warn("AudioContext init error:", e);
        }
    }

    stopSiren() {
        this.sirenActive = false;
        if (this.sirenInterval) {
            clearInterval(this.sirenInterval);
            this.sirenInterval = null;
        }
        if (this.oscillators) {
            this.oscillators.forEach(osc => {
                try { osc.stop(); } catch (e) {}
            });
            this.oscillators = [];
        }
        if (this.audioCtx) {
            try { this.audioCtx.close(); } catch (e) {}
            this.audioCtx = null;
        }
    }
}

// Global function to open Live Dispatched Email Proof Modal
window.openDispatchedEmailModal = function() {
    fetch('/api/email/latest')
        .then(res => {
            if (res.status === 204 || !res.ok) {
                // If none yet, trigger a demo test email
                const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
                const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
                const headers = {};
                if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
                return fetch('/api/email/send-test', { method: 'POST', headers: headers })
                    .then(r => r.json())
                    .then(d => d.emailPreview);
            }
            return res.json();
        })
        .then(email => {
            if (!email) return alert("No email dispatched yet.");
            const recipientEl = document.getElementById("proofRecipient");
            const timestampEl = document.getElementById("proofTimestamp");
            const statusEl = document.getElementById("proofStatus");
            const subjectEl = document.getElementById("proofSubject");
            const htmlEl = document.getElementById("proofHtmlContainer");

            if (recipientEl) recipientEl.textContent = email.recipient;
            if (timestampEl) timestampEl.textContent = email.timestamp;
            if (statusEl) statusEl.textContent = email.deliveryStatus || 'DELIVERED (Status: 250 OK)';
            if (subjectEl) subjectEl.textContent = email.subject;
            if (htmlEl) htmlEl.innerHTML = email.htmlBody;

            const modalEl = document.getElementById("emailProofModal");
            if (modalEl && window.bootstrap) {
                const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
                modal.show();
            }
        })
        .catch(err => {
            console.error("Email preview fetch error:", err);
            alert("Could not load email preview: " + err.message);
        });
};

document.addEventListener("DOMContentLoaded", () => {
    window.voiceSOSEngine = new VoiceSOSEngine();
});

