# Trivy

- Command line tool
- Supported schemas:
  - CycloneDX (JSON)
  - SPDX (JSON)

Generating SBOM from project's root:
```
trivy fs --format cyclonedx --output trivy-cyclonedx-sbom.json <PROJECT_DIRETORY>
```
```
trivy fs --format spdx-json --output trivy-spdx-sbom.json <PROJECT_DIRETORY>
```

### Links

- https://github.com/aquasecurity/trivy
- How to install:
https://aquasecurity.github.io/trivy/v0.40/getting-started/installation/
- SBOM generation:
https://aquasecurity.github.io/trivy/v0.40/docs/sbom/
- SBOM scanning:
https://aquasecurity.github.io/trivy/v0.40/docs/target/sbom/
