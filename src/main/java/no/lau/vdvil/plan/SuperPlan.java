package no.lau.vdvil.plan;

import no.lau.vdvil.collector.*;
import no.lau.vdvil.collector.plan.*;
import no.lau.vdvil.domain.MediaFile;
import no.lau.vdvil.domain.Segment;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.*;

/**
 * @author Stig@Lau.no
 */
public class SuperPlan implements FrameRepresentationsPlan, AudioPlan, ImageCollectable {

    final long lastTimeStamp;
    final FramePlan[] framePlans;
    List<FrameRepresentation> frameRepresentations = new ArrayList<>();
    final MediaFile storageLocation;
    public URL audioLocation;

    public SuperPlan(long lastTimeStamp, MediaFile storageLocation, FramePlan... framePlans) {
        this.lastTimeStamp = lastTimeStamp;
        this.framePlans = framePlans;
        this.storageLocation = storageLocation;

        for (FramePlan framePlan : framePlans) {
            frameRepresentations.addAll(framePlan.calculateFramesFromSegment());
        }
    }

    @Override
    public boolean isFinishedProcessing(long timestamp) {
        return timestamp > lastTimeStamp;
    }

    @Override
    public List<FrameRepresentation> whatToDoAt(long timestamp) {
        List<FrameRepresentation> foundFrames = new ArrayList<>();
        for (FrameRepresentation frameRepresentation : frameRepresentations) {
            if(!frameRepresentation.used && frameRepresentation.timestamp <= timestamp) {
                frameRepresentation.use();
                foundFrames.add(frameRepresentation);
            }
        }
        return foundFrames;
    }

    public String id() {
        return toString();
    }

    public String ioFile() {
        return storageLocation.fileName.toString();
    }

    public static long calculateLastTimeStamp(float bpm, List<Segment> segments) {
        long lastTimeStamp = 0;
        for (Segment segment : segments) {
            long current = calculateEnd(bpm, segment);
            if (current > lastTimeStamp) {
                lastTimeStamp = current;
            }
        }
        return lastTimeStamp;
    }

    public static long calculateEnd(float bpm, Segment segment) {
        return segment.startCalculated(bpm) + segment.durationCalculated(bpm);
    }

    public static FramePlan createCollectPlan(Segment originalSegment, FramePlan buildFramePlan, long finalFramerate, float collectBpm) {
        Segment buildSegment = buildFramePlan.wrapper().segment;
        float buildBpm = buildFramePlan.wrapper().bpm;
        long buildCalculatedBpm = buildSegment.durationCalculated(buildBpm);
        return SegmentFramePlanFactory.createInstance(buildSegment.id(), new SegmentWrapper(originalSegment, collectBpm, finalFramerate, new SimpleCalculator(originalSegment.durationCalculated(collectBpm), buildCalculatedBpm)));
    }

    public static FramePlan[] createBuildPlan(List<Segment> buildSegments, float buildBpm, long finalFramerate, Map<String, Segment> segmentIdCollectSegmentMap) {
        List<FramePlan> framePlans = new ArrayList<>();
        for (Segment buildSegment : buildSegments) {
            SimpleCalculator frameCalculator;
            { //Extract the Frame duration
                Segment collectSegment = segmentIdCollectSegmentMap.get(buildSegment.shortId());
                if(collectSegment == null) {
                    throw new RuntimeException("Could not find buildSegment with id: " + buildSegment.shortId()+" in collectSegmentList " + segmentIdCollectSegmentMap.keySet());
                } else {
                    long collectDuration = collectSegment.durationCalculated(buildBpm);

                    long buildDuration = buildSegment.durationCalculated(buildBpm);
                    frameCalculator = new SimpleCalculator(collectDuration, buildDuration);
                }
            }
            framePlans.add(SegmentFramePlanFactory.createInstance(buildSegment.id(), new SegmentWrapper(buildSegment, buildBpm, finalFramerate, frameCalculator)));
        }
        return framePlans.toArray(new FramePlan[framePlans.size()]);
    }

    public List<FramePlan> getFramePlans() {
        return Arrays.asList(framePlans);
    }

    public List<FrameRepresentation> getFrameRepresentations() {
        return frameRepresentations;
    }

    public ImageCollector collector(ImageStore<BufferedImage> imageStore, int framerateMillis) {
        for (FramePlan framePlan : framePlans) {
            if(framePlan instanceof KnownNumberOfFramesPlan || framePlan instanceof TimeStampFixedImageSamplePlan) {
                return new WaitingVideoThumbnailsCollector(this, imageStore, true);
            } else if(framePlan instanceof StaticImagesFramePlan) {
                return new FromImageFileCollector(this, imageStore, framerateMillis);
            } else {
                //TODO Simplify the different possibilities and maby move it out
                throw new RuntimeException("Not implemented collection type for " + framePlan.getClass());
            }
        }
        throw new RuntimeException("Should not happen!");
    }

    public URL audioLocation() {
        return audioLocation;
    }

    public Plan withAudioLocation(URL audioLocation) {
        this.audioLocation = audioLocation;
        return this;
    }
}