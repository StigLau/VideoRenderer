package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.renderer.video.creator.filter.PercentageSplitter;
import no.lau.vdvil.renderer.video.creator.filter.Reverter;
import no.lau.vdvil.renderer.video.creator.filter.TaktSplitter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Stig@Lau.no 23/04/15.
 */
public class ListFilterLogicTest {

    List<String> testList;
    @BeforeEach
    public void setUp() {
        testList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            testList.add("" + i);
        }
    }

    @Test
    public void testSplitStreamByPercentag() {
        List<String> rez = new PercentageSplitter<String>(0, 0.5).modifyList(testList);
        assertEquals(5, rez.size());
        assertEquals("0", rez.get(0));
        assertEquals("4", rez.get(4));
    }

    @Test
    public void testSplitStreamByTakt() {
        List<String> rez = new TaktSplitter<String>(3).modifyList(testList);
        assertEquals(3, rez.size());
        assertEquals("0", rez.get(0));
        assertEquals("3", rez.get(1));
        assertEquals("6", rez.get(2));
    }

    @Test
    public void testReversingList() {
        List<String> rez = new Reverter<String>().modifyList(testList);
        assertEquals(9, rez.size());
        assertEquals("8", rez.get(0));
        assertEquals("7", rez.get(1));
        assertEquals("1", rez.get(7));
        assertEquals("0", rez.get(8));
    }
}
