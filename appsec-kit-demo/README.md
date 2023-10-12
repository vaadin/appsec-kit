AppSec Kit Demo Vaadin 24 Spring
===============================

A simple application for testing and demonstrating Vaadin AppSec Kit with Vaadin 24 and Spring.

Prerequisites
=============

Install `@cyclonedx/cyclonedx-npm` plugin as a global tool ala `npx`:

```sh
npx --package @cyclonedx/cyclonedx-npm --call exit
```

Workflow
========

To generate the `Maven` and `npm` SBOM files under the `/resources`, run
```sh
mvn install
```

To run the application, run
```sh
`mvn spring-boot:run`
```
and open http://localhost:8080/.
