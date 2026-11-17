package io.github.gasharaujo.oneenoughfix.harness;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

public final class TestGlobalProperties implements IGlobalPropertyService {
    private final Map<IPropertyKey, Object> values = new ConcurrentHashMap<>();
    private record Key(String name) implements IPropertyKey {}
    @Override public IPropertyKey resolveKey(String name) { return new Key(name); }
    @SuppressWarnings("unchecked")
    @Override public <T> T getProperty(IPropertyKey key) { return (T) values.get(key); }
    @Override public void setProperty(IPropertyKey key, Object value) {
        if (value == null) values.remove(key); else values.put(key, value);
    }
    @Override public <T> T getProperty(IPropertyKey key, T fallback) {
        T value = getProperty(key);
        return value == null ? fallback : value;
    }
    @Override public String getPropertyString(IPropertyKey key, String fallback) {
        Object value = values.get(key);
        return value == null ? fallback : value.toString();
    }
}
