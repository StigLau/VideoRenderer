# Skill: Video Composition

## When to Activate
Working with Komposition planning, segment assembly, video building, or the collect/build pipeline.

## Architecture
1. **Define fetch kompositions** - source videos with timestamp-addressed segments
2. **Define build komposition** - output layout referencing fetch segments by shortId
3. **KompositionPlanner** orchestrates: creates collect-plans and build-plan
4. **Collectors** extract frames from source video into ImageStore (PipeDream)
5. **Builders** assemble frames into output video via Xuggle IMediaWriter

## Key Classes
- `Komposition` - BPM + segments + storage location + framerate
- `KompositionPlanner` - the orchestrator, maps build to collect segments via shortId
- `SuperPlan` - Plan impl with frame representations and meta-plan lookup
- `CreateVideoFromScratchImages` - the video writer loop (video + audio streams)

## Segment Types
- `VideoStillImageSegment` - beat-addressed, supports filters
- `TimeStampFixedImageSampleSegment` - microsecond-addressed, for fetch kompositions
- `TransitionSegment` - cross-fades between segments
- `StaticImagesSegment` - pre-extracted static images

## Pitfalls
- Build/collect segments linked by `shortId()` not full `id()`
- `verifyNonOverlappingSegments` is commented out in KompositionPlanner
- Thread.sleep used for synchronization in CreateVideoFromScratchImages
