package dev.thebjoredcraft.gecko.server.instrumentation;

import dev.slne.surf.api.minestom.server.impl.mixin.SurfMixinAgent;
import dev.thebjoredcraft.gecko.server.instrumentation.mixin.InstrumentationMixinService;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import me.lucko.luckperms.minestom.dependencies.LuckPermsAgent;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class GeckoRunner {

  private static final String SURF_MIXIN_CONFIGS_ATTRIBUTE = "Surf-Mixin-Configs";

  private GeckoRunner() {
  }

  public static void agentmain(String agentArgs, Instrumentation instrumentation) {
    try {
      GeckoInstrumentation.setInstrumentation(instrumentation);
      InstrumentationMixinService.setInstrumentation(instrumentation);

      List<String> configs = new ArrayList<>(surfMixinConfigs());
      configs.addAll(SurfMixinAgent.compatibilityConfigs());
      configs.add("mixins.surf-gecko.json");

      SurfMixinAgent.install(instrumentation, configs);

      LuckPermsAgent.agentmain(agentArgs, instrumentation);
    } catch (Throwable e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to bootstrap gecko server instrumentation", e);
    }
  }

  private static List<String> surfMixinConfigs() throws IOException, java.net.URISyntaxException {
    URI location = GeckoRunner.class.getProtectionDomain().getCodeSource().getLocation().toURI();
    Path jarPath = Path.of(location);

    try (JarFile jar = new JarFile(jarPath.toFile())) {
      String attribute = jar.getManifest().getMainAttributes()
          .getValue(SURF_MIXIN_CONFIGS_ATTRIBUTE);

      if (attribute == null || attribute.isBlank()) {
        return List.of();
      }

      return Arrays.stream(attribute.split(","))
          .map(String::trim)
          .filter(s -> !s.isEmpty())
          .toList();
    }
  }
}
