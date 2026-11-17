package io.github.gasharaujo.oneenoughfix.compat;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Optional;

/** Lazily connects to TNO's own cache; no third-party code or classes are bundled. */
public final class CacheBridge {
    private CacheBridge() {}

    public static Optional<Object> resolveByTags(Object block) {
        return invokeResolver(ResolverHolder.RESOLVER, block);
    }

    static MethodHandle bindResolver(Object cache) throws ReflectiveOperationException {
        return MethodHandles.publicLookup().findVirtual(cache.getClass(), "resolveByTags",
                MethodType.methodType(Optional.class, Object.class, Object.class)).bindTo(cache);
    }

    @SuppressWarnings("unchecked")
    static Optional<Object> invokeResolver(MethodHandle resolver, Object block) {
        try {
            // TNO explicitly supports a null lookup by querying the block's built-in holder.
            return (Optional<Object>) resolver.invokeExact(block, (Object) null);
        } catch (RuntimeException | Error exception) {
            throw exception;
        } catch (Throwable exception) {
            throw new IllegalStateException("Unable to resolve a OneEnoughBlock tag through TNO", exception);
        }
    }

    private static final class ResolverHolder {
        private static final MethodHandle RESOLVER = initialize();

        private static MethodHandle initialize() {
            try {
                Class<?> compat = Class.forName(
                        "com.tensura_tno.compat.oneenough.OneEnoughBlockCompat",
                        true, CacheBridge.class.getClassLoader());
                return bindResolver(compat.getField("CACHE").get(null));
            } catch (ReflectiveOperationException exception) {
                throw new IllegalStateException("Unsupported Tensura TNO OneEnoughBlock cache API", exception);
            }
        }
    }
}
