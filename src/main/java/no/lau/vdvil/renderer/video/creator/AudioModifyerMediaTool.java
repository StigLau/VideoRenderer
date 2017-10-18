package no.lau.vdvil.renderer.video.creator;

import com.xuggle.mediatool.MediaToolAdapter;
import com.xuggle.mediatool.event.IAudioSamplesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ShortBuffer;

class AudioModifyerMediaTool extends MediaToolAdapter {

    private Logger logger = LoggerFactory.getLogger(getClass());

    // the amount to adjust the volume by
    private double mVolume;

    public AudioModifyerMediaTool(double volume) {
        mVolume = volume;
    }

    @Override
    public void onAudioSamples(IAudioSamplesEvent event) {
        logger.info("event = " + event);
        // get the raw audio bytes and adjust it's value
        ShortBuffer buffer = event.getAudioSamples().getByteBuffer().asShortBuffer();

        for (int i = 0; i < buffer.limit(); ++i) {
            buffer.put(i, (short) (buffer.get(i) * mVolume));
        }

        // call parent which will pass the audio onto next tool in chain
        super.onAudioSamples(event);

    }
}