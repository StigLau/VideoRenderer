# Skill: Build System

## When to Activate
- Working with Maven builds, test execution, CI/CD, or releases
- Adding or modifying module dependencies
- Troubleshooting compilation or test failures
- Upgrading dependency versions (especially Xuggle)

## Quick Start

```bash
# Quick compile check (fastest feedback loop)
mvn clean compile -q

# Full build with all tests (needs FFmpeg + network)
mvn clean install

# Skip integration tests (no FFmpeg/network needed)
mvn clean install -P ex-integration

# Run a single test class
mvn test -pl video-renderer -Dtest=ListFilterLogicTest

# Run only unit tests in one module
mvn test -pl video-renderer-core -P ex-integration
```

## Module Dependencies

```
image-core (no deps)
  <- video-renderer-core (domain model, planning)
    <- ffmpeg (+ bramp ffmpeg 0.6.2)
    <- video-renderer (+ xuggle 5.4)
```

## Key Files

| File | Purpose |
|------|---------|
| `pom.xml` | Root POM, module list, shared deps, profiles |
| `video-renderer-core/pom.xml` | Core domain module |
| `ffmpeg/pom.xml` | FFmpeg wrapper module |
| `video-renderer/pom.xml` | Xuggle-based video creation module |

## Custom Repository

Xuggle 5.4 hosted at `https://kompost.s3.amazonaws.com/maven/`.
This is an unmaintained library; the S3 repo is the only source.

## Integration Tests

Tagged `@Tag("IntegrationTest")`. Excluded with `-P ex-integration`.
These tests require:
- FFmpeg installed locally (`ffmpeg` and `ffprobe` on PATH or env vars set)
- Network access to download video files from S3

## Key Gotchas

- The `ex-integration` profile name has a typo in the POM property: `test.exlusion` (not `exclusion`)
- `deprecated/picscreator` is NOT listed as a module in root POM -- it will not build
- Xuggle 5.4 JARs are only available from the custom S3 repo; if S3 is down, the build breaks
- Java 17 is required (`maven.compiler.release=17`)
