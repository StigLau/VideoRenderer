# Skill: Frame Calculation

## When to Activate
Working with frame extraction, frame counts, timestamp-to-frame mapping, or BPM-based timing.

## Key Classes
- `FrameCalculator` (interface) - calculates frame counts from segment/bpm/framerate
- `SimpleCalculator` - ratio-based: `duration * bpm * framerate / (60M) * buildRatio / collectRatio`
- `FrameRepresentation` - a single frame with timestamp, reference ID, and used-flag
- `FramePlan` (interface) - produces list of FrameRepresentations from a segment

## Frame Math
- BPM-based timing: `calc(time, bpm) = time * 60 * 1000 * 1000 / bpm` (microseconds)
- Segments define start/duration in beats; `startCalculated(bpm)` converts to microseconds
- `TimeStampFixedImageSampleSegment` bypasses BPM math - uses raw microsecond timestamps

## Pitfalls
- Frame counts depend on both BPM and framerate
- `framesPerBeat` in TimeStampFixedImageSampleSegment is metadata, not directly used in calculation
- Off-by-one: `lastTimeStamp` uses `start + duration`, verify boundary conditions
