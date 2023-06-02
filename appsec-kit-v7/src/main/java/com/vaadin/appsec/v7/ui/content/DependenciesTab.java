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

import com.vaadin.appsec.v7.data.DependencyDTO;
import com.vaadin.appsec.v7.data.SeverityLevel;
import com.vaadin.appsec.v7.service.AppSecDataProvider;
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

    /**
     * Instantiates a new Dependencies tab.
     */
    public DependenciesTab() {
        buildFilters();
        buildGrid();
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
                DependencyDTO dependencyDTO = (DependencyDTO) itemId;
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

        BeanItemContainer<DependencyDTO> cont = new BeanItemContainer<>(
                DependencyDTO.class);
        grid.setContainerDataSource(cont);
        grid.setColumns("name", "numOfVulnerabilities", "group", "version",
                "severityLevel", "riskScore");
        grid.getColumn("name").setHeaderCaption("Dependency");
        grid.getColumn("numOfVulnerabilities")
                .setHeaderCaption("# of vulnerabilities");
        grid.getColumn("group").setHeaderCaption("Dependency group");
        grid.getColumn("severityLevel").setHeaderCaption("Severity");
        grid.getColumn("riskScore").setHeaderCaption("Risk score");

        addComponent(grid);
        setExpandRatio(grid, 1);

        grid.addItemClickListener(item -> {
            // TODO Open details view for clicked dependency
        });
    }

    @SuppressWarnings("unchecked")
    private BeanItemContainer<DependencyDTO> getContainer() {
        return (BeanItemContainer<DependencyDTO>) grid.getContainerDataSource();
    }

    public void refresh() {
        getContainer().removeAllItems();
        AppSecDataProvider.getDependencies()
                .forEach(dep -> getContainer().addBean(dep));

        BeanItemContainer<String> groupsCont = new BeanItemContainer<>(
                String.class);
        getContainer().getItemIds().stream().map(DependencyDTO::getGroup)
                .collect(Collectors.toSet()).forEach(groupsCont::addBean);
        group.setContainerDataSource(groupsCont);
    }
}
