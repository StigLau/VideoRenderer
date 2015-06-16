package com.github.axet.vget;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.axet.vget.info.VGetParser;
import com.github.axet.vget.info.VideoInfo;
import com.github.axet.vget.vhs.VimeoInfo;
import com.github.axet.vget.vhs.YoutubeInfo;
import com.github.axet.wget.info.DownloadInfo;
import com.github.axet.wget.info.DownloadInfo.Part;
import com.github.axet.wget.info.DownloadInfo.Part.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppManagedDownload {

    private Logger logger = LoggerFactory.getLogger(AppManagedDownload.class);

    public void run(URL url, File path) {
        try {
            Downloader downloader = new Downloader();

            AtomicBoolean stop = new AtomicBoolean(false);

            // [OPTIONAL] limit maximum quality, or do not call this function if
            // you wish maximum quality available.
            //
            // if youtube does not have video with requested quality, program
            // will raise en exception.
            VGetParser user = null;

            // create proper html parser depends on url
            user = VGet.parser(url);

            // download maximum video quality from youtube
            // user = new YouTubeQParser(YoutubeQuality.p480);

            // download mp4 format only, fail if non exist
            // user = new YouTubeMPGParser();

            // create proper videoinfo to keep specific video information
            downloader.info = user.info(url);

            VGet v = new VGet(downloader.info, path);

            // [OPTIONAL] call v.extract() only if you d like to get video title
            // or download url link
            // before start download. or just skip it.
            v.extract(user, stop, downloader);

            logger.info("Title: " + downloader.info.getTitle());
            logger.info("Download URL: " + downloader.info.getInfo().getSource());

            v.download(user, stop, new Downloader());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

class Downloader implements Runnable {
    VideoInfo info;
    long last;

    private Logger logger = LoggerFactory.getLogger(AppManagedDownload.class);

    public void run() {
        VideoInfo i1 = info;
        DownloadInfo i2 = i1.getInfo();

        // notify app or save download state
        // you can extract information from DownloadInfo info;
        switch (i1.getState()) {
            case EXTRACTING:
            case EXTRACTING_DONE:
            case DONE:
                if (i1 instanceof YoutubeInfo) {
                    YoutubeInfo i = (YoutubeInfo) i1;
                    logger.info(i1.getState() + " " + i.getVideoQuality());
                } else if (i1 instanceof VimeoInfo) {
                    VimeoInfo i = (VimeoInfo) i1;
                    logger.info(i1.getState() + " " + i.getVideoQuality());
                } else {
                    logger.warn("downloading unknown quality");
                }
                break;
            case RETRYING:
                logger.warn(i1.getState() + " " + i1.getDelay());
                break;
            case DOWNLOADING:
                long now = System.currentTimeMillis();
                if (now - 1000 > last) {
                    last = now;

                    String parts = "";

                    List<Part> pp = i2.getParts();
                    if (pp != null) {
                        // multipart download
                        for (Part p : pp) {
                            if (p.getState().equals(States.DOWNLOADING)) {
                                parts += String.format("Part#%d(%.2f) ", p.getNumber(), p.getCount()
                                        / (float) p.getLength());
                            }
                        }
                    }

                    System.out.println(String.format("%s %.2f %s", i1.getState(),
                            i2.getCount() / (float) i2.getLength(), parts));
                }
                break;
            default:
                break;
        }
    }
}

