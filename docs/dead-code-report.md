# Dead Code Report (2026-05-31)

## Scope
- Static reachability check over `src/main/java`, `src/test/java`, `src/main/resources`, `src/main/webapp`.
- Manual validation for Spring-discovered classes (controllers/filters/listeners) to avoid false positives.

## Findings
1. `src/main/java/com/importease/proyecto/service/SpringDataBridgeService.java`
- Status: `UNUSED`
- Evidence: no inbound references (`rg "SpringDataBridgeService"` only returns its own file).
- Decision: `REMOVE`
- Reason: migration path now handled directly in DAOs via Spring Data repositories + fallback.

2. `src/main/resources/db/migration/V20260520__create_orders.sql`
- Status: `UNUSED`
- Evidence:
  - no Flyway/Liquibase configured in `pom.xml`.
  - no runtime references to this SQL file from Java/resources/tests.
  - domain tables `orders/order_items` are not referenced by current web flow.
- Decision: `REMOVE`

3. `src/main/resources/static/` and `src/main/resources/templates/`
- Status: `EMPTY`
- Decision: `KEEP (placeholder)`
- Reason: kept intentionally for the agreed Spring Boot structure.

## Notes
- Several controllers may appear “single-reference” in static grep but are runtime-discovered by annotations/web config; they are not dead code.
