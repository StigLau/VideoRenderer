package no.lau.vdvil.renderer.video.creator;

import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.domain.VideoStillImageSegment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Stig@Lau.no 23/04/15.
 */
public class ListModificator<TYPE> {
    public List<TYPE> applyModifications(Segment segment, List<TYPE> images) {

        if (segment instanceof VideoStillImageSegment) {
            VideoStillImageSegment imageSegment = (VideoStillImageSegment) segment;

            List<TYPE> taktersplitImages = splitByTakter(images, imageSegment.takter);
            List<TYPE> maybeSortedList = revertImages(taktersplitImages, imageSegment.isReverted());
            List<TYPE> splittedList = splitByPercentage(imageSegment.fromLimit, imageSegment.untilLimit, maybeSortedList);
            return splittedList;
        } else
            return Collections.emptyList();
    }

    List<TYPE> splitByPercentage(double fromLimit, double untilLimit, List<TYPE> maybeSortedList) {
        List<TYPE> newList = new ArrayList<>();
        for (int i = 0, nrOfImages = maybeSortedList.size(); i < nrOfImages; i++) {
            double perc = (double) i / (double) nrOfImages;
            if (perc >= fromLimit && perc < untilLimit) {
                newList.add(maybeSortedList.get(i));
            }
        }
        return newList;
    }

    List<TYPE> revertImages(List<TYPE> taktersplitImages, boolean revert) {
        List<TYPE> maybeSortedList = new ArrayList<>(taktersplitImages);
        if (revert) {
            Collections.reverse(maybeSortedList);
        }
        return maybeSortedList;
    }

    List<TYPE> splitByTakter(List<TYPE> images, int takter) {
        List<TYPE> taktersplitImages = new ArrayList<>();
        if (takter > 0) {
            for (int i = 0; i < images.size(); i++) {
                if (i % takter == 0) {
                    taktersplitImages.add(images.get(i));
                }
            }
        } else {
            taktersplitImages = new ArrayList<>(images);
        }
        return taktersplitImages;
    }
}
