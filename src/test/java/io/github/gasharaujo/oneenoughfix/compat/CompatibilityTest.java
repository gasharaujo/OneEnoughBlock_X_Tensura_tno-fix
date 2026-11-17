package io.github.gasharaujo.oneenoughfix.compat;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.util.Annotations;
import static org.junit.jupiter.api.Assertions.*;

class CompatibilityTest {
    private static MethodNode legacyHandler(String selector) {
        MethodNode method = new MethodNode(Opcodes.ACC_STATIC, Compatibility.HANDLER,
                Compatibility.LEGACY_HANDLER_DESCRIPTOR, null, null);
        AnnotationNode injection = new AnnotationNode(Compatibility.INJECT);
        injection.values = new ArrayList<>(List.of("method", List.of(selector), "require", 0));
        method.visibleAnnotations = new ArrayList<>(List.of(injection));
        return method;
    }

    @Test void narrowsTheBrokenSelectorWithoutRemovingTheHandler() {
        MethodNode method = legacyHandler("resolveTargetByTags");
        var annotation = AdjustableAnnotationNode.fromNode(method.visibleAnnotations.getFirst());
        var result = new LegacyTagInjectorAdjuster().adjust(List.of(Compatibility.TARGET),
                Compatibility.ORIGINAL_MIXIN, method, annotation);
        assertSame(annotation, result);
        assertEquals(List.of(Compatibility.LEGACY_METHOD), result.get("method").orElseThrow());
        assertEquals(0, result.get("require").orElseThrow());
        assertEquals(Compatibility.LEGACY_HANDLER_DESCRIPTOR, method.desc);
    }

    @Test void leavesOtherModsAndAlreadyFixedSelectorsAlone() {
        MethodNode method = legacyHandler("resolveTargetByTags");
        var unrelated = AdjustableAnnotationNode.fromNode(method.visibleAnnotations.getFirst());
        new LegacyTagInjectorAdjuster().adjust(List.of(Compatibility.TARGET),
                "another.mod.ReplacementMixin", method, unrelated);
        assertEquals(List.of("resolveTargetByTags"), unrelated.get("method").orElseThrow());

        MethodNode fixed = legacyHandler(Compatibility.LEGACY_METHOD);
        var fixedAnnotation = AdjustableAnnotationNode.fromNode(fixed.visibleAnnotations.getFirst());
        new LegacyTagInjectorAdjuster().adjust(List.of(Compatibility.TARGET),
                Compatibility.ORIGINAL_MIXIN, fixed, fixedAnnotation);
        assertEquals(List.of(Compatibility.LEGACY_METHOD), fixedAnnotation.get("method").orElseThrow());
    }

    @Test void onlyEnablesTheAdapterForTheAffectedApiCombination() {
        ClassNode modern = new ClassNode();
        modern.methods.add(new MethodNode(Opcodes.ACC_STATIC, "resolveTargetByTags",
                Compatibility.MODERN_DESCRIPTOR, null, null));
        ClassNode original = new ClassNode();
        original.methods.add(legacyHandler("resolveTargetByTags"));
        assertTrue(Compatibility.needsModernAdapter(modern, original));

        ClassNode legacy = new ClassNode();
        legacy.methods.add(new MethodNode(Opcodes.ACC_STATIC, "resolveTargetByTags",
                "(" + Compatibility.BLOCK + Compatibility.LOOKUP + ")Ljava/util/Optional;", null, null));
        assertFalse(Compatibility.needsModernAdapter(legacy, original));
        original.methods.clear();
        assertFalse(Compatibility.needsModernAdapter(modern, original));
    }
}
