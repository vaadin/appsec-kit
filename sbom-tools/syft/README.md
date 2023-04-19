# Syft

- Command line tool
- Format conversion
- Supported schemas:
  - JSON
  - text: A row-oriented, human-and-machine-friendly output
  - cyclonedx-xml
  - cyclonedx-json
  - spdx-tag-value: A tag-value formatted report conforming to the SPDX specification
  - spdx-json: A JSON report conforming to the SPDX JSON Schema
  - github: A JSON report conforming to GitHub's dependency snapshot format
  - table: A columnar summary (default)
  - template: User specified output format

Generating SBOM from project's root in syft and spdx formats:
```
syft <PROJECT_DIRECTORY> -o json=sbom.syft.json -o spdx-json=sbom.spdx.json
```

### Links
- Documentation:
https://github.com/anchore/syft
