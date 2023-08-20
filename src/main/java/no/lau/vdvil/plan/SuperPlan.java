package no.lau.vdvil.plan;

import no.lau.vdvil.collector.*;
import no.lau.vdvil.collector.plan.*;
import no.lau.vdvil.domain.*;
import no.lau.vdvil.renderer.video.creator.ImageStore;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Stig@Lau.no
 */
public class SuperPlan implements FrameRepresentationsPlan, AudioPlan, ImageCollectable {

    final long lastTimeStamp;
    final FramePlan[] framePlans;
    List<FrameRepresentation> frameRepresentations = new ArrayList<>();
    final MediaFile storageLocation;
    public PathRef audioLocation;
    int imageDownloadCacheSize = 10; //Just a default to allow for more than one cached image

    final Map<String, FramePlan> metaPlanLookup;

    public SuperPlan(long lastTimeStamp, MediaFile storageLocation, FramePlan... framePlans) {
        this.lastTimeStamp = lastTimeStamp;
        this.framePlans = framePlans;
        this.storageLocation = storageLocation;

        for (FramePlan framePlan : framePlans) {
            frameRepresentations.addAll(framePlan.calculateFramesFromSegment());
        }
        Collections.sort(frameRepresentations);
        metaPlanLookup = buildMetaPlanLookup(framePlans);
    }

    @Override
    public boolean isFinishedProcessing(long timestamp) {
        return timestamp > lastTimeStamp;
    }

    public List<FrameRepresentation> whatToDoAt(long timestamp) {
        return whatToDoAt(timestamp, true);
    }

    List<FrameRepresentation> whatToDoAt(long timestamp, boolean careAboutUsed) {
        List<FrameRepresentation> foundFrames = new ArrayList<>();
        for (FrameRepresentation frameRepresentation : frameRepresentations) {
            if(frameRepresentation.timestamp <= timestamp) {
                if(careAboutUsed) {
                    if (!frameRepresentation.used) {
                        foundFrames.add(frameRepresentation);
                    }
                } else {
                    foundFrames.add(frameRepresentation);
                }
            }
        }
        return foundFrames;
    }

    public String id() {
        return toString();
    }

    public PathRef localStorage() {
        return storageLocation.getReference();
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
                if(buildSegment instanceof TransitionSegment) {
                    frameCalculator = new SimpleCalculator(1, 1); //Don't care about this calculator
                } else if(collectSegment == null) {
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
                return new FromImageFileCollector(this, imageStore, framerateMillis, imageDownloadCacheSize);
            } else {
                //TODO Simplify the different possibilities and maby move it out
                throw new RuntimeException("Not implemented collection type for " + framePlan.getClass());
            }
        }
        throw new RuntimeException("Should not happen!");
    }

    public PathRef audioLocation() {
        return audioLocation;
    }

    public Plan withAudioLocation(PathRef audioLocation) {
        this.audioLocation = audioLocation;
        return this;
    }

    public long lastTimeStamp() {
        return lastTimeStamp;
    }

    public Collection<FramePlan> getMetaPlansAt(long timestamp) {
        return whatToDoAt(timestamp, false).stream().map(
                frameRepresentation -> metaPlanLookup.get(frameRepresentation.referenceId())
        ).collect(Collectors.toSet());
    }

    private static Map<String, FramePlan> buildMetaPlanLookup(FramePlan... framePlans) {
        Map<String, FramePlan> planRef = new HashMap<>();
        for (FramePlan framePlan : framePlans) {
            if(framePlan.wrapper().segment instanceof MetaSegment) {
                for (Object reference : ((TransitionSegment) framePlan.wrapper().segment).references()) {
                    planRef.put((String) reference, framePlan);
                }
            }
        }
        return planRef;
    }
}