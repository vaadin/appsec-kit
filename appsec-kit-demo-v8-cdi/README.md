AppSec Kit Demo Vaadin 8 CDI
===============================

A simple application for testing and demonstrating Vaadin AppSec Kit with Vaadin 8 and CDI.

Note: This application uses an older version of Vaadin 8 (8.8.0) in order to have
some vulnerabilities to show in the scan results.

Workflow
========

To run the application, run `mvn wildfly:run` and open http://localhost:8080/.

---

To debug the application, add this setting to the `wildfly-maven-plugin` configuration in the `pom.xml` file

```
<javaOpts>
    <javaOpt>-agentlib:jdwp=transport=dt_socket,address=8787,server=y,suspend=y</javaOpt>
</javaOpts>
```

run `mvn wildfly:run` and attach a remote debugger.