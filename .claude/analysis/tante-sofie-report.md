# Tante Sofie Adversarial Analysis - VideoRenderer

Date: 2026-03-30

## 1. Disabled Tests (13 @Disabled annotations across 7 files)

### Can be re-enabled with minor fixes:
None trivially re-enableable - all depend on external state (downloaded files, specific tmp paths).

### Categorized:

| Test | File | Reason | Verdict |
|------|------|--------|---------|
| `testCapturing` | ImageCapturingFromVideoTest | Needs specific input file at /tmp path | Needs test data setup |
| `videoAudioCombinationTest` | CombineAudioAndVideoTest | "Download first" - needs local files | Integration test, tag missing |
| `testExtractingAudion` | AudioExtractionTest | "Download first" - needs local files | Integration test, tag missing |
| `testConverting` | MediaConvertorTest | "Download first" - needs local files | Integration test, tag missing |
| `testCountNumberOfFrames` | BuildVideoFromScratchImagesTest | Needs specific /tmp files from prior runs | Chain dependency, not standalone |
| `testConcatStuff` | BuildVideoFromScratchImagesTest | Needs specific /tmp files from prior runs | Chain dependency, not standalone |
| `specificVideoCompositionTest` | BuildVideoFromScratchImagesTest | "Not working yet" | Genuine broken test |
| `testStreamingFromInVideoSource` | StreamingImageStoreTest | "Test often fails!!" - race condition | Flaky, needs synchronization fix |
| `testSegmentStrip` | StreamingImageStoreTest | "Segment strip doesnt stop" | Genuine bug - infinite loop |
| `restructureMediaTest` (x2) | RestructureMediaTest | "Download first" - deprecated module | Leave disabled, deprecated |

### Actionable items:
- CombineAudioAndVideoTest, AudioExtractionTest, MediaConvertorTest: Add `@Tag("IntegrationTest")` so they run in integration profile but are excluded in CI
- The "Download first" tests could use TestData.fetch() pattern to auto-download

## 2. Bugs Found

### BUG: `VideoStillImageSegment.revert()` sets `reverted = false`
File: `video-renderer-core/.../domain/VideoStillImageSegment.java:24`
The method is supposed to mark the segment as reverted but sets `reverted = false` instead of `true`.
**Severity: High** - this silently breaks all segment reversal logic.

### BUG: `humanReadablePeriod()` incorrect math
File: `ffmpeg/.../snippets/FFmpegFunctions.java:82-91`
The method divides totalMillis by 1000, then takes `rest % 1000` for millis and divides by 1000 again.
The input is already in milliseconds but the first line divides by 1000, losing precision.
Also the seconds/minutes extraction logic has errors in the intermediate calculations.
**Severity: Medium** - produces wrong timestamps for FFmpeg commands.

### BUG: `SuperSegment.compareTo()` uses deprecated `new Long()`
File: `video-renderer-core/.../domain/SuperSegment.java:48`
Uses `new Long(this.start())` which is deprecated since Java 9 and will be removed.
**Severity: Low** - works but generates warnings.

### BUG: `LocalMediaFile.getChecksums()` dead logic
File: `video-renderer-core/.../domain/LocalMediaFile.java:58-71`
The outer `if` checks `checksums == null || checksums.isEmpty()`, then inside, the code checks
`if (checksums != null && !checksums.isEmpty())` which can never be true (contradicts outer condition).
**Severity: Low** - the fallback `else` branch works correctly by accident.

### BUG: `ImprovedFFMpegFunctions.concatVideoSnippets()` silent failure
File: `ffmpeg/.../ImprovedFFMpegFunctions.java:152-153`
When `snippets.length < 2`, it logs an error but continues execution instead of throwing.
**Severity: Medium** - will produce corrupted output silently.

## 3. Dead Code

- `deprecated/picscreator` entire module - not in build, uses ancient JCodec
- `video-renderer/.../deprecated/phun/` package - 4 classes (CreateBallVideo, ForNoTull, ImageCreator, ImageCreatorI) - toy/experiment code in main source tree
- `ImprovedFFMpegFunctions.detectSceneChanges()` - returns hardcoded "some results"
- `ImprovedFFMpegFunctions.detectSceneChangesProbe()` - hardcoded file paths, 6 unused String variables
- `ScreenRecordingExample`, `TranscodingExample` - example files in main source
- Multiple commented-out code blocks throughout test files

## 4. Dependency Health

| Dependency | Version | Status |
|------------|---------|--------|
| Xuggle | 5.4 | **Critical**: Unmaintained since ~2012. Hosted on custom S3 repo. No Java 17+ updates. |
| bramp ffmpeg | 0.6.2 | **Stale**: Last release 2019. Works but no maintenance. |
| JUnit 5 | 5.10.0 | OK (current: 5.11.x) |
| SLF4J | 1.7.36 | **Stale**: SLF4J 2.x is current, 1.7.x is legacy |
| JCodec (deprecated) | ancient | Not in active build, can ignore |

**Xuggle is the biggest risk.** It's the only video I/O library and is completely dead upstream.
Long-term path: migrate to JavaCV (FFmpeg JNI bindings) or pure FFmpeg CLI.

## 5. Code Quality Issues

- `CommonFunctions.md5Checksum()` error message: "Shit didn't go all that well" - unprofessional
- Mixed use of `System.out.println` and logger throughout codebase
- `Thread.sleep(5000)` and `Thread.sleep(2000)` used as synchronization in CreateVideoFromScratchImages
- Raw types: `Segment implements Comparable` without generic parameter
- `VideoStillImageSegment` uses raw type `List` in `applyModifications` return
- Property typo in pom.xml: `test.exlusion` (should be `test.exclusion`)
- `fetchEnvsOrEx` joins keys twice: `String.join(",", kz)` where `kz` is already joined
- `ImprovedFFMpegFunctions.ffmpegFormatInfo()` and `ffmpegStreamInfo()` use `System.out.format` instead of logger
- `VideoInfoTest.findOutShit()` - unprofessional test name

## 6. Missing Error Handling

- `FFmpegFunctions.performFFMPEG()` doesn't check process exit code
- `ImprovedFFMpegFunctions.concatVideoSnippets()` logs error but doesn't throw on insufficient input
- `UrlHandler.urlCreator()` catches all exceptions and wraps generically, losing stack trace context
- No timeout on FFmpeg process execution - could hang forever
- `detectSceneChangesProbe()` calls `waitFor()` without timeout
