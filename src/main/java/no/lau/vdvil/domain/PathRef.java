package no.lau.vdvil.domain;

import no.lau.vdvil.renderer.video.ExtensionType;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathRef implements VUrl {
    Path localPath;

    public PathRef(String pathString) {
        localPath = Paths.get(pathString);
    }
    public PathRef(Path localPath) {
        this.localPath = localPath;
    }

    @Override
    public InputStream openStream() throws IOException {
        return Files.newInputStream(localPath);
    }

    public Path path() {
        return localPath;
    }

    @Override
    public String toString() {
        return localPath.toString();
    }

    public static PathRef createTempPath(String prefix, ExtensionType extensionType) {
        return createTempPath(prefix, "." +extensionType.toString());
    }

    public static PathRef createTempPath(String prefix, String suffix) {
        try {
            Path tempfile = Files.createTempFile(prefix, suffix);
            if(Files.exists(tempfile)) {
                Files.deleteIfExists(tempfile);
            }
            return new PathRef(tempfile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
