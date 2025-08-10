# VideoRenderer - Claude Code Integration Guide

## Project Overview

VideoRenderer is a **specialized FFMPEG integration library** that provides **frame-accurate video processing capabilities** for the Komposteur music video platform. As a foundational Java library with multi-module architecture, it handles the complex video manipulation tasks that enable beat-synchronized music video creation.

### Core Domain: FFMPEG Video Processing ✅
- **Frame-Accurate Video Manipulation**: Precise timing for music synchronization
- **Multi-Format Video Processing**: MP4, AVI, MKV, WebM support via FFMPEG
- **Video Segment Operations**: Cutting, concatenation, crossfading, normalization
- **YouTube Content Processing**: Integration with yt-dlp for content acquisition
- **Master Format Normalization**: Ensures consistent 30fps CFR output for predictable timing
- **Audio/Video Synchronization**: Separate audio and video track processing

### Relationship to Komposteur
- **Role**: Core dependency providing FFMPEG integration and video processing primitives
- **Consumer**: Komposteur application uses VideoRenderer for all video manipulation tasks
- **Architecture**: VideoRenderer handles low-level FFMPEG operations, Komposteur orchestrates high-level music video creation

## Project Architecture

### Current Status
- **Version**: 1.2.0-SNAPSHOT (semantic versioning)
- **Main Branch**: `master`
- **Java Version**: 17
- **Build System**: Maven with multi-module structure

### Module Structure
```
video-renderer-base (parent POM)
├── ffmpeg/ - Core FFMPEG wrapper functions
├── image-core/ - Image processing utilities  
├── video-renderer-core/ - Domain models and core logic
└── video-renderer/ - Main implementation and integrations
```

### Key Components
- **`ImprovedFFMpegFunctions.java`**: Enhanced FFMPEG operations with frame accuracy
- **`FFmpegFunctions.java`**: Core video processing primitives
- **Domain Models**: Segments, plans, and video composition abstractions

## Technical Capabilities

### Frame-Accurate Processing (Recently Added)
- **Master Format Normalization**: Converts all sources to consistent 30fps CFR
- **Frame-Level Seeking**: Uses exact frame calculations instead of time-based seeking
- **Crossfade Transitions**: Smooth transitions between video segments to mask timing issues
- **Beat-to-Frame Conversion**: Converts musical beat timing to precise frame positions

### Video Processing Operations
- **Segment Extraction**: Frame-accurate video snippet creation
- **Concatenation**: Seamless video joining with timing precision
- **Format Conversion**: Multi-format support with quality preservation
- **Audio Extraction**: Separate audio track processing for music video workflows

## Build and Release Configuration

### Maven Configuration
- **Distribution**: GitHub Packages (`https://maven.pkg.github.com/StigLau/VideoRenderer`)
- **Profile**: `ex-integration` for extended integration tests
- **Dependencies**: FFMPEG binary, Xuggler (legacy), JUnit Jupiter

### Release Strategy
- **Process**: Manual GitHub Actions workflow with semantic versioning
- **Artifacts**: JAR files, sources, deployed to GitHub Packages
- **Documentation**: Comprehensive release process in `docs/release/RELEASE_PROCESS.md`
- **Workflow**: `release.yml` for proper releases, `pr_deployment.yml` disabled to prevent version conflicts

## Git Workflow Preferences

- **Current Branch**: `feature/frame-accurate-video-processing`
- **Main Branch**: `master` (for PRs)
- **Recent Work**: Added frame-accurate video processing capabilities for better music synchronization
- **Release Strategy**: Feature branches → PR → manual release workflow
- **⚠️ PUSH POLICY**: Create feature branches with upstream tracking as needed

## Domain-Specific Considerations

### FFMPEG Integration Expertise
- **Binary Dependency**: Requires FFMPEG binary installation
- **Command Generation**: Dynamic FFMPEG command building for complex operations  
- **Error Handling**: Robust process management for FFMPEG operations
- **Performance**: Optimized for batch processing and concurrent operations

### Music Video Specific Features
- **Beat Synchronization**: Frame-accurate timing calculations for music alignment
- **Crossfade Support**: Visual transitions that complement musical transitions
- **Format Standardization**: Consistent output format for predictable behavior in music video assembly
- **Audio Separation**: Independent audio and video track processing

### Integration Patterns
- **Komposteur Integration**: Provides video processing primitives for higher-level music video operations
- **S3 Compatibility**: Works with Komposteur's S3 caching architecture for cloud-based processing
- **Parallel Processing**: Supports concurrent video operations for scalability

## Testing and Quality

### Test Coverage
- **Integration Tests**: FFMPEG operations with real video files
- **Unit Tests**: Core logic and domain model validation
- **Test Resources**: Sample video files and expected outputs
- **Profile**: `ex-integration` for comprehensive testing including FFMPEG operations

### Code Quality
- **Style**: Follows existing Java conventions, minimal commenting unless complex logic
- **Dependencies**: Conservative approach - verify library availability before use
- **Security**: No credentials in code, secure file handling practices

## Developer Workflow Preferences

### Stig's Development Context
- **Experience Level**: Senior developer with deep Java, FFMPEG, and video processing expertise
- **Communication Style**: Minimal responses, direct implementation, assumes technical knowledge
- **Architecture Preference**: Clean abstractions, proper separation of concerns
- **Quality Focus**: Production-ready code, proper error handling, performance considerations

### Development Approach
- **Implementation First**: YOLO commands welcome for direct changes
- **Minimal Documentation**: Code should be self-explanatory, documentation only when necessary
- **Performance Conscious**: Optimize for real-world video processing workloads
- **Integration Aware**: Always consider impact on Komposteur's video processing pipeline

## Common Operations

### Build Commands
```bash
mvn clean package -P ex-integration          # Build with integration tests
mvn clean verify -P ex-integration -DskipITs=false  # Full verification
mvn deploy -P ex-integration -DskipTests=true       # Deploy to GitHub Packages
```

### Release Process
- GitHub Actions → Release workflow → Enter semantic version → Automated release
- See `docs/release/RELEASE_PROCESS.md` for complete process

### FFMPEG Operations
- All video processing goes through FFMPEG wrapper functions
- Frame-accurate operations use new normalization and seeking capabilities
- Audio/video separation for music video workflows

---

**Domain Focus**: VideoRenderer is the video processing engine that enables Komposteur's music video creation. Every enhancement should consider frame accuracy, performance at scale, and integration with music-driven workflows.