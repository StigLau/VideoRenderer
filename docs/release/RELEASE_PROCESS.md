# VideoRenderer Release Process

## Overview

VideoRenderer supports both automatic and manual release workflows for semantic version releases. This ensures proper version control, comprehensive testing, and consistent release artifacts.

## Release Strategy

- **Versioning**: Semantic versioning (MAJOR.MINOR.PATCH)
- **Distribution**: GitHub Packages Maven Registry
- **Automation**: Both automatic (PR merge) and manual triggers
- **Artifacts**: JAR files, sources, GitHub releases

## Prerequisites

- Write access to the repository
- All tests passing on main branch
- Code changes merged and ready for release

## Release Process Options

### Option 1: Automatic Release (Recommended for regular development)

Automatic releases trigger when PRs are merged to `master`:

1. **Create and merge PR**: Standard development workflow
2. **Automatic trigger**: `auto_release.yml` workflow runs automatically
3. **Version calculation**: Auto-increments PATCH version (1.2.0 → 1.2.1)
4. **Standard process**: Same validation, build, test, and deployment steps as manual

**Benefits**:
- Zero-friction releases for continuous delivery
- Consistent versioning without manual input
- Immediate availability of changes

### Option 2: Manual Release (For major/minor versions or specific control)

#### 1. Trigger Release Workflow

1. Navigate to **GitHub Actions** → **Release** workflow
2. Click **"Run workflow"** 
3. Fill in required parameters:
   - **Release version**: e.g., `1.2.0` (semantic version format)
   - **Next SNAPSHOT version**: e.g., `1.2.1-SNAPSHOT`

#### 2. Automated Release Steps (Both Workflows)

The workflow automatically performs:

1. **Validation**
   - Validates version format (X.Y.Z pattern)
   - Checks if release tag already exists
   - Ensures clean working state

2. **Version Updates**
   - Updates POM version to release version
   - Verifies version was set correctly

3. **Build & Test** 
   - Runs `mvn clean verify -P ex-integration -DskipITs=false`
   - Ensures all tests pass before release

4. **Release Creation**
   - Commits release version changes
   - Creates git tag `v{version}`
   - Deploys artifacts to GitHub Packages
   - Creates GitHub Release with auto-generated notes

5. **Post-Release**
   - Updates POM to next SNAPSHOT version
   - Commits snapshot version
   - Pushes all changes and tags to repository

### Release Artifacts (Both Workflows)

Each release produces:

- **Maven Artifacts**: Available in GitHub Packages
  ```xml
  <dependency>
      <groupId>no.lau.vdvil</groupId>
      <artifactId>video-renderer</artifactId>
      <version>1.2.0</version>
  </dependency>
  ```

- **GitHub Release**: Tagged release with:
  - Auto-generated release notes
  - Release description and usage instructions
  - Download assets

## Version Strategy

### Semantic Versioning Rules

- **MAJOR** (X.0.0): Breaking changes, API incompatibility
- **MINOR** (x.Y.0): New features, backward compatible
- **PATCH** (x.y.Z): Bug fixes, backward compatible

### Examples

- `1.0.0` → `1.0.1` (bug fix)
- `1.0.1` → `1.1.0` (new feature) 
- `1.1.0` → `2.0.0` (breaking change)

## GitHub Packages Usage

### For Consumers

Add GitHub Packages repository to your `pom.xml`:

```xml
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/StigLau/VideoRenderer</url>
    </repository>
</repositories>
```

Add authentication to your `~/.m2/settings.xml`:

```xml
<servers>
    <server>
        <id>github</id>
        <username>YOUR_GITHUB_USERNAME</username>
        <password>YOUR_GITHUB_TOKEN</password>
    </server>
</servers>
```

## Troubleshooting

### Common Issues

**Issue**: Version format validation fails
**Solution**: Ensure version follows X.Y.Z format (e.g., 1.2.3)

**Issue**: Tag already exists
**Solution**: Check existing releases, use next available version

**Issue**: Tests fail during release
**Solution**: Fix tests on main branch before triggering release

**Issue**: GitHub Packages authentication fails
**Solution**: Verify GITHUB_TOKEN permissions include `packages:write`

### Release Rollback

If release fails after tag creation:
```bash
# Delete the tag locally and remotely
git tag -d v1.2.0
git push origin :refs/tags/v1.2.0

# Delete the GitHub release via web interface
# Fix issues and retry release
```

## Workflow Status

### Active Workflows
- **`auto_release.yml`**: Automatic releases on PR merge (PATCH version increments)
- **`release.yml`**: Manual releases with full version control
- **`manual_release.yml`**: Post-release publishing support

### Disabled Workflows
- **`pr_deployment.yml`**: Disabled to prevent version conflicts
  - Previously auto-released on PR merge using run numbers
  - Caused inconsistent versioning with proper semantic releases
  - Replaced by `auto_release.yml` with proper semantic versioning

## Current Project State

- **Current Version**: 1.2.0-SNAPSHOT
- **Main Branch**: master
- **Java Version**: 17
- **Build Profile**: ex-integration

## Release History

Track releases via:
- GitHub Releases page
- GitHub Packages registry
- Git tags (`git tag -l`)

## Version Strategy Recommendations

### When to Use Each Workflow

- **Automatic Release**: Regular development, bug fixes, small enhancements
  - Increments PATCH version automatically
  - Zero overhead for continuous delivery
  
- **Manual Release**: Breaking changes, major features, version strategy changes
  - Full control over MAJOR/MINOR version increments
  - Can specify exact versions for coordinated releases

---

**Status**: Both automatic and manual release workflows available. Automatic releases provide continuous delivery, manual releases provide version control.