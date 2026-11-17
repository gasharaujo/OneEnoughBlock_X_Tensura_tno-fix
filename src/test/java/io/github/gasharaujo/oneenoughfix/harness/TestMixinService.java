package io.github.gasharaujo.oneenoughfix.harness;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.platform.container.ContainerHandleVirtual;
import org.spongepowered.asm.launch.platform.container.IContainerHandle;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.service.*;

public final class TestMixinService extends MixinServiceAbstract
        implements IClassProvider, IClassBytecodeProvider, ITransformerProvider, IClassTracker {
    private final IContainerHandle container = new ContainerHandleVirtual("oneenoughfix-regression");
    @Override public String getName() { return "OneEnoughFixRegression"; }
    @Override public boolean isValid() { return true; }
    @Override public MixinEnvironment.Phase getInitialPhase() { return MixinEnvironment.Phase.DEFAULT; }
    @Override public IClassProvider getClassProvider() { return this; }
    @Override public IClassBytecodeProvider getBytecodeProvider() { return this; }
    @Override public ITransformerProvider getTransformerProvider() { return this; }
    @Override public IClassTracker getClassTracker() { return this; }
    @Override public IMixinAuditTrail getAuditTrail() { return null; }
    @Override public Collection<String> getPlatformAgents() { return List.of(); }
    @Override public IContainerHandle getPrimaryContainer() { return container; }
    @Override public InputStream getResourceAsStream(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }
    @Override public URL[] getClassPath() { return new URL[0]; }
    @Override public Class<?> findClass(String name) throws ClassNotFoundException {
        return findClass(name, true);
    }
    @Override public Class<?> findClass(String name, boolean initialize) throws ClassNotFoundException {
        return Class.forName(name, initialize, getClass().getClassLoader());
    }
    @Override public Class<?> findAgentClass(String name, boolean initialize) throws ClassNotFoundException {
        return findClass(name, initialize);
    }
    @Override public ClassNode getClassNode(String name) throws ClassNotFoundException, IOException {
        return getClassNode(name, true);
    }
    @Override public ClassNode getClassNode(String name, boolean transformers) throws ClassNotFoundException, IOException {
        return getClassNode(name, transformers, 0);
    }
    @Override public ClassNode getClassNode(String name, boolean transformers, int flags)
            throws ClassNotFoundException, IOException {
        // Match the real ModLauncher service: raw bytecode retrieval is unsupported.
        if (!transformers) {
            throw new IllegalArgumentException(
                    "ModLauncher service does not currently support retrieval of untransformed bytecode");
        }
        try (InputStream stream = getResourceAsStream(name.replace('.', '/') + ".class")) {
            if (stream == null) throw new ClassNotFoundException(name);
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, flags);
            return node;
        }
    }
    @Override public Collection<ITransformer> getTransformers() { return List.of(); }
    @Override public Collection<ITransformer> getDelegatedTransformers() { return List.of(); }
    @Override public void addTransformerExclusion(String name) {}
    @Override public void registerInvalidClass(String name) {}
    @Override public boolean isClassLoaded(String name) { return false; }
    @Override public String getClassRestrictions(String name) { return ""; }
}
