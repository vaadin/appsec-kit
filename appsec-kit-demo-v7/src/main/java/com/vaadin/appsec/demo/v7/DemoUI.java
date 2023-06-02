/*
 * -
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.demo.v7;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

@Theme("valo")
@Push(transport = Transport.LONG_POLLING)
public class DemoUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();

        layout.addComponent(new Label("AppSec Kit Vaadin 7 Demo Application"));
        layout.addComponent(new Label("SBOM components:"));

        // Grid<Component> sbomGrid = new Grid<>();
        // sbomGrid.setItems(
        // BillOfMaterialsStore.getInstance().getBom().getComponents());
        // sbomGrid.addColumn(Component::getGroup).setCaption("Group");
        // sbomGrid.addColumn(Component::getName).setCaption("Name");
        // sbomGrid.addColumn(Component::getVersion).setCaption("Version");
        // sbomGrid.setSizeFull();
        // layout.addComponent(sbomGrid);
        //
        // layout.addComponent(new Label("Vulnerabilities:"));
        //
        // Grid<OpenSourceVulnerability> vulnGrid = new Grid<>();
        // vulnGrid.setItems(
        // VulnerabilityStore.getInstance().getVulnerabilities());
        // vulnGrid.addColumn(OpenSourceVulnerability::getId).setCaption("Id");
        // vulnGrid.addColumn(OpenSourceVulnerability::getSummary)
        // .setCaption("Summary");
        // vulnGrid.addColumn(OpenSourceVulnerability::getAliases,
        // OpenSourceVulnerabilityUtils::getAliasesStr)
        // .setCaption("Aliases");
        // vulnGrid.addColumn(OpenSourceVulnerability::getSeverity,
        // OpenSourceVulnerabilityUtils::getCvssVectorsStr)
        // .setCaption("CVSS vectors");
        // vulnGrid.addColumn(OpenSourceVulnerability::getSeverity,
        // OpenSourceVulnerabilityUtils::getScoresStr)
        // .setCaption("Scores");
        // vulnGrid.setSizeFull();
        // layout.addComponent(vulnGrid);

        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "DemoUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = DemoUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
