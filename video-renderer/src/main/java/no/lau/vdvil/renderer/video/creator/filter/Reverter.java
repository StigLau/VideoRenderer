package no.lau.vdvil.renderer.video.creator.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Stig@Lau.no 24/04/15.
 */
public class Reverter<TYPE> implements ListFilter<TYPE> {


    @Override
    public List<TYPE> modifyList(List<TYPE> listToBeModified) {
        List<TYPE> maybeSortedList = new ArrayList<>(listToBeModified);
        Collections.reverse(maybeSortedList);
        return maybeSortedList;
    }
}

