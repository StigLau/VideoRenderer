# Skill: Build System

## When to Activate
Working with Maven builds, test execution, CI/CD, or releases.

## Build Commands
```bash
mvn clean install                    # Full build + tests
mvn clean install -P ex-integration  # Skip integration tests
mvn clean compile -q                 # Quick compile check
```

## Module Dependencies
```
image-core (no deps)
  <- video-renderer-core
    <- ffmpeg (+ bramp ffmpeg 0.6.2)
    <- video-renderer (+ xuggle 5.4)
```

## Custom Repository
Xuggle 5.4 hosted at `https://kompost.s3.amazonaws.com/maven/`.

## Integration Tests
Tagged `@Tag("IntegrationTest")`. Require FFmpeg installed and network access.
