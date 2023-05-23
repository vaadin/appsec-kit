/*-
 * Copyright (C) 2023 Vaadin Ltd
 *
 * This program is available under Vaadin Commercial License and Service Terms.
 *
 *
 * See <https://vaadin.com/commercial-license-and-service-terms> for the full license.
 */

package com.vaadin.appsec.v8.ui;

import com.vaadin.ui.Button;
import com.vaadin.ui.UI;

/**
 * A button that instantiates and opens the app sec dialog.
 */
public class AppSecButton extends Button {

    /**
     * Instantiates a new App sec button.
     */
    public AppSecButton() {
        setCaption("Open");
        addClickListener(e -> {
            AppSecDialog asd = new AppSecDialog();
            UI.getCurrent().addWindow(asd);
        });
    }
}
