package io.github.gasharaujo.oneenoughfix.harness;

import io.github.gasharaujo.oneenoughfix.compat.Compatibility;
import java.io.InputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.IMixinTransformer;

/** Applies the actual Sponge Mixin engine to real installed mod bytecode, without opening a world. */
public final class MixinRegressionHarness {
    public static void main(String[] args) throws Exception {
        boolean fixed = args.length > 0 && args[0].equals("fixed");
        MixinBootstrap.init();
        MixinEnvironment environment = MixinEnvironment.getDefaultEnvironment();
        environment.setSide(MixinEnvironment.Side.CLIENT);
        var constructor = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer")
                .getDeclaredConstructor();
        constructor.setAccessible(true);
        IMixinTransformer transformer = (IMixinTransformer) constructor.newInstance();
        Mixins.addConfiguration("regression.tensura.mixins.json");
        if (fixed) Mixins.addConfiguration("oneenoughblock_tensura_tno_fix.mixins.json");

        byte[] original;
        try (InputStream stream = MixinRegressionHarness.class.getClassLoader().getResourceAsStream(
                Compatibility.TARGET.replace('.', '/') + ".class")) {
            if (stream == null) throw new AssertionError("Actual OneEnoughBlock bytecode is missing");
            original = stream.readAllBytes();
        }
        try {
            byte[] result = transformer.transformClass(environment, Compatibility.TARGET, original);
            if (!fixed) throw new AssertionError("Unpatched mixin unexpectedly succeeded");
            ClassNode transformed = new ClassNode();
            new ClassReader(result).accept(transformed, 0);
            boolean adapter = transformed.methods.stream().anyMatch(method ->
                    method.name.contains("oneEnoughFix$resolveTags"));
            boolean originalPublisher = transformed.methods.stream().anyMatch(method ->
                    method.name.contains("tensuraTno$publishRules"));
            boolean originalLegacy = transformed.methods.stream().anyMatch(method ->
                    method.name.contains("tensuraTno$resolveTagsSafely"));
            if (!adapter || !originalPublisher || !originalLegacy)
                throw new AssertionError("Expected modern adapter and original TNO handlers");
            System.out.println("PASS: real Mixin application succeeds and preserves TNO's original handlers.");
        } catch (Throwable error) {
            if (!fixed && hasCause(error, "Invalid descriptor")) {
                System.out.println("PASS: unpatched real bytecode reproduces the original Invalid descriptor crash.");
                return;
            }
            throw error;
        }
    }

    private static boolean hasCause(Throwable error, String message) {
        for (Throwable cause = error; cause != null; cause = cause.getCause()) {
            if (cause.getMessage() != null && cause.getMessage().contains(message)) return true;
        }
        return false;
    }
}
