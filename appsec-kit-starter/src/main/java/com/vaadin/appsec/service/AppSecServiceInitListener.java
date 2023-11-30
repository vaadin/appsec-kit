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
import java.util.concurrent.atomic.AtomicBoolean;

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
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.SessionDestroyEvent;
import com.vaadin.flow.server.UIInitEvent;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinServiceInitListener;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.Version;
import com.vaadin.flow.shared.communication.PushMode;

/**
 * A Vaadin service listener for registering the AppSec Kit route and
 * initializing AppSec Kit services. Will be invoked automatically by Vaadin.
 */
public class AppSecServiceInitListener implements VaadinServiceInitListener {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(AppSecServiceInitListener.class);

    private static final AtomicBoolean pushWarningShown = new AtomicBoolean(
            false);

    private final Map<UI, Registration> scanEventRegistrations = new ConcurrentHashMap<>();

    @Override
    public void serviceInit(ServiceInitEvent event) {
        VaadinService vaadinService = event.getSource();
        if (isDebugMode(vaadinService)) {
            AppSecService appSecService = AppSecService.getInstance();

            registerRoute(appSecService.getConfiguration().getAppSecRoute());
            vaadinService.addUIInitListener(this::addAfterNavigationListener);
            appSecService.init();
            vaadinService.addUIInitListener(this::subscribeUIToScanEvents);
            vaadinService
                    .addSessionDestroyListener(this::removeUIRegistrations);
            LOGGER.info("AppSec Kit initialized");

            appSecService.scanForVulnerabilities()
                    .thenRun(appSecService::scheduleAutomaticScan);
        } else {
            LOGGER.info("AppSec Kit not enabled in production mode. Run the "
                    + "application in debug mode to initialize AppSec Kit");
        }
    }

    private boolean isDebugMode(VaadinService vaadinService) {
        return !vaadinService.getDeploymentConfiguration().isProductionMode();
    }

    private void registerRoute(String path) {
        RouteConfiguration configuration = RouteConfiguration
                .forApplicationScope();
        configuration.setRoute(path, AppSecView.class);
    }

    private void addAfterNavigationListener(UIInitEvent event) {
        event.getUI().addAfterNavigationListener(this::checkForPush);
    }

    private void checkForPush(AfterNavigationEvent event) {
        UI ui = UI.getCurrent();
        if (!canPushChanges(ui) && isActivationEnabled()) {
            ui.getPushConfiguration().setPushMode(PushMode.AUTOMATIC);

            boolean warningAlreadyShown = pushWarningShown.getAndSet(true);
            if (!warningAlreadyShown) {
                int flowVersionInVaadin14 = 2;
                String annotationLocation = Version
                        .getMajorVersion() == flowVersionInVaadin14
                                ? "root layout or individual views"
                                : "AppShellConfigurator class";

                LOGGER.warn(
                        "Server push has been automatically enabled so updates can be shown immediately. "
                                + "Add @Push annotation on your "
                                + annotationLocation
                                + " to suppress this warning. "
                                + "Set automaticallyActivatePush to false in AppSecConfiguration if you want to ensure push is not automatically enabled.");
            }
        }
    }

    private boolean canPushChanges(UI ui) {
        return ui.getPushConfiguration().getPushMode().isEnabled()
                || ui.getPollInterval() > 0;
    }

    private boolean isActivationEnabled() {
        return AppSecService.getInstance().getConfiguration()
                .isAutomaticallyActivatePush();
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
        scanEventRegistrations.computeIfAbsent(event.getUI(),
                this::computeRegistration);
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

        Button closeButton = new Button(new Icon(VaadinIcon.CLOSE_SMALL));
        closeButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        closeButton.getElement().setAttribute("aria-label", "Close");
        closeButton.addClickListener(buttonClickEvent -> notification.close());

        HorizontalLayout layout = new HorizontalLayout(content, closeButton);
        layout.setAlignItems(FlexComponent.Alignment.CENTER);

        notification.add(layout);
        notification.open();
    }
}
