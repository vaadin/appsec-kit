package com.vaadin.appsec.ui;

import com.vaadin.ui.Button;
import com.vaadin.ui.UI;

public class AppSecButton extends Button {

    public AppSecButton() {
        setCaption("Open");
        addClickListener(e -> {
            AppSecDialog asd = new AppSecDialog();
            UI.getCurrent().addWindow(asd);
        });
    }
}
