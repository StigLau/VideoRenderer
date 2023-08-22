package no.lau.vdvil.domain;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Path;

public class UrlHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout(30000); // 20 sec
        connection.setReadTimeout(60000); // 30 sec
        return (connection);
    }

    public static URL urlCreator(Path pathUrl) {
        return urlCreator(pathUrl.toString());
    }

    public static URL urlCreator(String finalURI) {
        try {
            //URL.of(URI.create(url), new MyUrlHandler() );
            URI uri = new URI(finalURI);
            String protocol = uri.getScheme();

            String query = uri.getRawQuery();
            String path = uri.getRawPath();
            String file = (query == null) ? path : path + "?" + query;
            String host = uri.getHost();
            if (host == null) {
                host = "";
            }
            int port = uri.getPort();
            return new URL(protocol, host, port, file, new UrlHandler());
        } catch (Exception ex) {
            throw new RuntimeException("URL Creation went to h*ll " + ex.getMessage());
        }
    }
}
