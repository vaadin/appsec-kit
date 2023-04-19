How to install:

https://aquasecurity.github.io/trivy/v0.40/getting-started/installation/

https://github.com/aquasecurity/trivy

SBOM generation: https://aquasecurity.github.io/trivy/v0.40/docs/sbom/

SBOM scanning: https://aquasecurity.github.io/trivy/v0.40/docs/target/sbom/

Generating SBOM from project's root with sbom-tool:
```
trivy fs --format cyclonedx --output trivy-sbom.json <PROJECT_DIRETORY>
```
```
trivy fs --format spdx-json --output trivy-spdx-sbom.json <PROJECT_DIRETORY>
```
