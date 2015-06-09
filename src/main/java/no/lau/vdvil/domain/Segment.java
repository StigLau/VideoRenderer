package no.lau.vdvil.domain;

/**
 * @author Stig@Lau.no 07/04/15.
 */
public interface Segment extends Comparable {
    String id();
    String shortId();
    long start();
    long duration();

    long startCalculated(float bpm);

    long durationCalculated(float bpm);
}
