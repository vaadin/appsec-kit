# Microsoft sbom-tool

- Command line tool
- Supported schemas:
    - SPDX 2.2 (JSON)

Generating SBOM from project's root with sbom-tool:
```
sbom-tool generate -b ./sbom/sbom-tool -bc . -pn Test -pv 1.0.0 -ps MyCompany -nsb https://sbom.mycompany.com -V Verbose
```

### Links

- Documentation:
https://github.com/microsoft/sbom-tool
- CLI Reference:
https://github.com/microsoft/sbom-tool/blob/main/docs/sbom-tool-cli-reference.md
- GitHub actions:
https://github.com/microsoft/sbom-tool/blob/main/docs/setting-up-github-actions.md
