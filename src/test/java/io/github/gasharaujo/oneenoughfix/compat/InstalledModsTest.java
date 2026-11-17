package io.github.gasharaujo.oneenoughfix.compat;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarFile;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.util.Annotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Runs against locally supplied mod JARs; those copyrighted JARs are never shipped. */
class InstalledModsTest {
    private static String requiredProperty(String name) {
        String value = System.getProperty(name);
        assumeTrue(value != null, "Supply -P" + name + "=... to verify the installed mods");
        return value;
    }

    private static ClassNode readClass(String jar, String name) throws Exception {
        try (JarFile file = new JarFile(Path.of(jar).toFile());
                InputStream stream = file.getInputStream(file.getJarEntry(name.replace('.', '/') + ".class"))) {
            ClassNode node = new ClassNode();
            new ClassReader(stream).accept(node, 0);
            return node;
        }
    }

    @Test void installedCrashIsCoveredByTheSelectorRepairAndModernAdapter() throws Exception {
        ClassNode target = readClass(requiredProperty("oneEnoughBlockJar"), Compatibility.TARGET);
        ClassNode original = readClass(requiredProperty("tensuraJar"), Compatibility.ORIGINAL_MIXIN);
        assertTrue(Compatibility.needsModernAdapter(target, original));
        var handler = original.methods.stream().filter(Compatibility::isLegacyHandler).findFirst().orElseThrow();
        var injection = AdjustableAnnotationNode.fromNode(Annotations.getVisible(handler, org.spongepowered.asm.mixin.injection.Inject.class));
        new LegacyTagInjectorAdjuster().adjust(List.of(Compatibility.TARGET),
                Compatibility.ORIGINAL_MIXIN, handler, injection);
        assertEquals(List.of(Compatibility.LEGACY_METHOD), injection.get("method").orElseThrow());
        assertTrue(target.methods.stream().anyMatch(method ->
                ("resolveTargetByTags" + method.desc).equals(Compatibility.LEGACY_METHOD)));
    }

    @Test void bridgeUsesTheRealTnoCacheAndObservesReloads() throws Exception {
        requiredProperty("tensuraJar");
        Class<?> cacheType = Class.forName("com.tensura_tno.compat.oneenough.OneEnoughBlockCache");
        Class<?> registryType = Class.forName("com.tensura_tno.compat.oneenough.OneEnoughBlockCache$BlockRegistry");
        Set<String> registered = Set.of("source", "stone", "dirt");
        Object registry = Proxy.newProxyInstance(registryType.getClassLoader(), new Class<?>[]{registryType},
                (proxy, method, args) -> switch (method.getName()) {
                    case "idOf" -> args[0];
                    case "findBlock" -> registered.contains((String) args[0]) ? args[0] : null;
                    case "hasTag" -> {
                        assertNull(args[1], "Modern API must use TNO's built-in-holder lookup path");
                        yield "source".equals(args[0]) && "test:replaceable".equals(args[2]);
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        Object cache = cacheType.getConstructor(registryType).newInstance(registry);
        var replace = cacheType.getMethod("replaceRules", Map.class, Map.class);
        MethodHandle resolver = CacheBridge.bindResolver(cache);

        replace.invoke(cache, Map.of(), Map.of("test:replaceable", "stone"));
        assertEquals(Optional.of("stone"), CacheBridge.invokeResolver(resolver, "source"));
        assertEquals(Optional.empty(), CacheBridge.invokeResolver(resolver, "dirt"));
        assertEquals(Optional.empty(), CacheBridge.invokeResolver(resolver, null));

        // The adapter must retain the same cache object and see a replacement reload immediately.
        replace.invoke(cache, Map.of(), Map.of("test:replaceable", "dirt"));
        assertEquals(Optional.of("dirt"), CacheBridge.invokeResolver(resolver, "source"));

        LinkedHashMap<String, String> missingTarget = new LinkedHashMap<>();
        missingTarget.put("test:replaceable", "missing");
        replace.invoke(cache, Map.of(), missingTarget);
        assertEquals(Optional.empty(), CacheBridge.invokeResolver(resolver, "source"));
        replace.invoke(cache, Map.of(), Map.of());
        assertEquals(Optional.empty(), CacheBridge.invokeResolver(resolver, "source"));
    }
}
