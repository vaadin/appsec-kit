/*
 * -
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v7.ui.content;

import java.util.stream.Collectors;

import com.vaadin.appsec.backend.AppSecService;
import com.vaadin.appsec.backend.model.dto.Dependency;
import com.vaadin.appsec.backend.model.dto.SeverityLevel;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;

/**
 * Dependencies tab content
 */
public class DependenciesTab extends AbstractAppSecContent {
    private Grid grid;
    private ComboBox group;
    private ComboBox severity;
    private AppSecView parent;

    /**
     * Instantiates a new Dependencies tab.
     */
    public DependenciesTab(AppSecView parent) {
        this.parent = parent;
        buildFilters();
        buildGrid();
        setMargin(true);
    }

    private void buildFilters() {
        group = new ComboBox("Dependency group");

        severity = new ComboBox("Severity level");
        severity.setContainerDataSource(buildSeverityContainer());

        buildFilterBar(group, severity);
    }

    protected void clearFilters() {
        group.setValue(null);
        severity.setValue(null);
        getContainer().removeAllContainerFilters();
    }

    protected void applyFilters() {
        getContainer().removeAllContainerFilters();

        final String groupFilter = (String) group.getValue();
        final SeverityLevel severityFilter = (SeverityLevel) severity
                .getValue();

        getContainer().addContainerFilter(new Container.Filter() {
            @Override
            public boolean passesFilter(Object itemId, Item item)
                    throws UnsupportedOperationException {
                Dependency dependencyDTO = (Dependency) itemId;
                if (groupFilter != null
                        && !groupFilter.equals(dependencyDTO.getGroup())) {
                    return false;
                }
                if (severityFilter != null && !severityFilter
                        .equals(dependencyDTO.getSeverityLevel())) {
                    return false;
                }
                return true;
            }

            @Override
            public boolean appliesToProperty(Object propertyId) {
                return "group".equals(propertyId)
                        || "severityLevel".equals(propertyId);
            }
        });
    }

    private void buildGrid() {
        grid = new Grid();
        grid.setSizeFull();

        BeanItemContainer<Dependency> cont = new BeanItemContainer<>(
                Dependency.class);
        grid.setContainerDataSource(cont);
        grid.removeAllColumns();
        grid.addColumn("name");
        grid.addColumn("numOfVulnerabilities");
        grid.addColumn("group");
        grid.addColumn("version");
        grid.addColumn("severityLevel");
        grid.addColumn("riskScore");
        grid.getColumn("name").setHeaderCaption("Dependency");
        grid.getColumn("numOfVulnerabilities")
                .setHeaderCaption("# of vulnerabilities");
        grid.getColumn("group").setHeaderCaption("Dependency group");
        grid.getColumn("severityLevel").setHeaderCaption("Severity");
        grid.getColumn("riskScore").setHeaderCaption("Risk score");

        getMainContent().addComponent(grid);
        getMainContent().setExpandRatio(grid, 1);

        grid.addItemClickListener(e -> {
            if (e.isDoubleClick()) {
                parent.showVulnerabilitiesTabFor((Dependency) e.getItemId());
            }
        });
    }

    @SuppressWarnings("unchecked")
    private BeanItemContainer<Dependency> getContainer() {
        return (BeanItemContainer<Dependency>) grid.getContainerDataSource();
    }

    public void refresh() {
        getContainer().removeAllItems();
        AppSecService.getInstance().getDependencies()
                .forEach(dep -> getContainer().addBean(dep));

        BeanItemContainer<String> groupsCont = new BeanItemContainer<>(
                String.class);
        getContainer().getItemIds().stream().map(Dependency::getGroup)
                .collect(Collectors.toSet()).forEach(groupsCont::addBean);
        group.setContainerDataSource(groupsCont);
    }
}
