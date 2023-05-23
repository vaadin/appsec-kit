package com.vaadin.appsec.v8.service;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.appsec.v8.ui.AppSecUI;
import com.vaadin.appsec.v8.ui.AppSecUIProvider;
import com.vaadin.server.ServiceInitEvent;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServiceInitListener;
import com.vaadin.server.VaadinSession;
import com.vaadin.shared.Position;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

/**
 * A Vaadin service init listener for initializing the notification mechanism.
 * Will be initialized automatically by Vaadin.
 */
public class NotificationInitListener implements VaadinServiceInitListener {

    private static final int NOTIFICATION_CHECK_INTERVAL = 5000; // ms

    private static final String NOTIFIED_UIS_SESSION_PARAM = "vaadin-appsec-kit-notified-uis";

    private static final int DELAY = 30 * 60 * 1000; // 30 mins

    @Override
    public void serviceInit(ServiceInitEvent event) {
        VaadinService vaadinService = event.getSource();
        if (AppSecUtil.isDebugMode(vaadinService)) {
            vaadinService.addSessionInitListener(e -> {
                e.getSession().addUIProvider(new AppSecUIProvider());
                createNotificationThread(e.getSession()).start();
            });
        }
    }

    private Thread createNotificationThread(final VaadinSession session) {
        return new Thread(() -> {
            try {
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
        n.setDelayMsec(DELAY);
        n.setHtmlContentAllowed(true);
        n.show(ui.getPage());
    }

    private boolean isSessionOpen(VaadinSession session) {
        return !(session.getState() != VaadinSession.State.OPEN
                || session.getSession() == null);
    }
}
