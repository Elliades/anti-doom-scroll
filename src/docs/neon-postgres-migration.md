# Neon PostgreSQL migration

## Summary

- **Current**: App uses PostgreSQL (localhost or env) + Flyway; schema is already PostgreSQL-compatible.
- **Target**: Neon serverless Postgres using the **pooler** endpoint (recommended for serverful apps).

## 1. Fix applied in repo

- **Duplicate Flyway versions (resolved)**: `V11__nback_cards_sequence.sql` duplicated the same SQL as `V23__nback_cards_sequence.sql`; the `V11__` copy was removed so only `V23__` remains. `V23__digit_span_subject.sql` collided with that `V23__` file, so digit span was moved to `V27__digit_span_subject.sql`.
- **Neon profile**: `application-neon.yml` configures the datasource from environment variables (no credentials in repo).

## 2. Configure environment

Set these before running or deploying (example for your Neon DB; **do not commit the password**):

```bash
# JDBC URL: use the pooler host, database name, and sslmode=require
export SPRING_DATASOURCE_URL="jdbc:postgresql://ep-old-heart-abyu622g-pooler.eu-west-2.aws.neon.tech:5432/neondb?sslmode=require"
export SPRING_DATASOURCE_USERNAME="neondb_owner"
export SPRING_DATASOURCE_PASSWORD="<your-password-from-neon-dashboard>"
```

On Windows PowerShell:

```powershell
$env:SPRING_DATASOURCE_URL = "jdbc:postgresql://ep-old-heart-abyu622g-pooler.eu-west-2.aws.neon.tech:5432/neondb?sslmode=require"
$env:SPRING_DATASOURCE_USERNAME = "neondb_owner"
$env:SPRING_DATASOURCE_PASSWORD = "<your-password>"
```

## 3. Run migration (Flyway on startup)

With the env vars set, start the app with the `neon` profile. Flyway will run all versioned migrations on first connect:

```bash
./gradlew bootRun --args='--spring.profiles.active=neon'
```

Or build and run the JAR:

```bash
./gradlew bootJar
java -jar build/libs/anti-doom-scroll-0.0.1-SNAPSHOT.jar --spring.profiles.active=neon
```

## 4. Verify

1. **Startup**: Check logs for `Flyway` and `Successfully applied X migrations` (or no new migrations if already applied).
2. **Health**: `curl http://localhost:5173/api/health` (or your deployed URL) — should return 200.
3. **API**: e.g. `curl http://localhost:5173/api/subjects` to confirm DB reads.

## 5. Optional: Run Flyway only (no app)

To migrate the DB without starting the full app (e.g. from CI or a one-off step), use the Flyway CLI or a Gradle task pointing at the same `SPRING_DATASOURCE_*` env vars and `db/migration` location. The app’s Flyway run on startup is usually enough.

## 6. Notes

- **Pooler**: The URL uses Neon’s **pooler** endpoint (`-pooler` in hostname). Use this for the app; direct endpoint is for migrations/single connections if needed.
- **Hikari**: `application-neon.yml` keeps a small pool (5) and sets connection timeout and keepalive suitable for serverless.
- **Tests**: Unit/integration tests continue to use H2 via `application-test.yml`; no Neon needed for tests.
