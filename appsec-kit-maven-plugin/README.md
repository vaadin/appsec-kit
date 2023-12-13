AppSec Kit Maven Plugin
=========

The AppSec Kit Maven plugin generates CycloneDX Software Bill of Materials (SBOM) containing the aggregate of all direct
and transitive dependencies of a project. CycloneDX is a lightweight software bill of materials
(SBOM) standard designed for use in application security contexts and supply chain component analysis.

Maven Usage
-------------------

```xml
<!-- uses default configuration -->
<plugins>
    <plugin>
        <groupId>com.vaadin</groupId>
        <artifactId>appsec-kit-maven-plugin</artifactId>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>makeAggregateBom</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
</plugins>
```


Default Values
-------------------
```xml
<plugins>
    <plugin>
        <groupId>com.vaadin</groupId>
        <artifactId>appsec-kit-maven-plugin</artifactId>
        <configuration>
            <projectType>library</projectType>
            <schemaVersion>1.4</schemaVersion>
            <includeBomSerialNumber>true</includeBomSerialNumber>
            <includeCompileScope>true</includeCompileScope>
            <includeProvidedScope>true</includeProvidedScope>
            <includeRuntimeScope>true</includeRuntimeScope>
            <includeSystemScope>true</includeSystemScope>
            <includeTestScope>false</includeTestScope>
            <includeLicenseText>false</includeLicenseText>
            <outputReactorProjects>true</outputReactorProjects>
            <outputFormat>all</outputFormat>
            <outputName>bom</outputName>
            <outputDirectory>${project.build.directory}</outputDirectory><!-- usually target, if not redefined in pom.xml -->
            <verbose>false</verbose><!-- = ${cyclonedx.verbose} -->
        </configuration>
    </plugin>
</plugins>
```

`<projectType>` default value is `library` but there are [more choices defined in the CycloneDX specification](https://cyclonedx.org/docs/1.5/json/#metadata_component_type).

Excluding Projects
-------------------
With `makeAggregateBom` goal, it is possible to exclude certain Maven reactor projects (aka modules) from getting included in the aggregate BOM:

* Pass `-DexcludeTestProject` to exclude any Maven module with artifactId containing the word "test"
* Pass `-DexcludeArtifactId=comma separated id` to exclude based on artifactId
* Pass `-DexcludeGroupId=comma separated id` to exclude based on groupId

Goals
-------------------
The AppSec Kit Maven plugin contains the following three goals:
* `makeBom`: creates a BOM for each Maven module with its dependencies,
* `makeAggregateBom`: creates an aggregate BOM at build root (with dependencies from the whole multi-modules build), and eventually a BOM for each module,
* `makePackageBom`: creates a BOM for each Maven module with `war` or `ear` packaging.

By default, the BOM(s) will be attached as an additional artifacts with `cyclonedx` classifier and `xml` or `json` extension during a Maven `install` or `deploy`:

* `${project.artifactId}-${project.version}-cyclonedx.xml`
* `${project.artifactId}-${project.version}-cyclonedx.json`

This may be switched off by setting `cyclonedx.skipAttach` to `true`.

Every goal can optionally be skipped completely by setting `cyclonedx.skip` to `true`.

Copyright & License
-------------------

This plugin is based on the [CycloneDX Maven Plugin](https://github.com/CycloneDX/cyclonedx-maven-plugin).
CycloneDX Maven Plugin is Copyright (c) OWASP Foundation. All Rights Reserved.

Permission to modify and redistribute is granted under the terms of the Apache 2.0 license. See the [LICENSE] file for the full license.

[License]: https://github.com/CycloneDX/cyclonedx-maven-plugin/blob/master/LICENSE
