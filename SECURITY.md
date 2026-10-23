# Security

- Phase 1 contains no network permission, no API keys, no analytics, no ads.
- No secrets are hardcoded; `.gitignore` excludes keystores and local properties.
- Assistant replies are generated locally; AI output is untrusted and is never executed as commands.
- Known limitations of Phase 1: debug APK is not signed for release; this file will
  expand in Phase 8 with the full threat model, checklist, and tests.
