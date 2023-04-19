How to install:
https://github.com/anchore/syft

Generating SBOM from project's root in syft and spdx formats:
```
syft <PROJECT_DIRECTORY> -o json=sbom.syft.json -o spdx-json=sbom.spdx.json
```
