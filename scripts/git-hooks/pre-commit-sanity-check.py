#!/usr/bin/env python3
"""
Pre-commit sanity check using Claude Haiku subagent
Usage: python scripts/pre-commit-sanity-check.py
"""

import subprocess
import sys
import json
from pathlib import Path

def get_staged_files():
    """Get list of staged files for commit"""
    try:
        result = subprocess.run(['git', 'diff', '--cached', '--name-only'], 
                              capture_output=True, text=True, check=True)
        return [f.strip() for f in result.stdout.split('\n') if f.strip()]
    except subprocess.CalledProcessError:
        return []

def call_claude_subagent(staged_files):
    """Call Claude subagent to analyze staged files"""
    
    file_list = '\n'.join([f"- {f}" for f in staged_files])
    working_dir = str(Path.cwd())
    
    prompt = f"""Please use Haiku model for cost efficiency. I need you to perform a pre-commit sanity check on these staged files in {working_dir}:

{file_list}

For each file, check for common issues:

**Critical Issues (BLOCK COMMIT):**
- Merge conflict markers (<<<<<<< HEAD, =======, >>>>>>> branch)
- Invalid YAML/JSON syntax
- Obvious syntax errors in code
- Exposed secrets, API keys, passwords, tokens
- TODO/FIXME comments mentioning security issues
- Hardcoded sensitive data (URLs with credentials, etc.)

**Warning Issues (LOG BUT ALLOW):**  
- Large files (>1MB) that might be accidentally committed
- Binary files that shouldn't be tracked
- Overly long lines (>200 chars) in code files
- Missing newlines at end of files
- Inconsistent indentation patterns

**Analysis Instructions:**
1. Use Read tool to examine each staged file (full path: {working_dir}/[filename])
2. Report findings in this format:
   - BLOCK: [filename] - [critical issue description]
   - WARN: [filename] - [warning issue description]  
   - OK: [filename] - looks good

Return ONLY the analysis results, no explanations. If no issues found, return "ALL_CLEAR".

Focus on catching the kinds of mistakes that break builds or expose security issues."""

    print(f"🔍 Calling Haiku subagent to analyze {len(staged_files)} staged files...")
    
    # Call Claude Code Task tool (this would be the actual implementation)
    # For demonstration, we'll use a placeholder that simulates real analysis
    
    # In real implementation, you'd integrate with Claude Code's Task API:
    # response = claude_code_api.task(
    #     subagent_type="general-purpose",
    #     description="Pre-commit sanity check",
    #     prompt=prompt,
    #     model="haiku"  # Cost-efficient model
    # )
    # return response.result
    
    # Placeholder demonstrating the concept:
    print("📝 Note: This is a demonstration. Real implementation would call Claude Code Task API.")
    
    # Simple file-based checks as fallback
    issues = []
    for file_path in staged_files:
        try:
            full_path = Path(file_path)
            if not full_path.exists():
                continue
                
            content = full_path.read_text(encoding='utf-8', errors='ignore')
            
            # Check for merge conflict markers
            if any(marker in content for marker in ['<<<<<<< HEAD', '=======', '>>>>>>> ']):
                issues.append(f"BLOCK: {file_path} - Contains merge conflict markers")
                continue
                
            # Check for common secrets patterns
            secret_patterns = ['password=', 'api_key=', 'secret=', 'token=', 'AUTH_TOKEN']
            if any(pattern in content.lower() for pattern in secret_patterns):
                issues.append(f"BLOCK: {file_path} - Potential exposed credentials")
                continue
                
            # Check file size
            if full_path.stat().st_size > 1024 * 1024:  # 1MB
                issues.append(f"WARN: {file_path} - Large file ({full_path.stat().st_size / (1024*1024):.1f}MB)")
                continue
                
            issues.append(f"OK: {file_path} - looks good")
            
        except Exception as e:
            issues.append(f"WARN: {file_path} - Could not analyze: {str(e)}")
    
    return '\n'.join(issues) if issues else "ALL_CLEAR"

def main():
    """Main pre-commit check function"""
    print("🚀 Running pre-commit sanity check...")
    
    # Get staged files
    staged_files = get_staged_files()
    
    if not staged_files:
        print("✅ No staged files to check")
        return 0
        
    print(f"📋 Checking {len(staged_files)} staged files...")
    
    # Call Claude subagent
    analysis_result = call_claude_subagent(staged_files)
    
    # Parse results
    blocking_issues = []
    warnings = []
    
    for line in analysis_result.strip().split('\n'):
        if line.startswith('BLOCK:'):
            blocking_issues.append(line[6:].strip())
        elif line.startswith('WARN:'):
            warnings.append(line[5:].strip())
    
    # Report results
    if analysis_result.strip() == "ALL_CLEAR":
        print("✅ All files passed sanity check")
        return 0
    
    # Show warnings
    if warnings:
        print("\n⚠️  WARNINGS:")
        for warning in warnings:
            print(f"  {warning}")
    
    # Show blocking issues
    if blocking_issues:
        print("\n❌ CRITICAL ISSUES (blocking commit):")
        for issue in blocking_issues:
            print(f"  {issue}")
        print("\nCommit blocked. Fix issues above and try again.")
        return 1
    
    if warnings:
        print("\n✅ No critical issues found (warnings can be ignored)")
    
    return 0

if __name__ == "__main__":
    sys.exit(main())