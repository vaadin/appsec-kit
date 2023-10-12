AppSec Kit Demo
===============

A simple application for testing and demonstrating Vaadin AppSec Kit.

Prerequisites
=============

Install `@cyclonedx/cyclonedx-npm` plugin as a global tool ala `npx`:

```sh
npx --package @cyclonedx/cyclonedx-npm --call exit
```

Workflow
========

Install npm dependencies before running the `@cyclonedx/cyclonedx-npm` plugin:
```sh
npm install
```

Generate the `Maven` and `npm` SBOM files under the `/resources`:
```sh
mvn install
```

Run the application:
```sh
`mvn spring-boot:run`
```
Open http://localhost:8080/.
