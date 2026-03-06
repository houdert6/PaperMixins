package com.houdert6.papermixins.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.nio.file.Path;

public class PaperMixinConfig {
    /**
     * Name of mixin-containing jar
     */
    public final String name;
    /**
     * Description of mixin-containing jar
     */
    public final String description;
    /**
     * Author of mixin-containing jar
     */
    public final String author;
    /**
     * Link to website of mixin-containing jar
     */
    public final String link;
    /**
     * mixin.json path
     */
    public final String mixinConfig;
    /**
     * Optional plugin related to mixin-containing jar
     */
    public final String relatedPlugin;
    /**
     * version of mixin. Defaults to 1.0.0
     */
    @NotNull
    public final String version;

    public Path pluginPath;

    /**
     * Reads a mixin config from a file
     */
    public PaperMixinConfig(Reader yml) {
        try {
            YamlConfiguration ymlConfig = YamlConfiguration.loadConfiguration(yml);
            name = ymlConfig.getString("name");
            description = ymlConfig.getString("description");
            author = ymlConfig.getString("author");
            link = ymlConfig.getString("link");
            mixinConfig = ymlConfig.getString("mixins");
            relatedPlugin = ymlConfig.getString("related-plugin");
            version = ymlConfig.getString("version", "1.0.0");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
