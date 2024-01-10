package no.lau.vdvil.collector;

import no.lau.vdvil.plan.ImageCollectable;

public interface CollectorWrapper {
    ImageCollector callBack(ImageCollectable plan);
}
