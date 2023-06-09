/*
 * -
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v7.service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecScanEvent;
import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.Registration;
import com.vaadin.appsec.backend.model.dto.SeverityLevel;
import com.vaadin.appsec.backend.model.dto.Vulnerability;
import com.vaadin.appsec.v7.ui.AppSecUI;
import com.vaadin.appsec.v7.ui.AppSecUIProvider;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * A class for initializing and running the notification mechanism.
 */
public class NotificationInitializer {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NotificationInitializer.class);

    private final Map<VaadinSession, Registration> scanEventRegistrations = new ConcurrentHashMap<>();

    void serviceInit(VaadinService service) {
        if (isDebugMode(service)) {
            service.addSessionInitListener(this::subscribeSessionToScanEvents);
            service.addSessionDestroyListener(this::removeSessionRegistration);
            LOGGER.info("Subscribed to AppSec Kit scan events");
        }
    }

    private void removeSessionRegistration(SessionDestroyEvent e) {
        VaadinSession session = e.getSession();
        Registration registration = scanEventRegistrations.get(session);
        if (registration != null) {
            registration.remove();
        }
    }

    private void subscribeSessionToScanEvents(SessionInitEvent e) {
        VaadinSession session = e.getSession();
        session.addUIProvider(new AppSecUIProvider());
        AppSecService appSecService = AppSecService.getInstance();
        Registration scanEventRegistration = appSecService
                .addScanEventListener(event -> {
                    int newVulns = event.getNewVulnerabilities().size();
                    if (isSessionOpen(session) && newVulns > 0) {
                        Collection<UI> uis = session.getUIs();
                        LOGGER.debug("Notifying {} UIs for session {}",
                                uis.size(), session.getSession().getId());
                        session.access(
                                () -> uis.forEach(ui -> doNotifyUI(ui, event)));
                    }
                });
        scanEventRegistrations.put(session, scanEventRegistration);
    }

    private void doNotifyUI(UI ui, AppSecScanEvent event) {
        if (ui instanceof AppSecUI) {
            return;
        }
        List<Vulnerability> vulns = event.getNewVulnerabilities();
        Map<SeverityLevel, Long> countBySeverity = vulns.stream()
                .collect(Collectors.groupingBy(Vulnerability::getSeverityLevel,
                        Collectors.counting()));
        String link = "<a class=\"appsec-notification-button\" href=\"?"
                + AppSecUIProvider.VAADIN_APPSEC_KIT_URL_PARAM
                + "\" target=\"_blank\">Open AppSec Kit</a>";
        String msg = "<div>%d vulnerabilities found (%d critical, %d moderate). "
                + "Open AppSec Kit for details.</div>" + link;
        Notification n = new Notification("AppSec Kit",
                String.format(msg, vulns.size(),
                        countBySeverity.getOrDefault(SeverityLevel.HIGH, 0l),
                        countBySeverity.getOrDefault(SeverityLevel.MEDIUM, 0l)),
                Notification.Type.TRAY_NOTIFICATION);
        n.setPosition(Position.TOP_RIGHT);
        int delay = (int) AppSecService.getInstance().getConfiguration()
                .getAutoScanInterval().toMillis();
        n.setDelayMsec(delay);
        n.setHtmlContentAllowed(true);
        n.show(ui.getPage());
    }

    private boolean isSessionOpen(VaadinSession session) {
        return !(session.getState() != VaadinSession.State.OPEN
                || session.getSession() == null);
    }

    private boolean isDebugMode(VaadinService vaadinService) {
        return !vaadinService.getDeploymentConfiguration().isProductionMode();
    }
}
