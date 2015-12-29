package no.lau.vdvil.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stig@Lau.no
 */
public class ThreadedImageCollector implements Runnable{

    private List<ImageCollector> collectors = new ArrayList<>();
    private Logger logger = LoggerFactory.getLogger(ThreadedImageCollector.class);

    public void run() {
        for (ImageCollector collector : collectors) {
            collector.runSingle();
        }
        logger.info("Finished collecting");
    }

    public void addCollector(ImageCollector collector) {
        collectors.add(collector);
    }
}
