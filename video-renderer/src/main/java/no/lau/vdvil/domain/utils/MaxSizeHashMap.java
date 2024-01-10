package no.lau.vdvil.domain.utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Used as a simple cache
 */
public class MaxSizeHashMap<K, V> extends LinkedHashMap<K, V> {
    private final int maxSize;

    public MaxSizeHashMap(int maxSize) {
        this.maxSize = maxSize;
    }

    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return size() > maxSize;
    }

    //Cache downloaded image to save bandwith to avoid multiple network calls if image has already been downloaded
    public V getOrCache(K key, CacheCallback<K, V> callback) throws IOException {
        if(!containsKey(key)) {
            put(key, callback.execute(key));
        }
        return get(key);
    }
}
