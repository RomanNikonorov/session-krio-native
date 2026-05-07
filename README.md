# Spring Session Redis + Kryo JVM/Native PoC

This project demonstrates sharing one Spring Session Redis namespace between two Java 21 / Spring Boot 3.5 applications:

- `jvm-app`: a regular JVM Spring Boot servlet application.
- `native-app`: the same kind of Spring Boot servlet application, compiled to a GraalVM native executable.

Both applications store HTTP sessions in the same Redis instance. Session values are serialized with a shared Kryo-based `RedisSerializer<Object>` bean named `springSessionDefaultRedisSerializer`, which is the bean name Spring Session Redis uses for custom session serialization.

## What This Experiment Proves

The experiment checks that a Spring Session created by one runtime can be read by the other runtime:

- JVM app writes a Redis-backed session, native executable reads it.
- Native executable writes a Redis-backed session, JVM app reads it.
- The same session cookie and Redis namespace are used in both directions.
- The session payload contains a shared DTO (`SessionUser`), strings, integers, and `Instant`.

The key success signal is that the second app returns the same `sessionId` and session attribute values that the first app wrote.

## Modules

- `session-codec`: shared Kryo serializer, DTOs, fixed Kryo class registry, Spring auto-configuration, and native runtime hints.
- `jvm-app`: Spring Boot app on port `8080`; creates and reads HTTP sessions.
- `native-app`: Spring Boot app on port `8081`; can run on the JVM or as `native-app/target/native-app`.

## Prerequisites

- Docker with Compose.
- Maven.
- Java 21+ for normal JVM runs.
- GraalVM with Native Image for the native build.

This project was verified with:

```bash
graalvm-ce-21.0.2/Contents/Home/bin/native-image --version
```

Expected shape:

```text
native-image 21.0.2 ...
GraalVM Runtime Environment GraalVM CE 21.0.2...
```

## Run The Experiment

Start Redis:

```bash
docker compose up -d redis
```

Verify Redis is listening on `6379`:

```bash
docker ps
```

Run tests:

```bash
mvn test
```

Install the shared codec module into the local Maven repository. This is needed when running only one app module at a time with `-pl`:

```bash
mvn -pl session-codec -am -DskipTests install
```

Build the native executable with GraalVM:

```bash
JAVA_HOME=JavaVirtualMachines/graalvm-ce-21.0.2/Contents/Home \
PATH=JavaVirtualMachines/graalvm-ce-21.0.2/Contents/Home/bin:$PATH \
mvn -pl native-app -Pnative native:compile
```

The build is successful only if Maven reports `BUILD SUCCESS` and GraalVM reports an executable artifact:

```text
Produced artifacts:
 /.../native-app/target/native-app (executable)
```

Start the JVM app in one terminal:

```bash
mvn -pl jvm-app spring-boot:run
```

Start the native executable in another terminal:

```bash
./native-app/target/native-app
```

The native startup log should say `AOT-processed`, which confirms that this is the native executable path, not `spring-boot:run`:

```text
Starting AOT-processed NativeApplication using Java 21.0.2
```

Run the smoke script:

```bash
bash scripts/smoke.sh
```

Expected output has four JSON responses:

1. `jvm-app` creates a session.
2. `native-app` reads the same `sessionId`.
3. `native-app` creates a session.
4. `jvm-app` reads the same `sessionId`.

Example:

```text
{"app":"jvm-app","sessionId":"cb05c80e-da37-4464-9c9b-7ed0861b21c7",...}
{"app":"native-app","sessionId":"cb05c80e-da37-4464-9c9b-7ed0861b21c7",...}
{"app":"native-app","sessionId":"80a0d03a-73e0-4a0b-9a26-942b08339db1",...}
{"app":"jvm-app","sessionId":"80a0d03a-73e0-4a0b-9a26-942b08339db1",...}
```

Matching `sessionId` values prove that both applications are reading and writing the same Redis-backed Spring Session data.

## Manual Smoke Commands

Create a session in the JVM app:

```bash
curl -i -c cookies.txt http://127.0.0.1:8080/session/create
```

Read it from the native executable:

```bash
curl -b cookies.txt http://127.0.0.1:8081/session
```

Create a session in the native executable:

```bash
curl -i -c native-cookies.txt http://127.0.0.1:8081/session/create
```

Read it from the JVM app:

```bash
curl -b native-cookies.txt http://127.0.0.1:8080/session
```

## What Was Done For Native Session Reading

Spring Session Redis stores session data in Redis hashes. The important customization point is a bean named exactly:

```java
springSessionDefaultRedisSerializer
```

The shared module provides this bean in `KryoSessionSerializationConfig`, returning `KryoRedisSerializer`.

Key implementation choices:

- Both apps depend on the same `session-codec` module, so they share the same DTOs and serializers.
- Both apps use the same Redis namespace:

```yaml
spring:
  session:
    redis:
      namespace: spring:session:kryo-native-poc
```

- Both apps use the same cookie name and path:

```yaml
server:
  servlet:
    session:
      cookie:
        name: SHAREDSESSION
        path: /
```

- Kryo runs with `setRegistrationRequired(true)`, so unknown session attribute classes fail immediately instead of silently using unstable fallback serialization.
- `SessionKryoRegistry` is the single source of truth for Kryo registration IDs. These IDs must not change after real session data exists in Redis.
- `SessionUser` has a custom Kryo serializer (`SessionUserKryoSerializer`) instead of relying on reflective field serialization.
- `Instant` and `Duration` have explicit serializers instead of Java serialization.
- Collection and map classes are registered with explicit `CollectionSerializer` and `MapSerializer` instances. This is important for native images: relying on Kryo default serializer discovery caused the native executable to fail because it tried to instantiate serializers reflectively.
- `KryoSessionRuntimeHints` registers reflection hints for the shared serializer and DTO types used by the native image.
- `KryoRedisSerializer` wraps all payloads in a small versioned envelope:

```text
byte 0: format version
byte 1: null marker
remaining bytes: Kryo payload
```

This gives a place to introduce future format migrations without guessing which serializer produced the Redis value.

## Known Constraints

- This PoC supports a deliberately small set of session attribute types. Add new types only through `SessionKryoRegistry` and tests.
- Do not store arbitrary objects in `HttpSession`; native compatibility depends on explicit serializers and stable class registration.
- Changing Kryo registration IDs breaks compatibility with existing Redis session payloads.
- For production evolution, use either a new Redis namespace or a richer versioned payload migration strategy.

## Useful Commands

Stop Redis:

```bash
docker compose down
```

Remove old Redis session data while keeping Redis running:

```bash
docker exec session-krio-native-redis-1 redis-cli FLUSHDB
```

Check which process listens on the app ports:

```bash
lsof -nP -iTCP:8080 -sTCP:LISTEN
lsof -nP -iTCP:8081 -sTCP:LISTEN
```
