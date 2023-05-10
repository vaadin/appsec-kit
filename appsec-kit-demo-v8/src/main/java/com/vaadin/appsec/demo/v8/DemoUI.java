package com.vaadin.appsec.demo.v8;

import javax.servlet.annotation.WebServlet;

import org.cyclonedx.model.Component;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.appsec.model.osv.response.OpenSourceVulnerability;
import com.vaadin.appsec.service.BillOfMaterialsStore;
import com.vaadin.appsec.service.VulnerabilityStore;
import com.vaadin.appsec.util.OpenSourceVulnerabilityUtil;
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

        layout.addComponent(new Label("AppSec Kit Vaadin 8 Demo Application"));

        layout.addComponent(new Label("SBOM components:"));

        Grid<Component> sbomGrid = new Grid<>();
        sbomGrid.setItems(BillOfMaterialsStore.getInstance().getBom().getComponents());
        sbomGrid.addColumn(Component::getGroup).setCaption("Group");
        sbomGrid.addColumn(Component::getName).setCaption("Name");
        sbomGrid.addColumn(Component::getVersion).setCaption("Version");
        sbomGrid.setSizeFull();
        layout.addComponent(sbomGrid);

        layout.addComponent(new Label("Vulnerabilities:"));

        Grid<OpenSourceVulnerability> vulnGrid = new Grid<>();
        vulnGrid.setItems(VulnerabilityStore.getInstance().getVulnerabilities());
        vulnGrid.addColumn(OpenSourceVulnerability::getId).setCaption("Id");
        vulnGrid.addColumn(OpenSourceVulnerability::getSummary).setCaption("Summary");
        vulnGrid.addColumn(OpenSourceVulnerability::getAliases, OpenSourceVulnerabilityUtil::getAliasesStr).setCaption("Aliases");
        vulnGrid.addColumn(OpenSourceVulnerability::getSeverity, OpenSourceVulnerabilityUtil::getCvssVectorsStr).setCaption("CVSS vectors");
        vulnGrid.addColumn(OpenSourceVulnerability::getSeverity, OpenSourceVulnerabilityUtil::getScoresStr).setCaption("Scores");
        vulnGrid.setSizeFull();
        layout.addComponent(vulnGrid);

        setContent(layout);
    }

    @WebServlet(urlPatterns = "/*", name = "DemoUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = DemoUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
