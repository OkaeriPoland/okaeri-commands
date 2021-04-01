package eu.okaeri.commands.service;

import java.util.LinkedHashMap;
import java.util.Map;

public class CommandContext {

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
}
