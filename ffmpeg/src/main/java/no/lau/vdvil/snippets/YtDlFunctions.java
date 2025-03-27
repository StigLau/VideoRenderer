package no.lau.vdvil.snippets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

public class YtDlFunctions {

    static Logger log = LoggerFactory.getLogger(YtDlFunctions.class);

    public static String performYoutubeDL(String command, File targetDir) throws IOException {
        //Make sure destination folder has been created!
        log.info("Running command: {}: {}", targetDir, command);

        Process p;
        if (targetDir == null) {
            p = Runtime.getRuntime().exec(command);
        } else {
            if (!targetDir.exists()) {
                log.info("Creating missing target directory structure: {}", targetDir);
                Files.createDirectories(targetDir.toPath());
            }
            p = Runtime.getRuntime().exec(command, new String[]{}, targetDir);
        }


        BufferedReader stdInput = new BufferedReader(new
                InputStreamReader(p.getInputStream()));


        String s;
        String result = "";
        while ((s = stdInput.readLine()) != null) {
            result += s + "\n";
            //Printing to console, to keep end user updated
            //System.out.print(s + "\r");
            System.out.println(s);
        }

        BufferedReader stdError = new BufferedReader(new
                InputStreamReader(p.getErrorStream()));

        result += "\n\n\n";
        while ((s = stdError.readLine()) != null) {
            result += s + "\n";
        }

        return result;
    }

    public static String extractYoutubeIdFromUrl(String downloadUrl) throws MalformedURLException {
        if (downloadUrl.contains("watch?v=")) {
            String urlParams = new URL(downloadUrl).getQuery();
            String[] param = urlParams.split("=");
            String youtubeIdent = param[1].split("&")[0];
            if (youtubeIdent.contains("?")) {
                return youtubeIdent.substring(0, youtubeIdent.indexOf("?"));
            } else {
                return youtubeIdent;
            }
        }
        throw new MalformedURLException("Problem with extracting youtubeId from youtube url: " + downloadUrl);
    }
}
