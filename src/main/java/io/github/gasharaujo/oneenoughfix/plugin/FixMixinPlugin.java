package io.github.gasharaujo.oneenoughfix.plugin;

import com.bawnorton.mixinsquared.MixinSquaredBootstrap;
import com.bawnorton.mixinsquared.adjuster.MixinAnnotationAdjusterRegistrar;
import io.github.gasharaujo.oneenoughfix.compat.Compatibility;
import io.github.gasharaujo.oneenoughfix.compat.LegacyTagInjectorAdjuster;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

public final class FixMixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        MixinSquaredBootstrap.init();
        MixinAnnotationAdjusterRegistrar.register(new LegacyTagInjectorAdjuster());
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!Compatibility.TARGET.equals(targetClassName)) return false;
        try {
            var bytecode = MixinService.getService().getBytecodeProvider();
            return Compatibility.needsModernAdapter(
                    bytecode.getClassNode(targetClassName, false),
                    bytecode.getClassNode(Compatibility.ORIGINAL_MIXIN, false));
        } catch (ClassNotFoundException exception) {
            return false; // A future TNO version may remove or rename the old mixin.
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot inspect OneEnoughBlock/TNO compatibility", exception);
        }
    }

    @Override public String getRefMapperConfig() { return null; }
    @Override public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}
    @Override public List<String> getMixins() {
        MixinSquaredBootstrap.reOrderExtensions();
        return List.of();
    }
    @Override public void preApply(String name, ClassNode target, String mixin, IMixinInfo info) {}
    @Override public void postApply(String name, ClassNode target, String mixin, IMixinInfo info) {}
}
