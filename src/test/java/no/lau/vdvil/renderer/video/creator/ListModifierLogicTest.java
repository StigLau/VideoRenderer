package no.lau.vdvil.renderer.video.creator;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Stig@Lau.no 23/04/15.
 */
public class ListModifierLogicTest {

    List<String> tahList;
    @Before
    public void setUp() {
        tahList = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            tahList.add(""+i);
        }
    }

    @Test
    public void testSplitStreamByPercentag() {
        List<String> rez = new ListModificator<String>().splitByPercentage(0, 0.5, tahList);
        assertEquals(5, rez.size());
        assertEquals("0", rez.get(0));
        assertEquals("4", rez.get(4));
    }

    @Test
    public void testSplitStreamByTakt() {
        List<String> rez = new ListModificator<String>().splitByTakter(tahList, 3);
        assertEquals(3, rez.size());
        assertEquals("0", rez.get(0));
        assertEquals("3", rez.get(1));
        assertEquals("6", rez.get(2));
    }

    @Test
    public void testReversingList() {
        List<String> rez = new ListModificator<String>().revertImages(tahList, true);
        assertEquals(9, rez.size());
        assertEquals("8", rez.get(0));
        assertEquals("7", rez.get(1));
        assertEquals("1", rez.get(7));
        assertEquals("0", rez.get(8));
    }
}
