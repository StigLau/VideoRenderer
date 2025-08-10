# Test Auto Release

This PR tests the automatic release workflow that's already implemented in master.

When merged, should trigger:
- Version bump 1.2.2-SNAPSHOT → 1.2.2 release → 1.2.3-SNAPSHOT  
- GitHub Packages deployment
- GitHub release creation
- Branch cleanup