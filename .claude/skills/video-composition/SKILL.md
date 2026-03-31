# Skill: Video Composition

## When to Activate
- Working with Komposition planning, segment assembly, or the collect/build pipeline
- Debugging why frames are missing, duplicated, or out of order
- Modifying video output creation or the KompositionPlanner orchestration
- Working with segment types, filters, or cross-fades

## Quick Start

```java
// 1. Define fetch kompositions (source videos with segments to extract)
Komposition fetchKomposition = new Komposition(128,  // BPM
    new TimeStampFixedImageSampleSegment("seg1", startMicros, durationMicros, framesPerBeat));

// 2. Define build komposition (output layout referencing fetch segments by shortId)
Komposition buildKomposition = new Komposition(128,
    new VideoStillImageSegment("seg1", startBeat, durationBeats, collectRatio));

// 3. Plan and execute
KompositionPlanner planner = new KompositionPlanner(
    List.of(fetchKomposition), buildKomposition, framerate);
// planner.collectPlans -- frames to extract from sources
// planner.buildPlan -- frames to write to output

// 4. Collect frames into store, then build output
ImageStore store = new PipeDream();
collector.collect(sourceVideo, fetchKomposition, store);
CreateVideoFromScratchImages.createVideo(buildKomposition, store, outputPath);
```

## Architecture

```
Source Videos                    Output Video
     |                               ^
     v                               |
[Fetch Kompositions]           [Build Komposition]
     |                               ^
     v                               |
[KompositionPlanner] -----> [collectPlans] + [buildPlan]
     |                               ^
     v                               |
[Collectors] --> [ImageStore] --> [Builders/Writers]
  (extract)      (PipeDream)     (Xuggle IMediaWriter)
```

## Key Classes

| Class | File | Purpose |
|-------|------|---------|
| Komposition | video-renderer-core/.../domain/out/Komposition.java | BPM + segments + storage + framerate |
| KompositionPlanner | video-renderer-core/.../collector/KompositionPlanner.java | Orchestrator: maps build to collect via shortId |
| SuperPlan | video-renderer-core/.../plan/SuperPlan.java | Plan with frame representations and meta-plan lookup |
| CreateVideoFromScratchImages | video-renderer/.../video/CreateVideoFromScratchImages.java | Video writer loop (video + audio streams) |
| PipeDream | video-renderer/.../video/creator/PipeDream.java | In-memory ImageStore implementation |
| VideoImageStitcher | video-renderer/.../video/creator/VideoImageStitcher.java | Assembles frames into video |
| VideoThumbnailsCollector | video-renderer/.../video/VideoThumbnailsCollector.java | Extracts frames from source video |

## Segment Types

| Type | Addressing | Use case |
|------|-----------|----------|
| VideoStillImageSegment | Beat-based | Build kompositions, supports filters |
| TimeStampFixedImageSampleSegment | Microsecond-based | Fetch kompositions |
| TransitionSegment | Beat-based | Cross-fades between segments |
| StaticImagesSegment | Pre-extracted | Static image sequences |
| KnownNumberOfFramesSegment | Frame count | When exact frame count is known |

## Key Gotchas

- Build/collect segments linked by shortId() NOT full id() -- mismatch causes silent failures
- verifyNonOverlappingSegments is commented out in KompositionPlanner -- overlapping segments can cause frame conflicts
- Thread.sleep used for synchronization in CreateVideoFromScratchImages -- fragile under load
- VideoStillImageSegment.revert() had a bug (set reverted = false) -- fixed in recent commit
- Xuggle IMediaWriter must receive frames in strictly increasing timestamp order
