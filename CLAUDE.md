# VideoRenderer

Java library for programmatic video rendering. Part of the VDVIL/Komposteur ecosystem.
Extracts frames from source videos, plans compositions, and assembles new videos from segments.

## Architecture

```
+-------------------+     +---------------------+     +------------------+
|   image-core      |     |  video-renderer-core |     |     ffmpeg       |
|  ImageCollector   |<----|  Komposition, Segment|---->| FFmpegFunctions  |
|  (interface)      |     |  Plan, Planner       |     | ImprovedFFMpeg   |
+-------------------+     |  FrameCalculator     |     | YtDlFunctions    |
                          +----------+-----------+     +------------------+
                                     |
                          +----------v-----------+
                          |   video-renderer     |
                          |  Collectors (Xuggle) |
                          |  Builders/Writers    |
                          |  CreateVideoFromScratch|
                          +----------------------+

Flow:  Source Videos --> [Fetch Kompositions] --> KompositionPlanner
       --> [Collectors] --> ImageStore --> [Builders] --> Output Video
```

## Tech Stack

- Java 17, Maven multi-module
- Xuggle 5.4 (legacy video I/O via custom S3 repository)
- FFmpeg via bramp wrapper 0.6.2
- JUnit 5 with `@Tag("IntegrationTest")` for heavy tests
- SLF4J logging

## Module Map

| Module | Purpose |
|--------|---------|
| `video-renderer-core` | Domain model (Segment, Komposition, Plan), frame calculation, planning |
| `video-renderer` | Collectors, builders, writers. Xuggle-based video creation |
| `image-core` | ImageCollector interface (thin abstraction) |
| `ffmpeg` | FFmpeg wrapper (bramp + raw CLI). Snippet splitting, concatenation, probing |
| `deprecated/picscreator` | **DEPRECATED** -- Old JCodec-based MP4 creator. Not in active build, not referenced by any active module. Safe to ignore. Uses JCodec 0.1.6 and Java 11. |

## Key Classes

| Class | Module | File | Purpose |
|-------|--------|------|---------|
| `Komposition` | core | `domain/out/Komposition.java` | BPM + segments + storage + framerate |
| `KompositionPlanner` | core | `collector/KompositionPlanner.java` | Orchestrator: maps build to collect via shortId |
| `FrameCalculator` | core | `collector/FrameCalculator.java` | Interface for frame count calculation |
| `SimpleCalculator` | core | `collector/SimpleCalculator.java` | Ratio-based frame calculator |
| `SuperPlan` | core | `plan/SuperPlan.java` | Plan with frame representations |
| `Segment` | core | `domain/Segment.java` | Interface: time-addressed video piece |
| `VideoStillImageSegment` | core | `domain/VideoStillImageSegment.java` | Beat-addressed segment with filters |
| `CreateVideoFromScratchImages` | renderer | `video/CreateVideoFromScratchImages.java` | Video writer loop |
| `VideoThumbnailsCollector` | renderer | `video/VideoThumbnailsCollector.java` | Frame extraction from source |
| `PipeDream` | renderer | `video/creator/PipeDream.java` | In-memory ImageStore |
| `ImprovedFFMpegFunctions` | ffmpeg | `ffmpeg/ImprovedFFMpegFunctions.java` | Type-safe FFmpeg wrapper |
| `FFmpegFunctions` | ffmpeg | `snippets/FFmpegFunctions.java` | Raw CLI FFmpeg execution |

## Key Concepts

- **Komposition**: A composition plan with BPM, segments, and storage location
- **Segment**: Time-addressed piece of video (start + duration in beats or timestamps)
- **Plan/SuperPlan**: Execution plan mapping timestamps to frame representations
- **KompositionPlanner**: Orchestrates collect-plans (fetch frames) and build-plan (assemble output)
- **FrameCalculator**: Handles frame count differences between source and target

## Build and Test

```bash
mvn clean compile -q                 # Quick compile check (fastest feedback)
mvn clean install -P ex-integration  # Build + unit tests only (no FFmpeg needed)
mvn clean install                    # Full build with all tests

# Run a specific test
mvn test -pl video-renderer -Dtest=ListFilterLogicTest

# Run a specific module's tests
mvn test -pl video-renderer-core -P ex-integration
```

Integration tests download large video files from S3 and require:
- FFmpeg installed locally (`ffmpeg`/`ffprobe` on PATH or set via env vars)
- Network access to `kompost.s3.amazonaws.com`

## Model Delegation

| Model | Use For |
|-------|---------|
| Haiku | File reads, searches, status checks, simple edits |
| Sonnet | Multi-file refactors, test fixes, module-level work |
| Opus | Architecture decisions, cross-module changes, planning |

## Known Gotchas

- `deprecated/picscreator` is NOT in the root POM modules list -- it does not build and is not referenced by any active code. It uses JCodec 0.1.6 (ancient) and Java 11. Safe to delete when ready.
- The `ex-integration` profile has a typo: property is `test.exlusion` not `test.exclusion`
- Xuggle 5.4 is unmaintained; hosted only on custom S3 Maven repo -- if S3 is down, build breaks
- Build/collect segments are linked by `shortId()` not full `id()` -- mismatch causes silent failures
- `Thread.sleep()` used as synchronization in `CreateVideoFromScratchImages` -- fragile

## Known Issues (remaining)

- 14 disabled tests: most need pre-downloaded video files or have race conditions
- `SuperSegment.compareTo()` uses deprecated `new Long()` boxing
- `CommonFunctions.md5Checksum()` has unprofessional error message
- Commented-out `verifyNonOverlappingSegments` call in KompositionPlanner
- Multiple `System.out.println` calls should use logger
