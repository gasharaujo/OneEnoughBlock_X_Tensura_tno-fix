package io.github.gasharaujo.oneenoughfix.compat;

import java.util.List;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.util.Annotations;

/** Bytecode inspection only: never initializes Minecraft registries during mixin preparation. */
public final class Compatibility {
    public static final String TARGET = "com.sighs.oneenoughblock.init.BlockReplacementCache";
    public static final String ORIGINAL_MIXIN =
            "com.tensura_tno.mixin.client.OneEnoughBlockReplacementCacheMixin";
    public static final String HANDLER = "tensuraTno$resolveTagsSafely";
    public static final String BLOCK = "Lnet/minecraft/world/level/block/Block;";
    public static final String LOOKUP = "Lnet/minecraft/core/HolderLookup$RegistryLookup;";
    public static final String CALLBACK =
            "Lorg/spongepowered/asm/mixin/injection/callback/CallbackInfoReturnable;";
    public static final String INJECT = "Lorg/spongepowered/asm/mixin/injection/Inject;";
    public static final String LEGACY_HANDLER_DESCRIPTOR = "(" + BLOCK + LOOKUP + CALLBACK + ")V";
    public static final String MODERN_DESCRIPTOR = "(" + BLOCK + ")Ljava/util/Optional;";
    public static final String LEGACY_METHOD =
            "resolveTargetByTags(" + BLOCK + LOOKUP + ")Ljava/util/Optional;";
    public static final String MODERN_METHOD = "resolveTargetByTags" + MODERN_DESCRIPTOR;

    private Compatibility() {}

    public static boolean isLegacyHandler(MethodNode method) {
        return method.name.endsWith(HANDLER) && LEGACY_HANDLER_DESCRIPTOR.equals(method.desc);
    }

    public static boolean needsModernAdapter(ClassNode target, ClassNode originalMixin) {
        boolean modernTarget = target.methods.stream().anyMatch(method ->
                method.name.equals("resolveTargetByTags") && method.desc.equals(MODERN_DESCRIPTOR));
        if (!modernTarget) return false;
        return originalMixin.methods.stream().anyMatch(method -> {
            if (!isLegacyHandler(method)) return false;
            AnnotationNode injection = Annotations.getVisible(method, org.spongepowered.asm.mixin.injection.Inject.class);
            if (injection == null) return false;
            List<String> selectors = Annotations.getValue(injection, "method");
            return selectors != null && (selectors.contains("resolveTargetByTags")
                    || selectors.contains(LEGACY_METHOD));
        });
    }
}
