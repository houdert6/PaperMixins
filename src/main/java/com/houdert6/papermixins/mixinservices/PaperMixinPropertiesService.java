package com.houdert6.papermixins.mixinservices;

import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

import java.util.HashMap;

/**
 * An implementation of the Mixin global property service that just stores everything in a hashmap lol
 */
public class PaperMixinPropertiesService implements IGlobalPropertyService {
    private final HashMap<String, Object> propertyMap = new HashMap<>();

    @Override
    public IPropertyKey resolveKey(String name) {
        return new PropertyKey(name);
    }
    @Override
    public <T> T getProperty(IPropertyKey key) {
        return getProperty(key, null);
    }
    @Override
    public void setProperty(IPropertyKey key, Object value) {
        if (key instanceof PropertyKey propertyKey) {
            propertyMap.put(propertyKey.name, value);
        }
    }
    @Override
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
        return key instanceof PropertyKey propertyKey ? (T)propertyMap.getOrDefault(propertyKey.name, defaultValue) : null;
    }
    @Override
    public String getPropertyString(IPropertyKey key, String defaultValue) {
        return getProperty(key, defaultValue);
    }
    public record PropertyKey(String name) implements IPropertyKey { }
}
