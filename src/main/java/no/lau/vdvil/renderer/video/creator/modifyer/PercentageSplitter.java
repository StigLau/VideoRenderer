package no.lau.vdvil.renderer.video.creator.modifyer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stig@Lau.no 24/04/15.
 */
public class PercentageSplitter<TYPE> implements ListFilter<TYPE> {

    final double fromLimit;
    final double untilLimit;

    public PercentageSplitter(double fromLimit, double untilLimit) {
        this.fromLimit = fromLimit;
        this.untilLimit = untilLimit;
    }

    public List<TYPE> modifyList(List<TYPE> listToBeModified) {
        List<TYPE> newList = new ArrayList<>();
        for (int i = 0, nrOfImages = listToBeModified.size(); i < nrOfImages; i++) {
            double perc = (double) i / (double) nrOfImages;
            if (perc >= fromLimit && perc < untilLimit) {
                newList.add(listToBeModified.get(i));
            }
        }
        return newList;
    }
}

