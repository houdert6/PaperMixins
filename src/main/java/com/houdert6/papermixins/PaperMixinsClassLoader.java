package com.houdert6.papermixins;

import com.houdert6.papermixins.mixinservices.PaperMixinService;
import org.objectweb.asm.*;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * ClassLoader that manipulates bytecode
 */
public class PaperMixinsClassLoader extends URLClassLoader {
    public PaperMixinsClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            if (isLoaded(name)) {
                return findLoadedClass(name);
            }
            if (name.startsWith(PaperMixinsClassLoaderUtils.class.getName())) { // the PaperMixinsClassLoaderUtils class, and all its internal classes should be defined and loaded by this ClassLoader
                // getClass().getClassLoader() because this classloader doesn't have this plugin loaded
                try (InputStream classStream = getClass().getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class")) {
                    byte[] classData = classStream.readAllBytes();
                    ClassReader r = new ClassReader(classData);
                    ClassWriter w = new ClassWriter(r, 0);
                    ClassVisitor v = new ClassVisitor(Opcodes.ASM9, w) {
                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                            // Make this method return false, as per its javadoc
                            if (name.equals("isPreMixin")) {
                                MethodVisitor v = super.visitMethod(access, name, descriptor, signature, exceptions);
                                v.visitCode();
                                v.visitInsn(Opcodes.ICONST_0); // Not a pre mixin state anymore so return false instead of true
                                v.visitInsn(Opcodes.IRETURN);
                                v.visitMaxs(1, 0);
                                v.visitEnd();
                                // Return a blank method visitor
                                return new MethodVisitor(Opcodes.ASM9) { };
                            } else {
                                return super.visitMethod(access, name, descriptor, signature, exceptions);
                            }
                        }
                    };
                    r.accept(v, 0);
                    byte[] data = w.toByteArray();
                    return defineClass(name, data, 0, data.length);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else if (!name.startsWith("net.minecraft") && !name.startsWith("com.mojang") && !name.startsWith("org.bukkit") && !name.startsWith("io.papermc") && !name.startsWith("com.destroystokyo") && !name.startsWith("ca.spottedleaf") && !name.startsWith("org.spigotmc") && !name.startsWith("co.aikar") && !name.startsWith("me.lucko") && !name.startsWith("alternate.current")) {
                // An interesting question is whether to use a blacklist or whitelist for what classes get passed through and which ones are defined. I chose a whitelist simply because the errors that arise from a non-whitelisted class that should be whitelisted are significantly easier to understand and fix than the errors which arise from a non-blacklisted class that should be blacklisted
                return super.loadClass(name, resolve);
            }
            IMixinTransformer transformer = PaperMixinService.getTransformer();
            if (transformer == null) {
                throw new IllegalStateException("Mixin transformer is null!");
            }
            try (InputStream stream = getResourceAsStream(name.replace('.', '/') + ".class")) {
                byte[] classBytes = null;
                if (stream != null) {
                    classBytes = stream.readAllBytes();
                }
                try {
                    byte[] classData = transformer.transformClassBytes(name, name, classBytes);
                    if (classData != null) {
                        // I don't *need* to reflect defineClass, but I do it to bypass the paper plugin remapper, which would overwrite a regular call to defineClass()
                        Method defineClassMethod = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
                        return (Class<?>) defineClassMethod.invoke(this, name, classData, 0, classData.length);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return super.loadClass(name, resolve);
    }

    /**
     * Checks if the given name has been passed to loadClass() before
     */
    public boolean isLoaded(String name) {
        return findLoadedClass(name) != null;
    }
}
