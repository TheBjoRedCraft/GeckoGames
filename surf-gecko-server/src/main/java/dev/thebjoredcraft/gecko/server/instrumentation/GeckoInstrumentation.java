package dev.thebjoredcraft.gecko.server.instrumentation;

import java.lang.instrument.Instrumentation;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class GeckoInstrumentation {

  private static volatile @Nullable Instrumentation instrumentation;

  private GeckoInstrumentation() {
  }

  public static void setInstrumentation(Instrumentation instr) {
    if (instrumentation != null) {
      throw new IllegalStateException("GeckoInstrumentation has already been initialized");
    }

    instrumentation = instr;
  }

  public static Instrumentation get() {
    Instrumentation inst = instrumentation;

    if (inst == null) {
      throw new IllegalStateException("GeckoInstrumentation has not been initialized yet");
    }

    return inst;
  }
}
