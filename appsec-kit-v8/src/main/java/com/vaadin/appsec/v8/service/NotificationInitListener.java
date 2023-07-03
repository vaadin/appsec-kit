/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.Registration;
import com.vaadin.appsec.v8.ui.AppSecUI;
import com.vaadin.appsec.v8.ui.AppSecUIProvider;
import com.vaadin.server.ServiceInitEvent;
import com.vaadin.server.SessionDestroyEvent;
import com.vaadin.server.SessionInitEvent;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * A Vaadin service listener for initializing the notifications for AppSec Kit.
 * Will be initialized automatically by Vaadin.
 */
public class NotificationInitListener extends AbstractInitListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NotificationInitListener.class);

    /**
     * Notification timeout (in ms). Set to 24 hours to essentially make it
     * persistent until either the appsec link is clicked or the notification is
     * dismissed.
     */
    private static final int NOTIFICATION_DELAY = 24 * 60 * 60 * 1000;

    private final Map<VaadinSession, Registration> scanEventRegistrations = new ConcurrentHashMap<>();

    @Override
    public void serviceInit(ServiceInitEvent event) {
        VaadinService service = event.getSource();
        if (isDebugMode(service)) {
            service.addSessionInitListener(this::subscribeSessionToScanEvents);
            service.addSessionDestroyListener(this::removeSessionRegistration);
            LOGGER.info("NotificationInitListener initialized.");
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
                .addScanEventListener(scanEvent -> {
                    int newVulns = scanEvent.getNewVulnerabilities().size();
                    if (isSessionOpen(session) && newVulns > 0) {
                        session.getUIs().forEach(this::doNotifyUI);
                    }
                });
        scanEventRegistrations.put(session, scanEventRegistration);
    }

    private void doNotifyUI(UI ui) {
        if (ui instanceof AppSecUI) {
            return;
        }
        String link = "<a href=\"?"
                + AppSecUIProvider.VAADIN_APPSEC_KIT_URL_PARAM
                + "\" target=\"_blank\">here</a>";
        String msg = "New vulnerabilities found! Click " + link
                + " to open Vaadin AppSec Kit,"
                + " or click on this message to dismiss it.";
        Notification n = new Notification("Vaadin AppSec Kit", msg,
                Notification.Type.TRAY_NOTIFICATION);
        n.setPosition(Position.TOP_RIGHT);
        n.setDelayMsec(NOTIFICATION_DELAY);
        n.setHtmlContentAllowed(true);
        n.show(ui.getPage());
    }

    private boolean isSessionOpen(VaadinSession session) {
        return !(session.getState() != VaadinSession.State.OPEN
                || session.getSession() == null);
    }
}
