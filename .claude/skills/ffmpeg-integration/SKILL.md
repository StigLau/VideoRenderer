# Skill: FFmpeg Integration

## When to Activate
- Working with FFmpeg commands, video probing, snippet splitting, or concatenation
- Debugging video format conversion issues
- Modifying how video segments are extracted or combined
- Working with `ImprovedFFMpegFunctions` or `FFmpegFunctions`

## Quick Start

```java
// Split a snippet from a video using bramp wrapper (preferred)
ImprovedFFMpegFunctions.snippetSplitter(sourcePath, startMillis, endMillis, destPath);

// Split using raw CLI execution (legacy)
FFmpegFunctions.snippetSplitter(sourcePath, startMillis, endMillis, destPath);

// Concatenate video snippets
Path result = FFmpegFunctions.concatVideoSnippets(ExtensionType.mp4, snippet1, snippet2);

// Probe video info
FFmpegFormatResult info = ImprovedFFMpegFunctions.ffmpegFormatInfo(videoPath);
// info.duration, info.format.filename, etc.

// Count frames in a video
long frames = FFmpegFunctions.countNumberOfFrames(videoPath);
```

## Two Implementations

| Class | Approach | When to use |
|-------|----------|-------------|
| `ImprovedFFMpegFunctions` | bramp FFmpeg Java wrapper (type-safe builder) | New code, snippet splitting |
| `FFmpegFunctions` | Raw `Runtime.exec()` CLI execution | Concatenation, frame counting, probing |

## Key Files

| File | Purpose |
|------|---------|
| `ffmpeg/src/main/java/no/lau/ffmpeg/ImprovedFFMpegFunctions.java` | bramp wrapper, format probing, scene detection |
| `ffmpeg/src/main/java/no/lau/vdvil/snippets/FFmpegFunctions.java` | Raw CLI, concat, frame counting, `humanReadablePeriod` |
| `ffmpeg/src/main/java/no/lau/vdvil/snippets/KompositionUtils.java` | Temp file creation helpers |
| `ffmpeg/src/main/java/no/lau/vdvil/snippets/YtDlFunctions.java` | YouTube download via yt-dlp |

## Configuration

- `ffmpegLocation()`: env var `ffmpeg` or default `/usr/bin/ffmpeg`
- `ffprobeLocation()`: env var `ffprobe` or default `/usr/bin/ffprobe`
- On macOS with Homebrew, set env vars or symlink to `/usr/bin/`

## Key Gotchas

- `humanReadablePeriod()` outputs `H:MM:SS.mmm` format used for FFmpeg `-ss` and `-t` args
- `detectSceneChanges*` methods are experimental with hardcoded thresholds
- `ImprovedFFMpegFunctions.combineAudioAndVideo` is `@Deprecated` and non-functional
- `FFmpegFunctions.performFFMPEG` uses `System.out.print` directly -- should use logger
- Raw CLI approach builds command strings via concatenation (safe in practice since inputs are file paths)
