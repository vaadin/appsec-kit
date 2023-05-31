/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.v8.ui.AppSecUI;
import com.vaadin.appsec.v8.ui.AppSecUIProvider;
import com.vaadin.server.ServiceInitEvent;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * A Vaadin service init listener for initializing the notification mechanism.
 * Will be initialized automatically by Vaadin.
 */
public class NotificationInitListener extends AbstractInitListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NotificationInitListener.class);

    private static final String NOTIFIED_UIS_SESSION_PARAM = "vaadin-appsec-kit-notified-uis";
    /**
     * New UI instance check interval (in ms). Determines how often the session
     * is checked for new UI instances which might need a notification shown.
     */
    private static final int NOTIFICATION_CHECK_INTERVAL = 5000;

    /**
     * Notification timeout (in ms). Set to 24 hours to essentially make it
     * persistent until either the appsec link is clicked or the notification is
     * dismissed.
     */
    private static final int NOTIFICATION_DELAY = 24 * 60 * 60 * 1000;

    @Override
    public void serviceInit(ServiceInitEvent event) {
        VaadinService vaadinService = event.getSource();
        if (isDebugMode(vaadinService)) {
            vaadinService.addSessionInitListener(e -> {
                e.getSession().addUIProvider(new AppSecUIProvider());
                createNotificationThread(e.getSession()).start();
            });
            LOGGER.debug("NotificationInitListener initialized.");
        }
    }

    private Thread createNotificationThread(final VaadinSession session) {
        return new Thread(() -> {
            try {
                LOGGER.debug(
                        "NotificationInitListener notification thread initialized.");

                Thread.sleep(NOTIFICATION_CHECK_INTERVAL);

                while (isSessionOpen(session)) {
                    session.access(() -> {
                        session.getUIs()
                                .forEach(ui -> notifyUiIfNeeded(session, ui));
                    });
                    Thread.sleep(NOTIFICATION_CHECK_INTERVAL);
                }
            } catch (InterruptedException e) {
                // NOP
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void notifyUiIfNeeded(VaadinSession session, UI ui) {
        if (ui instanceof AppSecUI) {
            return;
        }

        List<Integer> notifiedUIs;
        Object notifiedUIsFromSession = session
                .getAttribute(NOTIFIED_UIS_SESSION_PARAM);

        try {
            notifiedUIs = new ArrayList<>(
                    (List<Integer>) notifiedUIsFromSession);
        } catch (RuntimeException e) {
            notifiedUIs = new ArrayList<>();
        }

        // Notify UI
        if (!notifiedUIs.contains(ui.getUIId())) {
            doNotifyUI(ui);
            notifiedUIs.add(ui.getUIId());
        }

        session.setAttribute(NOTIFIED_UIS_SESSION_PARAM, notifiedUIs);
    }

    private void doNotifyUI(UI ui) {
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
