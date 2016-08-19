package no.lau.vdvil.domain;

import java.util.List;

/**
 * When segments have multiple related segments
 */
public interface MetaSegment {
    List<String> references();
}
