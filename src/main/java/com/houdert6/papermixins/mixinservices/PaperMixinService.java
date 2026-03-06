package com.houdert6.papermixins.mixinservices;

import com.houdert6.papermixins.PaperMixinsBootstrap;
import net.kyori.adventure.text.Component;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleURI;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.logging.Level;
import org.spongepowered.asm.logging.LoggerAdapterAbstract;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;
import org.spongepowered.asm.service.*;
import org.spongepowered.asm.util.ReEntranceLock;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Collection;
import java.util.List;

public class PaperMixinService implements IMixinService, IClassProvider, IClassBytecodeProvider, ITransformerProvider, IClassTracker
{
    // The thing actually able to transform class bytecode >:)
    private static IMixinTransformer transformer;
    private ReEntranceLock lock = new ReEntranceLock(1);
    private static Exception ex;

    private byte[] getClassBytes(String name) throws ClassNotFoundException {
        try (InputStream classResource = PaperMixinsBootstrap.loader().getResourceAsStream(name.replace('.', '/') + ".class")) {
            return classResource.readAllBytes();
        } catch (Exception e) {
            throw new ClassNotFoundException("MixinService class not found:", e);
        }
    }
    @Override
    public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return getClassNode(name, true);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers) throws ClassNotFoundException, IOException {
        return getClassNode(name, runTransformers, 0);
    }

    @Override
    public ClassNode getClassNode(String name, boolean runTransformers, int readerFlags) throws ClassNotFoundException, IOException {
        ClassReader reader = new ClassReader(getClassBytes(name));
        ClassNode node = new ClassNode();
        reader.accept(node, readerFlags);
        return node;
    }

    @Override
    public URL[] getClassPath() {
        return new URL[0];
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return PaperMixinsBootstrap.loader().loadClass(name);
    }

    @Override
    public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, PaperMixinsBootstrap.loader());
    }

    @Override
    public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, PaperMixinsBootstrap.class.getClassLoader());
    }

    @Override
    public void registerInvalidClass(String className) {

    }

    @Override
    public boolean isClassLoaded(String className) {
        return PaperMixinsBootstrap.loader().isLoaded(className);
    }

    @Override
    public String getClassRestrictions(String className) {
        return "";
    }

    @Override
    public String getName() {
        return "PaperMixins/PaperMC";
    }

    @Override
    public boolean isValid() {
        return true;
    }

    @Override
    public void prepare() {

    }

    @Override
    public MixinEnvironment.Phase getInitialPhase() {
        return MixinEnvironment.Phase.PREINIT;
    }

    @Override
    public void offer(IMixinInternal internal) {
        if (internal instanceof IMixinTransformer transformer) {
            PaperMixinService.transformer = transformer;
        } else {
            // Might be given a Mixin transformer factory, which is package-private (https://github.com/FabricMC/Mixin/blob/main/src/main/java/org/spongepowered/asm/mixin/transformer/MixinTransformer.java)
            try {
                Method createTransformer = internal.getClass().getDeclaredMethod("createTransformer");
                createTransformer.setAccessible(true);
                transformer = (IMixinTransformer) createTransformer.invoke(internal);
            } catch (Exception e) {
                ex = e;
            }
        }
    }

    @Override
    public void init() {

    }

    @Override
    public void beginPhase() {

    }

    @Override
    public void checkEnv(Object bootSource) {

    }

    @Override
    public ReEntranceLock getReEntranceLock() {
        return lock;
    }

    @Override
    public IClassProvider getClassProvider() {
        return this;
    }

    @Override
    public IClassBytecodeProvider getBytecodeProvider() {
        return this;
    }

    @Override
    public ITransformerProvider getTransformerProvider() {
        return this;
    }

    @Override
    public IClassTracker getClassTracker() {
        return this;
    }

    @Override
    public IMixinAuditTrail getAuditTrail() {
        return null;
    }

    @Override
    public Collection<String> getPlatformAgents() {
        return List.of("org.spongepowered.asm.launch.platform.MixinPlatformAgentDefault");
    }

    @Override
    public IContainerHandle getPrimaryContainer() {
        CodeSource paperMixinSource = PaperMixinsBootstrap.class.getProtectionDomain().getCodeSource();
        try {
            return new ContainerHandleURI(paperMixinSource == null ? null : paperMixinSource.getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Collection<IContainerHandle> getMixinContainers() {
        return List.of();
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return PaperMixinsBootstrap.loader().getResourceAsStream(name);
    }

    @Override
    public String getSideName() {
        return "SERVER"; // PaperMC is server software
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMinCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_8;
    }

    @Override
    public MixinEnvironment.CompatibilityLevel getMaxCompatibilityLevel() {
        return MixinEnvironment.CompatibilityLevel.JAVA_22;
    }

    @Override
    public ILogger getLogger(String name) {
        return new LoggerAdapterAbstract(name) {
            @Override
            public String getType() {
                return "PaperMixins logger";
            }
            @Override
            public void catching(Level level, Throwable t) {
                this.log(level, "Catching ", t);
            }

            @Override
            public void log(Level level, String message, Object... params) {
                switch (level) {
                    case FATAL -> PaperMixinsBootstrap.logger().error(Component.text(message), params);
                    case DEBUG -> PaperMixinsBootstrap.logger().debug(Component.text(message), params);
                    case WARN -> PaperMixinsBootstrap.logger().warn(Component.text(message), params);
                    case TRACE -> PaperMixinsBootstrap.logger().trace(Component.text(message), params);
                }
            }

            @Override
            public void log(Level level, String message, Throwable t) {
                switch (level) {
                    case FATAL -> PaperMixinsBootstrap.logger().error(Component.text(message), t);
                    case DEBUG -> PaperMixinsBootstrap.logger().debug(Component.text(message), t);
                    case WARN -> PaperMixinsBootstrap.logger().warn(Component.text(message), t);
                    case TRACE -> PaperMixinsBootstrap.logger().trace(Component.text(message), t);
                }
            }

            @Override
            public <T extends Throwable> T throwing(T t) {
                PaperMixinsBootstrap.logger().error(Component.text("Throwing "), t);
                return t;
            }
        };
    }

    @Override
    public Collection<ITransformer> getTransformers() {
        return List.of();
    }

    @Override
    public Collection<ITransformer> getDelegatedTransformers() {
        return List.of();
    }

    @Override
    public void addTransformerExclusion(String name) {

    }

    /**
     * Returns {@link #transformer}
     */
    public static IMixinTransformer getTransformer() {
        if (transformer == null) {
            PaperMixinsBootstrap.logger().warn(Component.text("Couldn't convert internal to transformer, PaperMixins will likely crash really soon..."), ex);
        }
        return transformer;
    }
}
