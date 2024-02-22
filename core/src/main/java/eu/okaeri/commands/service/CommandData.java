package eu.okaeri.commands.service;

import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class CommandData {

    private final Map<String, Object> metadata = new LinkedHashMap<>();

    public boolean has(String key) {
        return this.metadata.containsKey(key);
    }

    public boolean has(String key, Class<?> clazz) {
        return this.has(key) && clazz.isAssignableFrom(this.get(key).getClass());
    }

    public void add(String key, Object object) {
        this.metadata.put(key, object);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        return (T) this.metadata.get(key);
    }

    public Object get(String key) {
        return this.metadata.get(key);
    }

    public Map<String, Object> all() {
        return Collections.unmodifiableMap(this.metadata);
    }
}
