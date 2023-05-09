package com.vaadin.appsec.service;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

import org.cyclonedx.exception.ParseException;
import org.cyclonedx.model.Bom;
import org.cyclonedx.parsers.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.ServiceInitEvent;
import com.vaadin.server.VaadinServiceInitListener;

public class BillOfMaterialsServiceInitListener implements VaadinServiceInitListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BillOfMaterialsServiceInitListener.class);
    private static final String BOM_PATH = "/resources/bom.json";

    @Override
    public void serviceInit(ServiceInitEvent event) {
        URL resource = BillOfMaterialsServiceInitListener.class.getResource(BOM_PATH);
        if (resource != null) {
            JsonParser parser = new JsonParser();
            try {
                Bom bom = parser.parse(Paths.get(resource.toURI()).toFile());
                BillOfMaterialsService.getInstance().init(bom);
            } catch (URISyntaxException e) {
                LOGGER.error("Syntax error in BOM resource path: " + BOM_PATH, e);
            } catch (ParseException e) {
                LOGGER.error("Can't parse the BOM resource. ", e);
            }
        } else {
            LOGGER.error("Can't get BOM resource on path: " + BOM_PATH);
        }
    }
}
