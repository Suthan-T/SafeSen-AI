/**
 * SafeSen-AI Multi-Corridor Safe Route Visualizer & Flexible Geocoding
 * Based on Appendix A.4 of the Project Report
 */
let routeMap = null;
let currentRouteLayers = [];
let poiMarkers = [];
let originMarker = null;
let destMarker = null;

let calculatedRoutesData = null;
let currentSelectedRoute = null;

let activeJourney = {
    isRunning: false,
    isPaused: false,
    currentStep: 0,
    totalSteps: 0,
    speedMultiplier: 8, // 8x default for presentation demo
    timerId: null,
    points: [],
    route: null,
    marker: null,
    traveledLine: null
};

let currentStartCoord = { lat: 10.9602, lon: 78.0770, name: "Karur Central Bus Stand" };
let currentDestCoord = { lat: 11.0535, lon: 78.0062, name: "V.S.B. Engineering College Campus" };

let mapPickMode = null; // 'origin' or 'dest' or null

const KNOWN_PLACES = {
    "karur central bus stand": { lat: 10.9602, lon: 78.0770, name: "Karur Central Bus Stand" },
    "karur bus stand": { lat: 10.9602, lon: 78.0770, name: "Karur Central Bus Stand" },
    "v.s.b. engineering college campus": { lat: 11.0535, lon: 78.0062, name: "V.S.B. Engineering College Campus" },
    "vsb engineering college": { lat: 11.0535, lon: 78.0062, name: "V.S.B. Engineering College Campus" },
    "vsb campus": { lat: 11.0535, lon: 78.0062, name: "V.S.B. Engineering College Campus" },
    "district collectorate complex": { lat: 10.9328, lon: 78.0882, name: "District Collectorate Complex" },
    "collectorate": { lat: 10.9328, lon: 78.0882, name: "District Collectorate Complex" },
    "govt medical college hospital": { lat: 10.9482, lon: 78.0825, name: "Govt Medical College Hospital" },
    "hospital": { lat: 10.9482, lon: 78.0825, name: "Govt Medical College Hospital" },
    "amaravathi river ring road": { lat: 10.9710, lon: 78.0650, name: "Amaravathi River Ring Road" },
    "amaravathi bypass": { lat: 10.9710, lon: 78.0650, name: "Amaravathi River Ring Road" },
    "karur it & industrial corridor": { lat: 10.9500, lon: 78.0950, name: "Karur IT & Industrial Corridor" },
    "tech park": { lat: 10.9500, lon: 78.0950, name: "Karur IT & Industrial Corridor" },
    "chennai": { lat: 13.0827, lon: 80.2707, name: "Chennai Central" },
    "coimbatore": { lat: 11.0168, lon: 76.9558, name: "Coimbatore Junction" },
    "trichy": { lat: 10.7905, lon: 78.7047, name: "Tiruchirappalli Junction" },
    "salem": { lat: 11.6643, lon: 78.1460, name: "Salem New Bus Stand" },
    "madurai": { lat: 9.9252, lon: 78.1198, name: "Madurai Junction" }
};

function initSafeRouteMap() {
    const mapContainer = document.getElementById("routeMap");
    if (!mapContainer) return;

    routeMap = L.map('routeMap').setView([10.9800, 78.0500], 12);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors | SafeSen-AI',
        maxZoom: 19
    }).addTo(routeMap);

    // Map Click Listener for picking origin or destination directly on map
    routeMap.on('click', (e) => {
        if (!mapPickMode) return;
        const lat = e.latlng.lat;
        const lon = e.latlng.lng;

        if (mapPickMode === 'origin') {
            currentStartCoord = { lat: lat, lon: lon, name: `Map Location (${lat.toFixed(4)}, ${lon.toFixed(4)})` };
            document.getElementById('startInput').value = currentStartCoord.name;
            setOriginPin(lat, lon, currentStartCoord.name);
            endMapPickMode();
            runRouteEvaluation();
        } else if (mapPickMode === 'dest') {
            currentDestCoord = { lat: lat, lon: lon, name: `Map Location (${lat.toFixed(4)}, ${lon.toFixed(4)})` };
            document.getElementById('destInput').value = currentDestCoord.name;
            setDestPin(lat, lon, currentDestCoord.name);
            endMapPickMode();
            runRouteEvaluation();
        }
    });

    bindControls();
    runRouteEvaluation();
}

function bindControls() {
    // Preset Origin Chips
    document.querySelectorAll('.origin-preset').forEach(chip => {
        chip.addEventListener('click', () => {
            const name = chip.getAttribute('data-name');
            const lat = parseFloat(chip.getAttribute('data-lat'));
            const lon = parseFloat(chip.getAttribute('data-lon'));
            currentStartCoord = { lat, lon, name };
            document.getElementById('startInput').value = name;
            setOriginPin(lat, lon, name);
            runRouteEvaluation();
        });
    });

    // Preset Destination Chips
    document.querySelectorAll('.dest-preset').forEach(chip => {
        chip.addEventListener('click', () => {
            const name = chip.getAttribute('data-name');
            const lat = parseFloat(chip.getAttribute('data-lat'));
            const lon = parseFloat(chip.getAttribute('data-lon'));
            currentDestCoord = { lat, lon, name };
            document.getElementById('destInput').value = name;
            setDestPin(lat, lon, name);
            runRouteEvaluation();
        });
    });

    // Use GPS Location button (Resilient, zero annoying alert timeouts)
    const btnGps = document.getElementById('btnUseGps');
    if (btnGps) {
        btnGps.addEventListener('click', () => {
            btnGps.innerHTML = `<span class="spinner-border spinner-border-sm me-1"></span> Locating...`;

            const applyLocation = (lat, lon, label) => {
                btnGps.innerHTML = `<i class="bi bi-check-circle-fill text-success me-1"></i> Located`;
                currentStartCoord = { lat, lon, name: label };
                document.getElementById('startInput').value = label;
                setOriginPin(lat, lon, label);
                if (routeMap) routeMap.setView([lat, lon], 13);
                runRouteEvaluation();
                setTimeout(() => { btnGps.innerHTML = `<i class="bi bi-crosshair me-1"></i> Use My GPS Location`; }, 3000);
            };

            let resolved = false;

            // Fallback timer if browser takes longer than 3 seconds
            const timer = setTimeout(() => {
                if (!resolved) {
                    resolved = true;
                    // Use active map center or default urban position smoothly without annoying popup
                    const center = routeMap ? routeMap.getCenter() : { lat: 10.9574, lng: 78.0818 };
                    applyLocation(center.lat, center.lng, `Current Map Area (${center.lat.toFixed(4)}, ${center.lng.toFixed(4)})`);
                }
            }, 3000);

            if ("geolocation" in navigator) {
                navigator.geolocation.getCurrentPosition(
                    pos => {
                        if (!resolved) {
                            resolved = true;
                            clearTimeout(timer);
                            applyLocation(pos.coords.latitude, pos.coords.longitude, "My Current Live Location");
                        }
                    },
                    err => {
                        console.info("Geolocation fallback applied:", err.message);
                        if (!resolved) {
                            resolved = true;
                            clearTimeout(timer);
                            const center = routeMap ? routeMap.getCenter() : { lat: 10.9574, lng: 78.0818 };
                            applyLocation(center.lat, center.lng, `Current Area (${center.lat.toFixed(4)}, ${center.lng.toFixed(4)})`);
                        }
                    },
                    { enableHighAccuracy: false, timeout: 2800, maximumAge: 300000 }
                );
            } else {
                clearTimeout(timer);
                const center = routeMap ? routeMap.getCenter() : { lat: 10.9574, lng: 78.0818 };
                applyLocation(center.lat, center.lng, "Current Map Position");
            }
        });
    }

    // Pick on Map buttons
    const btnPickOrigin = document.getElementById('btnPickOriginMap');
    if (btnPickOrigin) {
        btnPickOrigin.addEventListener('click', () => {
            startMapPickMode('origin', 'Click anywhere on the map to set your TRIP ORIGIN');
        });
    }

    const btnPickDest = document.getElementById('btnPickDestMap');
    if (btnPickDest) {
        btnPickDest.addEventListener('click', () => {
            startMapPickMode('dest', 'Click anywhere on the map to set your TRIP DESTINATION');
        });
    }

    // Evaluate button
    const btnEval = document.getElementById('btnEvaluateRoutes');
    if (btnEval) {
        btnEval.addEventListener('click', (e) => {
            e.preventDefault();
            runRouteEvaluation();
        });
    }

    initJourneyControls();
}

function startMapPickMode(mode, message) {
    mapPickMode = mode;
    const notice = document.getElementById('mapPickNotice');
    const noticeText = document.getElementById('mapPickNoticeText');
    if (notice && noticeText) {
        noticeText.textContent = message;
        notice.classList.remove('d-none');
    }
    if (routeMap) routeMap.getContainer().style.cursor = 'crosshair';
}

function endMapPickMode() {
    mapPickMode = null;
    const notice = document.getElementById('mapPickNotice');
    if (notice) notice.classList.add('d-none');
    if (routeMap) routeMap.getContainer().style.cursor = '';
}

function setOriginPin(lat, lon, name) {
    if (!routeMap) return;
    if (originMarker) routeMap.removeLayer(originMarker);

    const originIcon = L.divIcon({
        html: '<div style="background: #10b981; color: white; border: 2px solid white; border-radius: 50%; width: 28px; height: 28px; display:flex; align-items:center; justify-content:center; box-shadow: 0 2px 8px rgba(0,0,0,0.4);"><i class="bi bi-geo-alt-fill"></i></div>',
        className: '',
        iconSize: [28, 28],
        iconAnchor: [14, 28]
    });

    originMarker = L.marker([lat, lon], { icon: originIcon, draggable: true })
        .bindPopup(`<b>Trip Origin:</b><br>${name}`)
        .addTo(routeMap);

    originMarker.on('dragend', (e) => {
        const pt = e.target.getLatLng();
        currentStartCoord = { lat: pt.lat, lon: pt.lng, name: `Custom Origin (${pt.lat.toFixed(4)}, ${pt.lng.toFixed(4)})` };
        document.getElementById('startInput').value = currentStartCoord.name;
        runRouteEvaluation();
    });
}

function setDestPin(lat, lon, name) {
    if (!routeMap) return;
    if (destMarker) routeMap.removeLayer(destMarker);

    const destIcon = L.divIcon({
        html: '<div style="background: #dc2626; color: white; border: 2px solid white; border-radius: 50%; width: 28px; height: 28px; display:flex; align-items:center; justify-content:center; box-shadow: 0 2px 8px rgba(0,0,0,0.4);"><i class="bi bi-pin-map-fill"></i></div>',
        className: '',
        iconSize: [28, 28],
        iconAnchor: [14, 28]
    });

    destMarker = L.marker([lat, lon], { icon: destIcon, draggable: true })
        .bindPopup(`<b>Trip Destination:</b><br>${name}`)
        .addTo(routeMap);

    destMarker.on('dragend', (e) => {
        const pt = e.target.getLatLng();
        currentDestCoord = { lat: pt.lat, lon: pt.lng, name: `Custom Destination (${pt.lat.toFixed(4)}, ${pt.lng.toFixed(4)})` };
        document.getElementById('destInput').value = currentDestCoord.name;
        runRouteEvaluation();
    });
}

async function resolveLocation(inputText, fallbackCoord) {
    if (!inputText || !inputText.trim()) return fallbackCoord;
    const clean = inputText.trim().toLowerCase();

    // Check known places dictionary
    if (KNOWN_PLACES[clean]) {
        return KNOWN_PLACES[clean];
    }
    for (const key in KNOWN_PLACES) {
        if (clean.includes(key) || key.includes(clean)) {
            return KNOWN_PLACES[key];
        }
    }

    // Try online OpenStreetMap Nominatim geocoding
    try {
        const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(inputText)}&limit=1`;
        const res = await fetch(url, { headers: { 'Accept': 'application/json' } });
        if (res.ok) {
            const data = await res.json();
            if (data && data.length > 0) {
                return {
                    lat: parseFloat(data[0].lat),
                    lon: parseFloat(data[0].lon),
                    name: data[0].display_name
                };
            }
        }
    } catch (e) {
        console.info("Nominatim search skipped or offline:", e.message);
    }

    return fallbackCoord;
}

async function runRouteEvaluation() {
    const startInput = document.getElementById("startInput");
    const destInput = document.getElementById("destInput");
    const loader = document.getElementById("routeLoader");
    const loaderText = document.getElementById("routeLoaderText");

    if (loader) {
        loader.classList.remove("d-none");
        if (loaderText) loaderText.textContent = "Resolving locations & running AI Safety Engine...";
    }

    const startText = startInput ? startInput.value : "";
    const destText = destInput ? destInput.value : "";

    // Resolve coordinates
    currentStartCoord = await resolveLocation(startText, currentStartCoord);
    currentDestCoord = await resolveLocation(destText, currentDestCoord);

    setOriginPin(currentStartCoord.lat, currentStartCoord.lon, currentStartCoord.name);
    setDestPin(currentDestCoord.lat, currentDestCoord.lon, currentDestCoord.name);

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

    const payload = {
        startLat: currentStartCoord.lat,
        startLon: currentStartCoord.lon,
        destLat: currentDestCoord.lat,
        destLon: currentDestCoord.lon,
        originName: currentStartCoord.name,
        destinationName: currentDestCoord.name
    };

    fetch('/api/routes/calculate', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(payload)
    })
    .then(res => res.json())
    .then(data => {
        if (loader) loader.classList.add("d-none");
        renderSafeRoutes(data);
    })
    .catch(err => {
        if (loader) loader.classList.add("d-none");
        console.error("Route calculation error:", err);
    });
}

function clearMapLayers() {
    currentRouteLayers.forEach(layer => routeMap.removeLayer(layer));
    currentRouteLayers = [];
    poiMarkers.forEach(marker => routeMap.removeLayer(marker));
    poiMarkers = [];
}

function renderSafeRoutes(routeData) {
    if (!routeMap) return;
    clearMapLayers();

    const allRoutes = routeData.allRoutes || [];
    const recommended = routeData.recommendedRoute;
    const cardsContainer = document.getElementById("routeCardsContainer");
    if (cardsContainer) cardsContainer.innerHTML = "";

    const bounds = L.latLngBounds([]);
    if (originMarker) bounds.extend(originMarker.getLatLng());
    if (destMarker) bounds.extend(destMarker.getLatLng());

    // 1. Render all candidate corridors
    allRoutes.forEach((route, idx) => {
        const isBest = (recommended && route.id === recommended.id);
        const geojsonObj = JSON.parse(route.geometry);

        let color = '#3b82f6'; // default blue
        if (isBest) {
            color = '#10b981'; // Emerald Green for Safe Recommended Route
        } else if (route.safetyScore < 50) {
            color = '#ef4444'; // Red for High Risk Shortcut
        }

        const polyline = L.geoJSON(geojsonObj, {
            style: {
                color: color,
                weight: isBest ? 6 : 4,
                opacity: isBest ? 0.95 : 0.65,
                dashArray: isBest ? null : '6, 8'
            }
        }).addTo(routeMap);

        polyline.bindPopup(`
            <div style="font-family: inherit;">
                <span class="badge ${route.safetyScore >= 80 ? 'bg-success' : (route.safetyScore >= 50 ? 'bg-warning text-dark' : 'bg-danger')}">${route.riskLevel}</span>
                <h6 class="mt-2 mb-1 fw-bold">${route.title}</h6>
                <div><strong>Safety Score:</strong> ${route.safetyScore}/100</div>
                <div><strong>Distance:</strong> ${route.distanceKm} km | <strong>Duration:</strong> ${route.durationMin} mins</div>
                <div class="text-muted small mt-1">${route.lightingCondition}</div>
            </div>
        `);

        currentRouteLayers.push(polyline);
        polyline.eachLayer(l => bounds.extend(l.getBounds()));

        // Add Route Selection Cards in the UI
        if (cardsContainer) {
            const card = document.createElement("div");
            card.className = `route-card ${isBest ? 'selected-recommended' : (route.safetyScore < 50 ? 'selected-shortcut' : 'selected-balanced')}`;
            card.innerHTML = `
                <div class="d-flex justify-content-between align-items-center mb-1">
                    <span class="badge ${route.safetyScore >= 80 ? 'badge-safe' : (route.safetyScore >= 50 ? 'badge-moderate' : 'badge-danger')}">
                        ${isBest ? '★ RECOMMENDED SAFE' : (route.safetyScore < 50 ? '⚠️ UNSAFE SHORTCUT' : 'BALANCED')}
                    </span>
                    <span class="fw-bold fs-5 ${route.safetyScore >= 80 ? 'text-success' : (route.safetyScore >= 50 ? 'text-warning' : 'text-danger')}">
                        ${route.safetyScore} <small class="fs-6 text-muted">/100</small>
                    </span>
                </div>
                <h6 class="fw-bold mb-1">${route.title}</h6>
                <div class="text-muted small mb-2">
                    <i class="bi bi-clock"></i> ${route.durationMin} mins &nbsp;|&nbsp; 
                    <i class="bi bi-geo-alt"></i> ${route.distanceKm} km &nbsp;|&nbsp; 
                    <i class="bi bi-shield-check"></i> ${route.confidencePercent}% Confidence
                </div>
                <div class="small">
                    <span class="badge bg-light text-dark border me-1"><i class="bi bi-shield-fill text-primary"></i> ${route.policeCount} Police Booths</span>
                    <span class="badge bg-light text-dark border"><i class="bi bi-hospital text-danger"></i> ${route.hospitalCount} Hospitals</span>
                </div>
                <div class="text-secondary small mt-1"><i class="bi bi-lightbulb"></i> ${route.lightingCondition}</div>
            `;
            card.addEventListener("click", () => {
                currentSelectedRoute = route;
                document.querySelectorAll('.route-card').forEach(c => {
                    c.style.borderColor = '';
                    c.style.borderWidth = '';
                });
                card.style.borderColor = '#10b981';
                card.style.borderWidth = '3px';
                routeMap.fitBounds(polyline.getBounds(), { padding: [30, 30] });
                polyline.openPopup();
                updateStartJourneyButton(route);
            });
            cardsContainer.appendChild(card);
        }
    });

    // Update Start Journey button with primary selected route
    calculatedRoutesData = routeData;
    currentSelectedRoute = recommended || (allRoutes.length > 0 ? allRoutes[0] : null);
    updateStartJourneyButton(currentSelectedRoute);

    // 2. Render Verified Emergency POIs along recommended corridor
    const corridorContainer = document.getElementById("corridorPoisContainer");
    const corridorCountBadge = document.getElementById("corridorPoiCount");

    if (recommended && recommended.emergencyPois && recommended.emergencyPois.length > 0) {
        if (corridorCountBadge) {
            corridorCountBadge.textContent = `${recommended.emergencyPois.length} Found`;
            corridorCountBadge.className = "badge bg-success";
        }
        if (corridorContainer) {
            corridorContainer.innerHTML = "";
        }

        recommended.emergencyPois.forEach((poi, pIdx) => {
            const isPolice = (poi.category === 'POLICE');
            const isHospital = (poi.category === 'HOSPITAL');
            const iconHtml = isPolice
                ? `<div style="background: #1e3a8a; color: white; border-radius: 50%; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; border: 2px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);"><i class="bi bi-shield-fill"></i></div>`
                : `<div style="background: #dc2626; color: white; border-radius: 50%; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; border: 2px solid white; box-shadow: 0 2px 5px rgba(0,0,0,0.3);"><i class="bi bi-hospital-fill"></i></div>`;

            const customIcon = L.divIcon({
                html: iconHtml,
                className: '',
                iconSize: [30, 30],
                iconAnchor: [15, 15]
            });

            const marker = L.marker([poi.latitude, poi.longitude], { icon: customIcon })
                .bindPopup(`
                    <div>
                        <span class="badge ${isPolice ? 'bg-primary' : 'bg-danger'} mb-1">${poi.category}</span>
                        <h6 class="mb-1 fw-bold">${poi.placeName}</h6>
                        <div class="small text-muted mb-1">${poi.address}</div>
                        <a href="tel:${poi.contactNumber}" class="btn btn-sm btn-outline-dark mt-1"><i class="bi bi-telephone"></i> ${poi.contactNumber}</a>
                    </div>
                `)
                .addTo(routeMap);

            poiMarkers.push(marker);
            bounds.extend([poi.latitude, poi.longitude]);

            // Append to corridor POIs sidebar list
            if (corridorContainer) {
                const item = document.createElement("div");
                item.className = "list-group-item list-group-item-action p-2 px-3 border-bottom";
                item.style.cursor = "pointer";
                item.innerHTML = `
                    <div class="d-flex justify-content-between align-items-center mb-1">
                        <strong class="text-dark small"><i class="bi ${isPolice ? 'bi-shield-fill text-primary' : 'bi-hospital-fill text-danger'} me-1"></i> ${poi.placeName}</strong>
                        <span class="badge ${isPolice ? 'bg-primary' : 'bg-danger'} font-monospace" style="font-size: 0.65rem;">${poi.category}</span>
                    </div>
                    <div class="text-muted small text-truncate" style="font-size: 0.75rem;"><i class="bi bi-geo-alt"></i> ${poi.address}</div>
                    <div class="d-flex justify-content-between align-items-center mt-1">
                        <span class="text-success small fw-semibold" style="font-size: 0.72rem;"><i class="bi bi-shield-check"></i> Active Along Corridor</span>
                        <a href="tel:${poi.contactNumber}" class="btn btn-sm btn-outline-danger py-0 px-2 fw-bold text-nowrap" style="font-size: 0.75rem;" onclick="event.stopPropagation();">
                            <i class="bi bi-telephone-fill"></i> ${poi.contactNumber}
                        </a>
                    </div>
                `;
                item.addEventListener("click", () => {
                    routeMap.setView([poi.latitude, poi.longitude], 15);
                    marker.openPopup();
                });
                corridorContainer.appendChild(item);
            }
        });
    } else {
        if (corridorCountBadge) {
            corridorCountBadge.textContent = "0 Found";
            corridorCountBadge.className = "badge bg-secondary";
        }
        if (corridorContainer) {
            corridorContainer.innerHTML = `<div class="p-3 text-muted small text-center"><i class="bi bi-info-circle me-1"></i> No emergency services within corridor buffer.</div>`;
        }
    }

    if (bounds.isValid()) {
        routeMap.fitBounds(bounds, { padding: [40, 40] });
    }
}

/**
 * Live Journey Navigation & Commuter Movement Engine
 */
function updateStartJourneyButton(route) {
    const box = document.getElementById("startJourneyBtnContainer");
    const btn = document.getElementById("btnStartJourney");
    if (!box || !btn || !route) return;
    box.classList.remove("d-none");
    const shortTitle = route.title.length > 28 ? route.title.substring(0, 28) + "..." : route.title;
    btn.innerHTML = `<i class="bi bi-play-circle-fill me-1"></i> Start Journey: ${shortTitle}`;
}

function initJourneyControls() {
    const btnStart = document.getElementById("btnStartJourney");
    if (btnStart) {
        btnStart.addEventListener("click", () => {
            if (currentSelectedRoute) {
                startLiveJourney(currentSelectedRoute);
            }
        });
    }

    const btnPause = document.getElementById("btnPauseJourney");
    if (btnPause) {
        btnPause.addEventListener("click", () => {
            if (!activeJourney.isRunning) return;
            if (activeJourney.isPaused) {
                resumeLiveJourney();
                btnPause.innerHTML = `<i class="bi bi-pause-fill"></i> Pause`;
                btnPause.className = "btn btn-warning btn-sm fw-bold";
            } else {
                pauseLiveJourney();
                btnPause.innerHTML = `<i class="bi bi-play-fill"></i> Resume`;
                btnPause.className = "btn btn-success btn-sm fw-bold";
            }
        });
    }

    const btnEnd = document.getElementById("btnEndJourney");
    if (btnEnd) {
        btnEnd.addEventListener("click", () => {
            endLiveJourney(false);
        });
    }

    // Speed multiplier buttons
    document.querySelectorAll(".hud-speed-btn").forEach(btn => {
        btn.addEventListener("click", () => {
            document.querySelectorAll(".hud-speed-btn").forEach(b => {
                b.classList.remove("active", "btn-success", "fw-bold");
                b.classList.add("btn-outline-light");
            });
            btn.classList.add("active", "btn-success", "fw-bold");
            btn.classList.remove("btn-outline-light");

            const speed = parseInt(btn.getAttribute("data-speed")) || 8;
            activeJourney.speedMultiplier = speed;

            // Adjust loop speed dynamically
            if (activeJourney.isRunning && !activeJourney.isPaused) {
                clearInterval(activeJourney.timerId);
                runJourneyTickLoop();
            }
        });
    });
}

function startLiveJourney(route) {
    if (!route || !route.geometry) return;

    endLiveJourney(false);

    let geojson;
    try {
        geojson = JSON.parse(route.geometry);
    } catch (e) {
        console.error("Invalid route geometry:", e);
        return;
    }

    if (!geojson.coordinates || geojson.coordinates.length < 2) return;

    // Convert GeoJSON [lon, lat] to Leaflet [lat, lon]
    const rawCoords = geojson.coordinates.map(pt => [pt[1], pt[0]]);
    const smoothPoints = interpolatePoints(rawCoords, 280);

    activeJourney.isRunning = true;
    activeJourney.isPaused = false;
    activeJourney.currentStep = 0;
    activeJourney.totalSteps = smoothPoints.length;
    activeJourney.points = smoothPoints;
    activeJourney.route = route;

    // Show HUD and hide tip
    const hud = document.getElementById("journeyHud");
    const tip = document.getElementById("mapTipBox");
    if (hud) hud.classList.remove("d-none");
    if (tip) tip.classList.add("d-none");

    // Populate HUD initial labels
    const titleEl = document.getElementById("hudRouteTitle");
    const safetyEl = document.getElementById("hudSafetyScore");
    const distEl = document.getElementById("hudDistanceRem");
    const timeEl = document.getElementById("hudTimeRem");
    const speedEl = document.getElementById("hudCurrentSpeed");
    const barEl = document.getElementById("hudProgressBar");

    if (titleEl) titleEl.textContent = route.title;
    if (safetyEl) safetyEl.textContent = `${route.safetyScore} / 100`;
    if (distEl) distEl.textContent = `${route.distanceKm} km`;
    if (timeEl) timeEl.textContent = `${route.durationMin} mins`;
    if (speedEl) speedEl.textContent = "35 km/h";
    if (barEl) barEl.style.width = "0%";

    // Add Live Commuter Animated Marker to Map
    const startPoint = smoothPoints[0];
    const commuterIcon = L.divIcon({
        html: `
            <div class="commuter-live-marker">
                <div class="commuter-radar-pulse"></div>
                <div class="commuter-dot"><i class="bi bi-person-fill"></i></div>
            </div>
        `,
        className: '',
        iconSize: [38, 38],
        iconAnchor: [19, 19]
    });

    activeJourney.marker = L.marker(startPoint, { icon: commuterIcon, zIndexOffset: 2000 }).addTo(routeMap);

    // Traveled path line (bright emerald trail)
    activeJourney.traveledLine = L.polyline([startPoint], {
        color: '#10b981',
        weight: 6,
        opacity: 0.95
    }).addTo(routeMap);

    routeMap.setView(startPoint, 15);

    // Audio announcement
    speakAlert("Safe journey started along " + route.title + ". Live safety monitoring is active.");

    runJourneyTickLoop();
}

function runJourneyTickLoop() {
    // Tick interval: base 80ms scaled by speed multiplier
    const intervalMs = Math.max(12, Math.floor(90 / activeJourney.speedMultiplier));

    activeJourney.timerId = setInterval(() => {
        if (!activeJourney.isRunning || activeJourney.isPaused) return;

        activeJourney.currentStep++;
        if (activeJourney.currentStep >= activeJourney.totalSteps) {
            // Reached destination!
            activeJourney.currentStep = activeJourney.totalSteps - 1;
            const finalPoint = activeJourney.points[activeJourney.currentStep];
            activeJourney.marker.setLatLng(finalPoint);
            activeJourney.traveledLine.addLatLng(finalPoint);
            endLiveJourney(true);
            return;
        }

        const currentPt = activeJourney.points[activeJourney.currentStep];
        activeJourney.marker.setLatLng(currentPt);
        activeJourney.traveledLine.addLatLng(currentPt);

        // Smooth map centering
        if (activeJourney.currentStep % 10 === 0) {
            routeMap.panTo(currentPt, { animate: true, duration: 0.2 });
        }

        // Telemetry calculation
        const progress = activeJourney.currentStep / activeJourney.totalSteps;
        const distRemaining = ((1 - progress) * activeJourney.route.distanceKm).toFixed(1);
        const timeRemaining = Math.max(0, Math.ceil((1 - progress) * activeJourney.route.durationMin));

        const distEl = document.getElementById("hudDistanceRem");
        const timeEl = document.getElementById("hudTimeRem");
        const barEl = document.getElementById("hudProgressBar");
        const speedEl = document.getElementById("hudCurrentSpeed");

        if (distEl) distEl.textContent = `${distRemaining} km`;
        if (timeEl) timeEl.textContent = `${timeRemaining} mins`;
        if (barEl) barEl.style.width = `${Math.min(100, Math.round(progress * 100))}%`;
        if (speedEl) speedEl.textContent = `${Math.round(35 * (activeJourney.speedMultiplier > 5 ? 1.4 : 1))} km/h`;

        // Emergency POIs Proximity Check along corridor
        if (activeJourney.route.emergencyPois && activeJourney.route.emergencyPois.length > 0) {
            let closestPoi = null;
            let minDist = 999999;
            for (const poi of activeJourney.route.emergencyPois) {
                const d = haversineDistance(currentPt[0], currentPt[1], poi.latitude, poi.longitude);
                if (d < minDist) {
                    minDist = d;
                    closestPoi = poi;
                }
            }

            const alertText = document.getElementById("hudCheckpointText");
            const alertBadge = document.getElementById("hudNearestService");

            if (closestPoi && minDist < 0.45) { // within 450m
                const meters = Math.round(minDist * 1000);
                if (alertText) alertText.innerHTML = `<strong>Safe Checkpoint:</strong> Passing near ${closestPoi.placeName} (${meters}m)`;
                if (alertBadge) alertBadge.innerHTML = `<i class="bi bi-shield-check"></i> ${closestPoi.category}: ${meters}m`;
            } else {
                if (alertText) alertText.textContent = "Corridor clear. Continuous CCTV & Police coverage active.";
                if (alertBadge) alertBadge.innerHTML = `<i class="bi bi-shield-fill"></i> Safe Corridor`;
            }
        }

        // Periodic location heartbeat to backend
        if (activeJourney.currentStep % 25 === 0) {
            sendLiveHeartbeat(currentPt[0], currentPt[1]);
        }
    }, intervalMs);
}

function pauseLiveJourney() {
    activeJourney.isPaused = true;
}

function resumeLiveJourney() {
    activeJourney.isPaused = false;
}

function endLiveJourney(completed = false) {
    if (activeJourney.timerId) {
        clearInterval(activeJourney.timerId);
        activeJourney.timerId = null;
    }
    activeJourney.isRunning = false;
    activeJourney.isPaused = false;

    const hud = document.getElementById("journeyHud");
    const tip = document.getElementById("mapTipBox");

    if (completed) {
        speakAlert("Destination reached. You have safely arrived at your destination.");

        // Show completion modal
        const modalEl = document.getElementById("journeyCompleteModal");
        const titleEl = document.getElementById("completeRouteName");
        const distEl = document.getElementById("completeDistance");
        const scoreEl = document.getElementById("completeSafetyScore");

        if (titleEl && activeJourney.route) titleEl.textContent = activeJourney.route.title;
        if (distEl && activeJourney.route) distEl.textContent = `${activeJourney.route.distanceKm} km`;
        if (scoreEl && activeJourney.route) scoreEl.textContent = `${activeJourney.route.safetyScore}/100 Safe`;

        if (modalEl && window.bootstrap) {
            const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
            modal.show();
        }
    }

    if (hud) hud.classList.add("d-none");
    if (tip) tip.classList.remove("d-none");

    if (activeJourney.marker && routeMap) {
        routeMap.removeLayer(activeJourney.marker);
        activeJourney.marker = null;
    }
    if (activeJourney.traveledLine && routeMap) {
        routeMap.removeLayer(activeJourney.traveledLine);
        activeJourney.traveledLine = null;
    }
}

function sendLiveHeartbeat(lat, lon) {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;

    fetch('/api/location/update', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({ latitude: lat, longitude: lon })
    }).catch(() => {});
}

function interpolatePoints(coords, targetCount = 280) {
    if (!coords || coords.length === 0) return [];
    if (coords.length >= targetCount) return coords;

    const result = [];
    const segments = coords.length - 1;
    const stepsPerSegment = Math.max(1, Math.floor(targetCount / segments));

    for (let i = 0; i < segments; i++) {
        const start = coords[i];
        const end = coords[i + 1];
        for (let j = 0; j < stepsPerSegment; j++) {
            const t = j / stepsPerSegment;
            const lat = start[0] + (end[0] - start[0]) * t;
            const lng = start[1] + (end[1] - start[1]) * t;
            result.push([lat, lng]);
        }
    }
    result.push(coords[coords.length - 1]);
    return result;
}

function haversineDistance(lat1, lon1, lat2, lon2) {
    const R = 6371; // km
    const dLat = (lat2 - lat1) * Math.PI / 180;
    const dLon = (lon2 - lon1) * Math.PI / 180;
    const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
              Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
              Math.sin(dLon / 2) * Math.sin(dLon / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
}

function speakAlert(text) {
    try {
        if ('speechSynthesis' in window) {
            window.speechSynthesis.cancel();
            const utterance = new SpeechSynthesisUtterance(text);
            utterance.rate = 1.0;
            utterance.pitch = 1.0;
            utterance.lang = 'en-US';
            window.speechSynthesis.speak(utterance);
        }
    } catch (e) {
        console.info("Speech synthesis info:", e);
    }
}

document.addEventListener("DOMContentLoaded", () => {
    initSafeRouteMap();
});
