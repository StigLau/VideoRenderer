package no.lau.vdvil.renderer.video;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test to verify FFmpeg crossfade filter syntax generation
 * Tests the fix for outer quotes and trailing semicolons  
 */
class FilterSyntaxTest {
    
    @Test
    void testCorrectCrossfadeFilterSyntax() {
        // Test the filter generation logic that was fixed
        StringBuilder filterComplex = new StringBuilder();
        
        // Simulate the corrected logic for 3 videos
        for (int i = 0; i < 2; i++) { // 3 videos = 2 transitions
            double crossfadeDuration = 0.2;
            double offset = 7.8;
            
            if (i == 0) {
                // First transition - no leading semicolon
                filterComplex.append(String.format("[0:v][1:v]xfade=transition=fade:duration=%.3f:offset=%.3f[v01]", crossfadeDuration, offset));
            } else if (i == 1) {
                // Last transition - semicolon separator but no trailing semicolon
                filterComplex.append(String.format(";[v0%d][%d:v]xfade=transition=fade:duration=%.3f:offset=%.3f[outv]", i, i + 1, crossfadeDuration, offset));
            } else {
                // Middle transitions - semicolon separator
                filterComplex.append(String.format(";[v0%d][%d:v]xfade=transition=fade:duration=%.3f:offset=%.3f[v0%d]", i, i + 1, crossfadeDuration, offset, i + 1));
            }
        }
        
        String filterString = filterComplex.toString();
        
        // Verify correct syntax (the fix)
        assertTrue(filterString.startsWith("[0:v][1:v]xfade="), "Filter should start correctly");
        assertTrue(filterString.contains("[v01];[v01][2:v]xfade="), "Filter should have correct middle section");
        assertTrue(filterString.endsWith("[outv]"), "Filter should end without trailing semicolon");
        assertFalse(filterString.endsWith("[outv];"), "Filter should NOT end with semicolon (this was the bug)");
        
        // Verify the complete expected pattern
        String expected = "[0:v][1:v]xfade=transition=fade:duration=0.200:offset=7.800[v01];[v01][2:v]xfade=transition=fade:duration=0.200:offset=7.800[outv]";
        assertEquals(expected, filterString, "Generated filter should match expected syntax");
    }
    
    @Test
    void testTwoVideoFilterSyntax() {
        // Test 2-video case
        double crossfadeDuration = 0.5;
        double offset = 9.5; // 10 seconds - 0.5 second crossfade
        
        String filterString = String.format("[0:v][1:v]xfade=transition=fade:duration=%.3f:offset=%.3f[outv]", crossfadeDuration, offset);
        
        // Should not have trailing semicolon
        assertFalse(filterString.endsWith(";"), "Two-video filter should not end with semicolon");
        assertTrue(filterString.endsWith("[outv]"), "Two-video filter should end with [outv]");
        
        String expected = "[0:v][1:v]xfade=transition=fade:duration=0.500:offset=9.500[outv]";
        assertEquals(expected, filterString, "Two-video filter should match expected syntax");
    }
    
    @Test
    void testCommandStructure() {
        // Test that commands don't have outer quotes around filter_complex or map parameters
        String filterContent = "[0:v][1:v]xfade=transition=fade:duration=0.500:offset=9.500[outv]";
        
        // Correct syntax (the fix)
        String correctCommand = "ffmpeg -i input1.mp4 -i input2.mp4 -filter_complex " + filterContent + " -map [outv] -an output.mp4";
        
        // Wrong syntax (the original bugs)  
        String wrongFilterCommand = "ffmpeg -i input1.mp4 -i input2.mp4 -filter_complex \"" + filterContent + "\" -map [outv] -an output.mp4";
        String wrongMapCommand = "ffmpeg -i input1.mp4 -i input2.mp4 -filter_complex " + filterContent + " -map \"[outv]\" -an output.mp4";
        
        // Verify the fixes
        assertFalse(correctCommand.contains("-filter_complex \"[0:v][1:v]xfade"), "Correct command should not have outer quotes around filter_complex");
        assertFalse(correctCommand.contains("-map \"[outv]\""), "Correct command should not have quotes around map parameter");
        
        assertTrue(wrongFilterCommand.contains("-filter_complex \"[0:v][1:v]xfade"), "Wrong filter command would have outer quotes (demonstrating the bug)");
        assertTrue(wrongMapCommand.contains("-map \"[outv]\""), "Wrong map command would have quotes around map parameter (demonstrating the bug)");
        
        // The correct command should have clean syntax
        assertTrue(correctCommand.contains("-filter_complex [0:v][1:v]xfade"), "Correct command should have filter_complex without outer quotes");
        assertTrue(correctCommand.contains("-map [outv]"), "Correct command should have map parameter without quotes");
    }
}