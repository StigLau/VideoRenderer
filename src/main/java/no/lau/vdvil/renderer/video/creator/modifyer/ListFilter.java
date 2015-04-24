package no.lau.vdvil.renderer.video.creator.modifyer;

import java.util.List;

public interface ListFilter<TYPE> {
    public List<TYPE> modifyList(List<TYPE> listToBeModified);
}
