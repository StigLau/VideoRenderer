# VideoRenderer

Java library for programmatic video rendering. Part of the VDVIL/Komposteur ecosystem.
Extracts frames from source videos, plans compositions, and assembles new videos from segments.

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
| `deprecated/picscreator` | Old JCodec-based MP4 creator. Not in active build |

## Key Concepts

- **Komposition**: A composition plan with BPM, segments, and storage location
- **Segment**: Time-addressed piece of video (start + duration in beats or timestamps)
- **Plan/SuperPlan**: Execution plan mapping timestamps to frame representations
- **KompositionPlanner**: Orchestrates collect-plans (fetch frames) and build-plan (assemble output)
- **FrameCalculator**: Handles frame count differences between source and target

## Build

```bash
mvn clean install                    # Full build with all tests
mvn clean install -P ex-integration  # Skip integration tests (needs video files)
mvn clean compile -q                 # Quick compile check
```

Integration tests download large video files from S3 and require FFmpeg installed locally.

## Model Delegation

| Model | Use For |
|-------|---------|
| Haiku | File reads, searches, status checks, simple edits |
| Sonnet | Multi-file refactors, test fixes, module-level work |
| Opus | Architecture decisions, cross-module changes, planning |

## Known Issues

- 14 disabled tests: most need pre-downloaded video files or have race conditions
- Xuggle 5.4 is unmaintained; hosted on custom S3 Maven repo
- `deprecated/picscreator` module not in active build but still in repo
- `VideoStillImageSegment.revert()` sets `reverted = false` (bug, should be `true`)
- `humanReadablePeriod()` in FFmpegFunctions has incorrect time math
- `SuperSegment.compareTo()` uses deprecated `new Long()` boxing
- `CommonFunctions.md5Checksum()` has unprofessional error message
- Commented-out `verifyNonOverlappingSegments` call in KompositionPlanner
- Multiple `System.out.println` calls should use logger
- `Thread.sleep()` used as synchronization mechanism in CreateVideoFromScratchImages
