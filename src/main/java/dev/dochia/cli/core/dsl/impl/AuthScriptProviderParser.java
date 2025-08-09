package dev.dochia.cli.core.dsl.impl;

import dev.dochia.cli.core.dsl.api.Parser;
import dev.dochia.cli.core.exception.DochiaException;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/** Parser used to run the authentication script to supply credentials to the playbooks. */
public class AuthScriptProviderParser implements Parser {
  private final PrettyLogger logger = PrettyLoggerFactory.getLogger(AuthScriptProviderParser.class);
  private long t0 = System.currentTimeMillis();
  private String existingValue;

  @Override
  public String parse(String expression, Map<String, String> context) {
    String script = context.get(Parser.AUTH_SCRIPT);
    int authRefreshInterval = Integer.parseInt(context.getOrDefault(Parser.AUTH_REFRESH, "0"));

    if (authRefreshInterval > 0 && refreshIntervalElapsed(authRefreshInterval)) {
      logger.debug("Refresh interval passed.");
      t0 = System.currentTimeMillis();
      existingValue = runScript(script);
    }

    if (existingValue == null) {
      existingValue = runScript(script);
    }
    return existingValue;
  }

  private String runScript(String script) {
    logger.note("Running script {} to get credentials", script);
    try {
      ProcessBuilder processBuilder = new ProcessBuilder(script);
      processBuilder.redirectErrorStream(true);
      StringBuilder builder = new StringBuilder();

      try (BufferedReader reader = processBuilder.start().inputReader(StandardCharsets.UTF_8)) {
        String line;
        while ((line = reader.readLine()) != null) {
          builder.append(line);
        }
      }

      return builder.toString();
    } catch (Exception e) {
      throw new DochiaException(e);
    }
  }

  private boolean refreshIntervalElapsed(int authRefreshInterval) {
    return (System.currentTimeMillis() - t0) / 1000 >= authRefreshInterval;
  }
}
