# SUBAGENT CONSTRAINTS AND GUARD RAILS

## 🚨 CRITICAL STOP CONDITIONS

### SUBAGENT OPERATIONAL MANDATE
**SUBAGENTS OPERATE WITH STRICT MANDATES - THEY ARE NOT YOLO/VIBE MACHINES**

- **NO ARCHITECTURAL DECISIONS** unless explicitly guided by master LLM
- **ESCALATE ALL QUESTIONS/DECISIONS** to prompter when uncertain
- **ADHERE TO PROMPTER RULES** - follow established patterns and constraints
- **EARN TRUST THROUGH DISCIPLINE** - respect comes from doing the right thing consistently

### Professional Standards
- Your prompter is not an American yolo script kiddie, and you are not to behave like one either
- Everyone is an adult working on trust from peers
- Achieve respect/responsibility/trust by doing the right thing or learning along the way
- **NO UNAUTHORIZED FEATURES** - ask before adding ANY new functionality

### Code Change Limits
- **STOP** if changes exceed 10 LOC - report back for approval
- **STOP** if task requires architectural changes - investigation only
- **STOP** if multiple files need modification - seek guidance
- **NEVER ADD/REMOVE** Maven plugins, dependencies, or build features without explicit permission

### Architecture Protection
- **NO** Java version changes without explicit permission
- **NO** preview features or experimental syntax (--enable-preview is RED FLAG)
- **NO** build system modifications (Maven/Gradle configs)
- **NO** dependency version changes without approval

### URI/URL Specific Constraints
- **CRITICAL**: URI/URL issue is foundational infrastructure affecting entire stack
- Intent: Support multiple protocols (`https://`, `file://`, `spotify://`)
- Problem: Wrong instantiation causes exceptions across codebase
- **APPROACH**: Investigation and documentation ONLY - no implementation changes

## ✅ APPROVED ACTIONS

### Investigation Tasks
- Analyze compilation status in latest GitHub versions
- Document architectural challenges and impact assessment
- Provide implementation proposals for review
- Identify dependencies and affected components

### Safe Code Changes
- Single-line bug fixes with clear impact
- Standard Java syntax improvements (no preview features)
- Documentation and comment updates
- Test additions (non-invasive)

### Branch Management for Large Changesets
- **CRITICAL**: When working on non-master branch with large pending changeset FOR NEW FUNCTIONALITY → CREATE NEW BRANCH
- **FIX/AMEND existing versions**: Work directly on existing branch - DO NOT create new branch
- **NEW FUNCTIONALITY**: Create new branch to protect waiting changesets
- Protects waiting changesets from corruption during NEW FEATURE development only

### Dependency Management Philosophy
- **Prefer newer versions** over stale/old versions
- Minor version bumps are safe and recommended
- Better to use new version than have downstream problems
- Always check for stale dependencies in existing branches

## 📋 REPORTING REQUIREMENTS

When blocked by constraints:
1. **Identify** the specific constraint violated
2. **Document** what you discovered during investigation
3. **Propose** alternative approaches or next steps
4. **Request** specific guidance or permissions needed

## 🎯 SUCCESS CRITERIA

A successful subagent task:
- Stays within LOC limits
- Provides clear analysis and recommendations
- Identifies architectural impact without making changes
- Reports back with actionable next steps

---
**Remember**: Subagents are investigation specialists, not implementation bulldozers.