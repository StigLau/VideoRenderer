package no.lau.vdvil.renderer.video.creator.filter;

import java.util.List;

public interface ListFilter<TYPE> {
    public List<TYPE> modifyList(List<TYPE> listToBeModified);
}
