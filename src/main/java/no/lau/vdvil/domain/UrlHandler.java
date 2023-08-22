package no.lau.vdvil.domain;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Path;

public class UrlHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL url) {
        return new URLConnection(url) {

            @Override
            public void connect() {

            }

            @Override
            public InputStream getInputStream() {
                return new ByteArrayInputStream(new byte[]{});
            }

            @Override
            public String getContentType() {
                return "content/notnull";
            }
        };

        /*
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url.toURI())
                .version(HttpClient.Version.HTTP_2)
                .GET()
                .build();
        HttpClient client = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
         response.body();
*/
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
            if(protocol.equals("https")) {
                return new URL(finalURI);
            } else {
                return new URL(protocol, host, port, file, new UrlHandler());
            }
        } catch (Exception ex) {
            throw new RuntimeException("URL Creation went to h*ll " + ex.getMessage());
        }
    }
}
