# Skill: Frame Calculation

## When to Activate
- Working with frame extraction, frame counts, or timestamp-to-frame mapping
- Debugging BPM-based timing or frame count mismatches
- Modifying how segments map to frames
- Working with FrameCalculator, SimpleCalculator, or any FramePlan implementation

## Quick Start

```java
// Calculate number of frames for a segment
FrameCalculator calc = new SimpleCalculator();
long frameCount = calc.calculateNumberOfFrames(segment, bpm, framerate);
// Formula: duration * bpm * framerate / (60 * 1000 * 1000) * buildRatio / collectRatio

// Create a plan that produces frame representations
FramePlan plan = new KnownNumberOfFramesPlan(segment, frameCount);
List<FrameRepresentation> frames = plan.calculateFramesFromSegment();

// BPM-to-microsecond conversion
long microseconds = Segment.calc(timeInBeats, bpm);
// = timeInBeats * 60 * 1000 * 1000 / bpm
```

## Key Classes

| Class | File | Purpose |
|-------|------|---------|
| FrameCalculator | video-renderer-core/.../collector/FrameCalculator.java | Interface for frame count calculation |
| SimpleCalculator | video-renderer-core/.../collector/SimpleCalculator.java | Ratio-based implementation |
| FrameRepresentation | video-renderer-core/.../collector/FrameRepresentation.java | Single frame: timestamp + refId + used flag |
| FramePlan | video-renderer-core/.../collector/plan/FramePlan.java | Interface: segment -> list of FrameRepresentations |
| KnownNumberOfFramesPlan | video-renderer-core/.../collector/plan/KnownNumberOfFramesPlan.java | Plan for segments with known frame count |
| TimeStampFixedImageSamplePlan | video-renderer-core/.../collector/plan/TimeStampFixedImageSamplePlan.java | Plan for microsecond-addressed segments |
| StaticImagesFramePlan | video-renderer-core/.../collector/plan/StaticImagesFramePlan.java | Plan for pre-extracted static images |

## Frame Math

- BPM-based timing: Segment.calc(time, bpm) = time * 60 * 1000 * 1000 / bpm (microseconds)
- Segments define start/duration in beats; startCalculated(bpm) converts to microseconds
- TimeStampFixedImageSampleSegment bypasses BPM math -- uses raw microsecond timestamps
- Frame count formula: duration * bpm * framerate / (60M) * buildRatio / collectRatio

## Key Gotchas

- Frame counts depend on BOTH BPM and framerate -- changing either affects output
- framesPerBeat in TimeStampFixedImageSampleSegment is metadata only, not used in calculation
- Off-by-one risk: lastTimeStamp uses start + duration -- verify boundary conditions
- Build ratio vs collect ratio: collect determines extraction density, build determines output density
- FrameRepresentation.used flag tracks whether a frame has been consumed during building
