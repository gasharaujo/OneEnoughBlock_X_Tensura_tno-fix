package io.github.gasharaujo.oneenoughfix.mixin;

import io.github.gasharaujo.oneenoughfix.compat.CacheBridge;
import io.github.gasharaujo.oneenoughfix.compat.Compatibility;
import java.util.Optional;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = Compatibility.TARGET, remap = false)
public abstract class ModernTagResolverMixin {
    @Inject(method = Compatibility.MODERN_METHOD, at = @At("HEAD"), cancellable = true,
            require = 1, remap = false)
    private static void oneEnoughFix$resolveTags(@Coerce Object block,
            CallbackInfoReturnable<Optional<Object>> callback) {
        callback.setReturnValue(CacheBridge.resolveByTags(block));
    }
}
