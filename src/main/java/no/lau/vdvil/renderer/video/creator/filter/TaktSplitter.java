package no.lau.vdvil.renderer.video.creator.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stig@Lau.no 24/04/15.
 */
public class TaktSplitter<TYPE> implements ListFilter<TYPE> {

    final int takter;

    public TaktSplitter(int takter) {
        this.takter = takter;
    }

    public List<TYPE> modifyList(List<TYPE> listToBeModified) {
        List<TYPE> taktersplitImages = new ArrayList<>();
        if (takter > 0) {
            for (int i = 0; i < listToBeModified.size(); i++) {
                if (i % takter == 0) {
                    taktersplitImages.add(listToBeModified.get(i));
                }
            }
        } else {
            taktersplitImages = new ArrayList<>(listToBeModified);
        }
        return taktersplitImages;
    }
}

