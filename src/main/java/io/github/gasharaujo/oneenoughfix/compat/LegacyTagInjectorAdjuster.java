package io.github.gasharaujo.oneenoughfix.compat;

import com.bawnorton.mixinsquared.adjuster.tools.AdjustableAnnotationNode;
import com.bawnorton.mixinsquared.api.MixinAnnotationAdjuster;
import java.util.List;
import org.objectweb.asm.tree.MethodNode;

/** Keeps TNO's legacy callback on its actual two-argument overload, when that overload exists. */
public final class LegacyTagInjectorAdjuster implements MixinAnnotationAdjuster {
    @Override
    public AdjustableAnnotationNode adjust(List<String> targetClassNames, String mixinClassName,
            MethodNode handler, AdjustableAnnotationNode annotation) {
        if (!Compatibility.ORIGINAL_MIXIN.equals(mixinClassName)
                || !targetClassNames.contains(Compatibility.TARGET)
                || !Compatibility.isLegacyHandler(handler)
                || !Compatibility.INJECT.equals(annotation.desc)) {
            return annotation;
        }
        List<String> selectors = annotation.<List<String>>get("method").orElse(List.of());
        if (!selectors.equals(List.of("resolveTargetByTags"))) return annotation;

        // require=0 only handles missing targets. An explicit descriptor prevents a
        // same-name, incompatible overload from passing selection and crashing validation.
        annotation.set("method", List.of(Compatibility.LEGACY_METHOD));
        annotation.set("require", 0);
        return annotation;
    }
}
