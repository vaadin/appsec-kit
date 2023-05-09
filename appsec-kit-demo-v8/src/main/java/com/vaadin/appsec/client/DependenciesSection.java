package com.vaadin.appsec.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.StyleInjector;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.regexp.shared.SplitResult;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.vaadin.appsec.client.data.Dependency;
import com.vaadin.client.ApplicationConfiguration;
import com.vaadin.client.ApplicationConnection;
import com.vaadin.client.ComponentConnector;
import com.vaadin.client.FastStringSet;
import com.vaadin.client.LayoutManager;
import com.vaadin.client.Profiler;
import com.vaadin.client.ServerConnector;
import com.vaadin.client.VConsole;
import com.vaadin.client.ValueMap;
import com.vaadin.client.debug.internal.DebugButton;
import com.vaadin.client.debug.internal.FindModeHelper;
import com.vaadin.client.debug.internal.Icon;
import com.vaadin.client.debug.internal.Section;
import com.vaadin.client.ui.VVerticalLayout;
import com.vaadin.client.ui.ui.UIConnector;
import com.vaadin.client.widget.grid.datasources.ListDataSource;
import com.vaadin.client.widgets.Grid;

public class DependenciesSection implements Section {
    private static final RegExp selectorSplit = RegExp.compile("\\s*,\\s*");

    private final DebugButton tabButton = new DebugButton(Icon.WARNING,
            "Show vulnerabilities discovered in application dependencies");

    private final FlowPanel controls = new FlowPanel();

    private final Label statusLabel = new Label();
//    private final VerticalPanel content = new VerticalPanel();
private final VVerticalLayout content = new VVerticalLayout();
    private final FindModeHelper findModeHelper;
    private final DataGrid<Dependency> table;

    private boolean stylesInjected = false;

    public DependenciesSection() {
        DebugButton findButton = new DebugButton(Icon.HIGHLIGHT,
                "Select a component to find style name candidates for it");
        findModeHelper = new FindModeHelper(findButton) {
            @Override
            protected void onFind(ComponentConnector connector, boolean clicked) {
                if (clicked) {
                    findStylenames(connector);
                } else {
                    String connectorName = connector.getClass().getSimpleName();

                    showStatus(connectorName + ": "
                            + connector.getWidget().getStyleName());
                }
            }

            @Override
            public void stopFind() {
                super.stopFind();
                statusLabel.setText("");
            }
        };

        controls.add(findButton);

        statusLabel.addStyleName("styleNameFinderStatus");

//        Grid<Dependency> grid = new Grid<>();
//        grid.addColumn(new Grid.Column<String, Dependency>("Group") {
//            @Override
//            public String getValue(Dependency row) {
//                return row.getGroup();
//            }
//        }.setSortable(true));
//        grid.addColumn(new Grid.Column<String, Dependency>("Name") {
//            @Override
//            public String getValue(Dependency row) {
//                return row.getName();
//            }
//        }.setSortable(true));
//        grid.addColumn(new Grid.Column<String, Dependency>("Version") {
//            @Override
//            public String getValue(Dependency row) {
//                return row.getVersion();
//            }
//        }.setSortable(true));
//
        Dependency d = new Dependency("javax.servlet", "javax.servlet-api", "3.0.1");
        Dependency dd = new Dependency("com.vaadin", "vaadin-server", "8.20.0");
        ArrayList<Dependency> list = new ArrayList<>();
        list.add(d);
        list.add(dd);
//        grid.setDataSource(new ListDataSource<Dependency>(d,dd));
//
//        grid.setWidth("100%");
//        grid.setHeight("100%");
//
//        grid.getEscalator().onResize();
//
//        content.setWidth("100%");
//        content.setHeight("100%");
//        content.add(grid);

        table = new DataGrid<>();
        table.setSize("100%", "100%");

        ListDataProvider<Dependency> dataProvider = new ListDataProvider<Dependency>(list);
        dataProvider.addDataDisplay(table);

        ColumnSortEvent.ListHandler<Dependency> sortHandler = new ColumnSortEvent.ListHandler<Dependency>(dataProvider.getList());
        table.addColumnSortHandler(sortHandler);

        Column<Dependency, String> groupColumn = new Column<Dependency, String>(
                new TextCell()) {
            @Override
            public String getValue(Dependency d) {
                return d.getGroup();
            }
        };
        groupColumn.setSortable(true);
        sortHandler.setComparator(groupColumn, new Comparator<Dependency>() {
            @Override
            public int compare(Dependency o1, Dependency o2) {
                return o1.getGroup().compareTo(o2.getGroup());
            }
        });
        table.addColumn(groupColumn, "Group");

        Column<Dependency, String> nameColumn = new Column<Dependency, String>(
                new TextCell()) {
            @Override
            public String getValue(Dependency d) {
                return d.getName();
            }
        };
        nameColumn.setSortable(true);
        sortHandler.setComparator(nameColumn, new Comparator<Dependency>() {
            @Override
            public int compare(Dependency o1, Dependency o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        table.addColumn(nameColumn, "Name");

        Column<Dependency, String> versionColumn = new Column<Dependency, String>(
                new TextCell()) {
            @Override
            public String getValue(Dependency d) {
                return d.getVersion();
            }
        };
        versionColumn.setSortable(true);
        sortHandler.setComparator(versionColumn, new Comparator<Dependency>() {
            @Override
            public int compare(Dependency o1, Dependency o2) {
                return o1.getVersion().compareTo(o2.getVersion());
            }
        });
        table.addColumn(versionColumn, "Version");

        table.getColumnSortList().push(groupColumn);

        content.setSize("100%", "100%");

        ResizeLayoutPanel rlp = new ResizeLayoutPanel();
        rlp.add(table);
        rlp.setSize("100%", "100%");
        content.add(rlp);
    }

    private void showStatus(String status) {
        statusLabel.setText(status);
        if (!statusLabel.isAttached()) {
//            content.setWidget(statusLabel);
        }
    }

    public void findStylenames(ComponentConnector connector) {
        Profiler.enter("findStylenames");
        Widget widget = connector.getWidget();

        Profiler.enter("getAllSelectors");
        JsArrayString selectors = getAllSelectors(widget.getStylePrimaryName());
        Profiler.leave("getAllSelectors");

        Profiler.enter("orderStyleNames");
        List<StyleNameMatch> styleNames = StyleNameMatch.orderStyleNames(
                selectors, widget);
        Profiler.leave("orderStyleNames");

        if (styleNames.isEmpty()) {
            showStatus("No style name candidates found");
        } else {
            Profiler.enter("buildStyleNameSelectorWidget");
            Widget styleNameSelector = buildStyleNameSelectorWidget(connector,
                    styleNames);
            Profiler.leave("buildStyleNameSelectorWidget");
//            content.setWidget(styleNameSelector);
        }
        Profiler.leave("findStylenames");

        Profiler.logTimings();
    }

    private Widget buildStyleNameSelectorWidget(
            final ComponentConnector connector, List<StyleNameMatch> styleNames) {
        FlowPanel panel = new FlowPanel();
        panel.addStyleName("styleNameFinderPreview");
        panel.add(new Label(
                "Hover style name candidate to preview, click to apply and run a full layout."));

        final Widget widget = connector.getWidget();
        final String originalStyleNames = widget.getStyleName();

        for (StyleNameMatch styleNameMatch : styleNames) {
            final String styleName = styleNameMatch.getStyleName();
            Button button = new Button(styleName);
            button.addMouseOverHandler(new MouseOverHandler() {
                @Override
                public void onMouseOver(MouseOverEvent event) {
                    widget.addStyleDependentName(styleName);
                    widget.addStyleName(styleName);
                }
            });
            button.addMouseOutHandler(new MouseOutHandler() {
                @Override
                public void onMouseOut(MouseOutEvent event) {
                    widget.setStyleName(originalStyleNames);
                }
            });
            button.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    widget.addStyleDependentName(styleName);
                    widget.addStyleName(styleName);
                    LayoutManager.get(connector.getConnection())
                            .setNeedsMeasureRecursively(connector);
                    showStatus("Applied style name: " + styleName);
                }
            });

            panel.add(button);
        }

        return panel;
    }

    private JsArrayString getAllSelectors(String filter) {
        FastStringSet selectorSet = FastStringSet.create();

        JsArray<StyleSheet> styleSheets = getStyleSheets();
        for (int i = 0; i < styleSheets.length(); i++) {
            JsArray<CssRule> cssRules = styleSheets.get(i).getCssRules();
            for (int j = 0; j < cssRules.length(); j++) {
                String selectorText = cssRules.get(j).getSelectorText();
                if (selectorText != null && selectorText.contains(filter)) {
                    SplitResult splitResult = selectorSplit.split(selectorText);
                    for (int k = 0; k < splitResult.length(); k++) {
                        selectorSet.add(splitResult.get(k));
                    }
                }
            }
        }

        return selectorSet.dump();
    }

    public void setDependencies(List<Dependency> dependencies) {
        ListDataProvider<Dependency> dataProvider = new ListDataProvider<Dependency>(dependencies);
        dataProvider.addDataDisplay(table);
        ColumnSortEvent.ListHandler<Dependency> sortHandler = new ColumnSortEvent.ListHandler<Dependency>(dataProvider.getList());
        table.addColumnSortHandler(sortHandler);
    }

    private static final class CssRule extends JavaScriptObject {
        protected CssRule() {

        }

        public native String getSelectorText()
        /*-{
          return this.selectorText;
        }-*/;
    }

    private static final class StyleSheet extends JavaScriptObject {
        protected StyleSheet() {

        }

        public native JsArray<CssRule> getCssRules()
        /*-{
          return this.cssRules;
        }-*/;
    }

    private native JsArray<StyleSheet> getStyleSheets()
    /*-{
        return $doc.styleSheets;
    }-*/;

    @Override
    public DebugButton getTabButton() {
        return tabButton;
    }

    @Override
    public Widget getControls() {
        return controls;
    }

    @Override
    public Widget getContent() {
        return content;
    }

    @Override
    public void show() {
        if (!stylesInjected) {
            // Inject some styles the first time the tab is opened
            StyleInjector
                    .inject(".styleNameFinderPreview button { display: block; color: #666}\n"
                            + ".styleNameFinderPreview button:hover { color: #000; }\n"
                            + ".styleNameFinderPreview, .styleNameFinderStatus { margin: 10px }\n",
                            true);
            stylesInjected = true;
        }

        update();
    }

    @Override
    public void hide() {
        findModeHelper.stopFind();
    }

    @Override
    public void meta(ApplicationConnection ac, ValueMap meta) {
        // noop
    }

    @Override
    public void uidl(ApplicationConnection ac, ValueMap uidl) {
        // noop
    }

    public void update() {
        List<ApplicationConnection> runningApplications = ApplicationConfiguration
                .getRunningApplications();
        UIConnector uiConn = runningApplications.get(0).getUIConnector();
        AppSecConnector conn = null;
        for (ServerConnector child : uiConn.getChildren()) {
            if(child instanceof AppSecConnector) {
                conn = (AppSecConnector) child;
            }
        }
        conn.setDependenciesSection(this);


        VConsole.error("Connector: " + conn);
    }
}
