appsec-kit-demo-v8
==============

A simple application for testing and demonstrating Vaadin AppSec Kit with Vaadin 8.

Note: This application uses an older version of Vaadin 8 (8.8.0) in order to have
some vulnerabilities to show in the scan results.

Workflow
========

To built the application, run "mvn install". This needs to be run before jetty is
started in order to generate the SBOM file. 

To run the application, run "mvn jetty:run" and open http://localhost:8080/.