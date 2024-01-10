package no.lau.vdvil.domain.utils;

public interface CacheCallback <K, V>{
    V execute(K key);
}
