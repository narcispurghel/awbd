package com.github.narcispurghel.common;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.List;

/**
 * Loads the repo-root {@code .env} for local development.
 *
 * <p>Gradle {@code bootRun} uses the module directory as the working directory (e.g.
 * {@code user-service/}), so we check {@code ..} first, then {@code .} when run from the repo
 * root.
 */
public final class DotenvLoader {

  private static final List<String> REPO_ROOT_CANDIDATES = List.of("..", ".");

  private DotenvLoader() {}

  public static void load() {
    for (String directory : REPO_ROOT_CANDIDATES) {
      Dotenv dotenv = Dotenv.configure().directory(directory).ignoreIfMissing().load();
      if (dotenv.entries().isEmpty()) {
        continue;
      }
      dotenv
        .entries()
        .forEach(entry -> {
          if (!entry.getValue().isBlank()) {
            System.setProperty(entry.getKey(), entry.getValue());
          }
        });
      return;
    }
  }
}
