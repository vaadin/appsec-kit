/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */
package com.vaadin.appsec.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.appsec.backend.AppSecScanEvent;
import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.Registration;
import com.vaadin.appsec.views.AppSecView;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.AnchorTarget;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.IronIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.SessionDestroyEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;

/**
 * A Vaadin service listener for initializing the notifications for AppSec Kit.
 * Will be initialized automatically by Vaadin.
 */
public class NotificationInitListener extends AbstractInitListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(NotificationInitListener.class);

    private final Map<UI, Registration> scanEventRegistrations = new ConcurrentHashMap<>();

    @Override
    public void serviceInit(ServiceInitEvent event) {
        VaadinService service = event.getSource();
        if (isDebugMode(service)) {
            service.addUIInitListener(this::subscribeUIToScanEvents);
            service.addSessionDestroyListener(this::removeUIRegistrations);
            LOGGER.info("Subscribed to AppSec Kit scan events");
            AppSecService appSecService = AppSecService.getInstance();
            appSecService.scanForVulnerabilities()
                    .thenRun(appSecService::scheduleAutomaticScan);
        }
    }

    private void removeUIRegistrations(SessionDestroyEvent event) {
        VaadinSession session = event.getSession();
        session.getUIs().forEach(ui -> {
            Registration registration = scanEventRegistrations.get(ui);
            if (registration != null) {
                registration.remove();
            }
            scanEventRegistrations.remove(ui);
            LOGGER.debug("Scan event listener removed");
        });
    }

    private void subscribeUIToScanEvents(UIInitEvent event) {
        UI ui = event.getUI();
        scanEventRegistrations.computeIfAbsent(ui, this::computeRegistration);
        LOGGER.debug("Scan event listener added");
    }

    private Registration computeRegistration(UI ui) {
        return AppSecService.getInstance().addScanEventListener(event -> {
            if (!event.getNewVulnerabilities().isEmpty()) {
                if (ui.getChildren().anyMatch(AppSecView.class::isInstance)) {
                    return;
                }
                ui.access(() -> doNotifyUI(event));
                LOGGER.debug("Notifying UI[" + ui.getUIId() + "]");
            }
        });
    }

    private void doNotifyUI(AppSecScanEvent event) {
        Notification notification = new Notification();
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
        notification.setPosition(Notification.Position.TOP_END);

        int duration = (int) AppSecService.getInstance().getConfiguration()
                .getAutoScanInterval().toMillis();
        notification.setDuration(duration);

        Div header = new Div(new Text("AppSec Kit"));
        header.getStyle().set("font-weight", "600");

        String infoStr = "%d vulnerabilities found.";
        Text info = new Text(
                String.format(infoStr, event.getNewVulnerabilities().size()));

        Anchor open = new Anchor("/" + AppSecService.getInstance()
                .getConfiguration().getAppSecRoute(), "Open AppSec Kit");
        open.setTarget(AnchorTarget.BLANK);
        Text text = new Text(" for details.");

        Div content = new Div(header, new Div(info), new Div(open, text));

        Button closeButton = new Button(new IronIcon("lumo", "cross"));
        closeButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        closeButton.getElement().setAttribute("aria-label", "Close");
        closeButton.addClickListener(buttonClickEvent -> notification.close());

        HorizontalLayout layout = new HorizontalLayout(content, closeButton);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        notification.add(layout);
        notification.open();
    }
}
