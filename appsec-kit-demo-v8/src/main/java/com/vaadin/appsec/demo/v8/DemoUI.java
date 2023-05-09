package com.vaadin.appsec.demo.v8;

import javax.servlet.annotation.WebServlet;

import org.cyclonedx.model.Component;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.appsec.extension.AppSecUIExtension;
import com.vaadin.appsec.service.BOMService;
import com.vaadin.appsec.ui.AppSecButton;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("demouitheme")
public class DemoUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();

        layout.addComponents(new Label("AppSec Kit Vaadin 8 Demo Application"));

        Grid<Component> grid = new Grid<>();
        grid.setItems(BOMService.getInstance().getBom().getComponents());
        grid.addColumn(Component::getGroup).setCaption("Group");
        grid.addColumn(Component::getName).setCaption("Name");
        grid.addColumn(Component::getVersion).setCaption("Version");
        grid.setSizeFull();
        layout.addComponent(grid);

        setContent(layout);

        AppSecUIExtension appSecUIExtension = new AppSecUIExtension();
        appSecUIExtension.extend(this);

        layout.addComponent(new AppSecButton());
    }

    @WebServlet(urlPatterns = "/*", name = "DemoUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = DemoUI.class, productionMode = false, widgetset = "com.vaadin.appsec.WidgetSet")
    public static class MyUIServlet extends VaadinServlet {
    }
}
