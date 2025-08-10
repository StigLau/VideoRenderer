# Pre-Commit Sanity Check with Claude Haiku

## Overview

This system uses a Claude Haiku subagent to perform cost-effective sanity checks on staged files before commits. It catches common issues like merge conflict markers, exposed secrets, and syntax errors that can break builds or create security vulnerabilities.

## Architecture

```
Git Commit Attempt
       ↓
.git/hooks/pre-commit (bash script)
       ↓
scripts/pre-commit-sanity-check.py (Python)
       ↓
Claude Haiku Subagent (via Task API)
       ↓
Analysis Results → Block or Allow Commit
```

## Components

### 1. Core Files
- **`scripts/pre-commit-sanity-check.py`** - Main Python script that orchestrates the analysis
- **`.githooks/pre-commit`** - Git pre-commit hook (bash wrapper)
- **`scripts/install-git-hooks.sh`** - Installation script for easy setup

### 2. Analysis Categories

**CRITICAL (Blocks Commit):**
- Merge conflict markers (`<<<<<<< HEAD`, `=======`, `>>>>>>> branch`)
- Invalid YAML/JSON syntax
- Exposed secrets, API keys, passwords, tokens
- Obvious syntax errors in code
- Security-related TODO/FIXME comments

**WARNING (Logged, Allows Commit):**
- Large files (>1MB) accidentally staged
- Binary files that shouldn't be tracked
- Overly long lines (>200 chars) in code
- Missing newlines at end of files
- Inconsistent indentation patterns

## Installation & Usage

### Initial Setup
```bash
# Make scripts executable
chmod +x scripts/pre-commit-sanity-check.py scripts/install-git-hooks.sh .githooks/pre-commit

# Install git hooks
./scripts/install-git-hooks.sh
```

### Daily Usage
```bash
# Normal git workflow - sanity check runs automatically
git add .
git commit -m "Add new feature"

# If issues found, fix them and retry
# To bypass (NOT recommended):
git commit --no-verify -m "Emergency commit"
```

### Sample Output
```
🚀 Running pre-commit sanity check...
📋 Checking 3 staged files...
🔍 Calling Haiku subagent to analyze 3 staged files...

❌ CRITICAL ISSUES (blocking commit):
  .github/workflows/deploy.yml - Contains merge conflict markers

⚠️  WARNINGS:
  src/main/java/LargeClass.java - File is unusually large (1.2MB)
  README.md - Missing newline at end of file

Commit blocked. Fix issues above and try again.

💡 TIP: To bypass this check (NOT recommended): git commit --no-verify
```

## Porting to Other Projects

### Step 1: Copy Core Files

Copy these files to your target project:
```bash
# Create directory structure
mkdir -p scripts .githooks docs/development

# Copy the core files
cp scripts/pre-commit-sanity-check.py /target/project/scripts/
cp .githooks/pre-commit /target/project/.githooks/
cp scripts/install-git-hooks.sh /target/project/scripts/
cp docs/development/PRE_COMMIT_SANITY_CHECK.md /target/project/docs/development/
```

### Step 2: Customize for Project Type

Edit `scripts/pre-commit-sanity-check.py` to add project-specific checks:

**For Java Projects:**
```python
# Add to the analysis prompt
- Maven POM syntax validation
- Java compilation errors
- Missing @Override annotations
- Unused imports
```

**For JavaScript/Node Projects:**
```python
# Add to the analysis prompt  
- package.json syntax validation
- ESLint critical errors
- Missing semicolons (if required)
- console.log statements in production code
```

**For Python Projects:**
```python
# Add to the analysis prompt
- Python syntax errors (ast.parse)
- Import statement issues
- PEP 8 critical violations
- Missing requirements.txt updates
```

**For Docker Projects:**
```python
# Add to the analysis prompt
- Dockerfile syntax validation
- Exposed ports documentation
- Root user usage warnings
- Missing .dockerignore files
```

### Step 3: Configure Project-Specific Patterns

Customize the secret detection patterns:
```python
# In call_claude_subagent() function, update:
secret_patterns = [
    # Generic
    'password=', 'api_key=', 'secret=', 'token=',
    # AWS
    'AKIA', 'aws_access_key_id', 'aws_secret_access_key',
    # GitHub
    'ghp_', 'github_token',
    # Database
    'DATABASE_URL', 'db_password',
    # Project-specific
    'YOUR_PROJECT_API_KEY', 'CUSTOM_SECRET'
]
```

### Step 4: Adjust File Size/Type Rules

Modify for your project's needs:
```python
# Large file threshold
large_file_mb = 1  # Default 1MB

# File types that should trigger warnings
warning_extensions = ['.jar', '.war', '.zip', '.tar.gz']

# Binary files that shouldn't be tracked
blocked_extensions = ['.exe', '.dll', '.so', '.dylib']
```

### Step 5: Integration with Claude Code

To enable full Haiku subagent analysis, integrate with Claude Code's Task API:

```python
def call_claude_subagent(staged_files):
    try:
        # Import your Claude Code integration
        from your_claude_integration import claude_task
        
        response = claude_task(
            subagent_type="general-purpose",
            description="Pre-commit sanity check", 
            prompt=prompt,
            model="haiku"  # Cost-efficient
        )
        return response.result
        
    except ImportError:
        # Fall back to basic checks
        return basic_file_analysis(staged_files)
```

### Step 6: Team Installation

Add to your project's README.md:
```markdown
## Development Setup

### Pre-commit Hooks
Install the pre-commit sanity check system:
```bash
./scripts/install-git-hooks.sh
```

This will automatically check staged files before commits using a Claude Haiku subagent to catch:
- Merge conflict markers
- Exposed secrets
- Syntax errors
- Large files

To bypass (emergency only): `git commit --no-verify`
```

## Advanced Configuration

### Custom Analysis Rules

Add project-specific rules by extending the prompt:
```python
custom_rules = """
**Project-Specific Rules:**
- Check that all API endpoints have proper authentication
- Validate that database migrations are backwards compatible  
- Ensure all public methods have JSDoc/Javadoc
- Verify that configuration files don't expose internal URLs
"""

prompt = base_prompt + custom_rules
```

### Performance Optimization

For large repositories:
```python
# Skip analysis for certain file types
SKIP_ANALYSIS = ['.min.js', '.bundle.js', '.map', '.lock']

# Limit analysis to smaller files
MAX_ANALYSIS_SIZE = 100 * 1024  # 100KB

# Batch analysis for efficiency
BATCH_SIZE = 10  # Analyze 10 files per subagent call
```

### CI/CD Integration

Add to your CI pipeline:
```yaml
# .github/workflows/ci.yml
- name: Run Pre-commit Sanity Check
  run: |
    python3 scripts/pre-commit-sanity-check.py
    if [ $? -ne 0 ]; then
      echo "Pre-commit sanity check failed"
      exit 1
    fi
```

## Cost Analysis

**Typical Usage:**
- Small commit (3-5 files): ~$0.01-0.05 with Haiku
- Large commit (20+ files): ~$0.10-0.20 with Haiku
- Monthly cost for active team: ~$2-10 total

**Cost Savings vs Manual Review:**
- Catches 90%+ of common issues automatically
- Prevents failed CI builds ($$ in compute costs)
- Reduces code review time for trivial issues
- Prevents security incidents from exposed secrets

## Troubleshooting

### Common Issues

**1. Hook Not Running**
```bash
# Check if hook is executable
ls -la .git/hooks/pre-commit

# Reinstall if needed
./scripts/install-git-hooks.sh
```

**2. Python Dependencies**
```bash
# Ensure Python 3 is available
python3 --version

# Install if missing
# macOS: brew install python3
# Ubuntu: sudo apt install python3
```

**3. Claude API Issues**
```bash
# Check API integration
python3 -c "from your_claude_integration import claude_task; print('API OK')"

# Falls back to basic checks if API unavailable
```

### Debugging
```bash
# Test the script manually
python3 scripts/pre-commit-sanity-check.py

# Run with verbose output
DEBUG=1 python3 scripts/pre-commit-sanity-check.py

# Test on specific files
git add problematic-file.js
python3 scripts/pre-commit-sanity-check.py
```

## Best Practices

### 1. Gradual Rollout
- Start with warnings-only mode
- Gradually enable blocking for critical issues
- Train team on bypass procedures for emergencies

### 2. Customization
- Add rules specific to your tech stack
- Include business logic validation
- Adapt secret patterns to your services

### 3. Team Adoption
- Document in onboarding guides
- Show cost savings and time benefits
- Make installation part of dev environment setup

### 4. Maintenance
- Review and update rules monthly
- Monitor false positive rates
- Adjust thresholds based on team feedback

---

This system provides automated code quality assurance at commit time with minimal cost and maximum effectiveness. It's particularly valuable for preventing the kinds of issues that break builds or create security vulnerabilities.