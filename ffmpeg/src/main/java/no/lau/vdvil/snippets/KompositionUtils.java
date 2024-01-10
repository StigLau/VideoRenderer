package no.lau.vdvil.snippets;

import no.lau.vdvil.renderer.video.ExtensionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class KompositionUtils {
    public static Path createTempFile(String descriptiveName, ExtensionType extensionType) throws IOException {
        Path tempfile = Files.createTempFile(descriptiveName, "." + extensionType.name());
        Files.deleteIfExists(tempfile);
        return tempfile;
    }

    public static Path createTempFiles(ExtensionType extensionType, Path[] snippets) throws IOException {
        Path fileList = createTempFile("fileList", extensionType);
        String strung = "";
        for (Path snipppet : snippets) {
            strung += "file '" + snipppet.toString() + "'\n";
        }
        Files.write(fileList, strung.getBytes());
        return fileList;
    }
}
