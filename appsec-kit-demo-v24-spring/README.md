AppSec Kit Demo Vaadin 24 Spring
===============================

A simple application for testing and demonstrating Vaadin AppSec Kit with Vaadin 24 and Spring.

Workflow
========

To generate the Maven SBOM, run
```sh
mvn install
```

To generate the npm SBOM, run
```sh
npm install
```
The `@cyclonedx/cyclonedx-npm` plugin is run in the `postinstall` script, it requires the `target/classes/resources` folder to be present. See [package.json](package.json).

To run the application, run
```sh
`mvn spring-boot:run`
```
and open http://localhost:8080/.
