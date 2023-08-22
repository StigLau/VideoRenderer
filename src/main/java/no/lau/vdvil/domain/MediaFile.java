package no.lau.vdvil.domain;

import java.net.URL;
import java.nio.file.Path;

public interface MediaFile {
    Path getReference();

    URL getFileName();
}
