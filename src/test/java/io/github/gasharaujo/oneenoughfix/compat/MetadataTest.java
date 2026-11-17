package io.github.gasharaujo.oneenoughfix.compat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MetadataTest {
    @Test void authorAndOpenEndedNonMinecraftDependenciesArePublished() throws Exception {
        String metadata;
        try (var stream = getClass().getResourceAsStream("/META-INF/neoforge.mods.toml")) {
            assertNotNull(stream);
            metadata = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertTrue(metadata.contains("authors=\"gasharaujo\""));
        assertTrue(metadata.contains("version=\"1.0.0\""));
        String[] dependencies = metadata.split("\\[\\[dependencies\\.");
        assertEquals(5, dependencies.length);
        for (int index = 1; index < dependencies.length; index++) {
            String dependency = dependencies[index];
            if (dependency.contains("modId=\"minecraft\"")) {
                assertTrue(dependency.contains("versionRange=\"[1.21.1]\""));
            } else {
                assertTrue(dependency.matches("(?s).*versionRange=\"\\[[^\"]+,\\)\".*"),
                        "Non-Minecraft dependency must have no maximum: " + dependency);
            }
        }
    }
}
