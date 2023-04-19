How to install:
https://github.com/microsoft/sbom-tool

GitHub actions:
https://github.com/microsoft/sbom-tool/blob/main/docs/setting-up-github-actions.md

Generating SBOM from project's root with sbom-tool:
```
sbom-tool generate -b ./sbom/sbom-tool -bc . -pn Test -pv 1.0.0 -ps MyCompany -nsb https://sbom.mycompany.com -V Verbose
```
