# Skill: FFmpeg Integration

## When to Activate
Working with FFmpeg commands, video probing, snippet splitting, concatenation, or format conversion.

## Two Implementations
1. **ImprovedFFMpegFunctions** - bramp FFmpeg Java wrapper (type-safe builder API)
2. **FFmpegFunctions** - raw CLI execution via `Runtime.exec()` (more flexible)

## Configuration
- `ffmpegLocation()`: env var `ffmpeg` or default `/usr/bin/ffmpeg`
- `ffprobeLocation()`: env var `ffprobe` or default `/usr/bin/ffprobe`

## Known Issues
- `humanReadablePeriod()` has a math bug in millis/seconds extraction
- `detectSceneChanges*` methods are experimental/hardcoded
- `ImprovedFFMpegFunctions.combineAudioAndVideo` is non-functional (@Deprecated)
