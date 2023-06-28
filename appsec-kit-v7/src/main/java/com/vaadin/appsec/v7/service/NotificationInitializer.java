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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.v7.ui.AppSecUIProvider;
import com.vaadin.appsec.v7.ui.AppSecUI;
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

    /**
     * Initializes the notification service
     *
     * @param vaadinService
     *            current VaadinService instance
     */
    static void serviceInit(VaadinService vaadinService) {
        if (isDebugMode(vaadinService)) {
            vaadinService.addSessionInitListener(e -> {
                e.getSession().addUIProvider(new AppSecUIProvider());
                createNotificationThread(e.getSession()).start();
            });
            LOGGER.info("NotificationInitListener initialized.");
        }
    }

    private static Thread createNotificationThread(
            final VaadinSession session) {
        return new Thread(() -> {
            try {
                LOGGER.info(
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
    private static void notifyUiIfNeeded(VaadinSession session, UI ui) {
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

    private static void doNotifyUI(UI ui) {
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

    private static boolean isSessionOpen(VaadinSession session) {
        return !(session.getState() != VaadinSession.State.OPEN
                || session.getSession() == null);
    }

    private static boolean isDebugMode(VaadinService vaadinService) {
        return !vaadinService.getDeploymentConfiguration().isProductionMode();
    }
}
