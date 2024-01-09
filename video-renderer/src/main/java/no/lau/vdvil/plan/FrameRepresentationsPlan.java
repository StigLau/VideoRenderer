package no.lau.vdvil.plan;

import no.lau.vdvil.collector.FrameRepresentation;

import java.util.List;

/**
 * @author Stig@Lau.no
 * An interface for a Plan which ahead knows which framerepresentations exist
 */
public interface FrameRepresentationsPlan extends Plan{
    List<FrameRepresentation> getFrameRepresentations();
}
