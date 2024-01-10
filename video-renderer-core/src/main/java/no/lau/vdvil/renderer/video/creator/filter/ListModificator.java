package no.lau.vdvil.renderer.video.creator.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Stig@Lau.no 23/04/15.
 */
public class ListModificator<TYPE> {

    final List<ListFilter> filters;

    public ListModificator(ListFilter... filters) {
        this.filters = Arrays.asList(filters);
    }

    public List<TYPE> applyModifications(List<TYPE> unfilteredList) {
        List<TYPE> filteredList = new ArrayList<>(unfilteredList);
        for (ListFilter filter : filters) {
            filteredList = filter.modifyList(filteredList);
        }
        return filteredList;
    }
}
