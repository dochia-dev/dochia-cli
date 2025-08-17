package dev.dochia.cli.core.playbook.body;

import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.args.FilesArguments;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.executor.SimpleExecutor;
import dev.dochia.cli.core.playbook.executor.SimpleExecutorContext;
import dev.dochia.cli.core.generator.simple.NumberGenerator;
import dev.dochia.cli.core.generator.simple.StringGenerator;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.OpenApiUtils;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.JsonUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Iterates through path variables and sens random resource identifiers. */
@BodyPlaybook
@Singleton
public class RandomResourcesPlaybook implements TestCasePlaybook {
  private final PrettyLogger logger = PrettyLoggerFactory.getLogger(getClass());
  private final SimpleExecutor simpleExecutor;
  private final FilesArguments filesArguments;
  private final TestCaseListener testCaseListener;
  private static final int ITERATIONS = 10;

  /**
   * Creates a new instance.
   *
   * @param simpleExecutor executor used to run the fuzz logic
   * @param filesArguments files argument
   */
  public RandomResourcesPlaybook(
      SimpleExecutor simpleExecutor,
      FilesArguments filesArguments,
      TestCaseListener testCaseListener) {
    this.simpleExecutor = simpleExecutor;
    this.filesArguments = filesArguments;
    this.testCaseListener = testCaseListener;
  }

  @Override
  public void run(PlaybookData data) {
    Set<String> pathVariables = OpenApiUtils.getPathVariables(data.getPath());

    if (pathVariables.isEmpty()) {
      return;
    }

    if (HttpMethod.requiresBody(data.getMethod())) {
      fuzzBodyMethods(data, pathVariables);
    } else {
      fuzzNonBodyMethods(data, pathVariables);
    }
  }

  private void fuzzBodyMethods(PlaybookData data, Set<String> pathVariables) {
    List<String> paths = new ArrayList<>();
    for (int i = 0; i < pathVariables.size() * ITERATIONS; i++) {
      String path = data.getPath();
      for (String pathVar : pathVariables) {
        Object generatedValue = generateNewValue();
        path = path.replace(pathVar, String.valueOf(generatedValue));
      }
      path = path.replaceAll("[{}]", "");
      paths.add(path);
    }
    for (String path : paths) {
      simpleExecutor.execute(
          SimpleExecutorContext.builder()
              .expectedResponseCode(ResponseCodeFamilyPredefined.FOURXX_NF_AND_VALIDATION)
              .playbookData(data)
              .logger(logger)
              .replaceRefData(false)
              .scenario("Send random values in path variables")
              .testCasePlaybook(this)
              .path(path)
              .replaceUrlParams(false)
              .build());
    }
  }

  private void fuzzNonBodyMethods(PlaybookData data, Set<String> pathVariables) {
    Set<String> payloads = new HashSet<>();

    for (int i = 0; i < ITERATIONS; i++) {
      String updatePayload = data.getPayload();
      for (String pathVar : pathVariables) {
        String pathVarValueFromUrlParamsList = filesArguments.getUrlParam(pathVar);
        boolean isPathVarPassedAsUrlParam = StringUtils.isNotBlank(pathVarValueFromUrlParamsList);

        if (isPathVarPassedAsUrlParam) {
          // when path variable is passed as URL param it won't be fuzzed -> won't be part of the
          // payload, so we add it
          updatePayload =
              JsonUtils.replaceNewElement(
                  data.getPayload(), "$", pathVar, pathVarValueFromUrlParamsList);
        }
        Object existingValue = JsonUtils.getVariableFromJson(updatePayload, pathVar);
        if (JsonUtils.isNotSet(String.valueOf(existingValue))) {
          testCaseListener.recordError(
              "OpenAPI spec is missing definition for %s".formatted(pathVar));
          break;
        }
        Object newValue = generateNewValue(existingValue);

        updatePayload = CommonUtils.justReplaceField(updatePayload, pathVar, newValue).json();
      }
      payloads.add(updatePayload);
    }
    if (payloads.size() == 1 && payloads.contains(data.getPayload())) {
      logger.warn(
          "The payload is the same after fuzzing, this might indicate an issue with the contract definition");
      return;
    }
    this.executeTests(data, payloads);
  }

  private void executeTests(PlaybookData data, Set<String> payloads) {
    for (String payload : payloads) {
      simpleExecutor.execute(
          SimpleExecutorContext.builder()
              .expectedResponseCode(ResponseCodeFamilyPredefined.FOURXX_NF_AND_VALIDATION)
              .playbookData(data)
              .logger(logger)
              .replaceRefData(false)
              .scenario("Send random values in path variables")
              .testCasePlaybook(this)
              .payload(payload)
              .replaceUrlParams(false)
              .build());
    }
  }

  private static Object generateNewValue() {
    int randomChoice = CommonUtils.random().nextInt(3);
    int randomLength = CommonUtils.random().nextInt(32);
    return switch (randomChoice) {
      case 0 -> UUID.randomUUID().toString();
      case 1 -> NumberGenerator.generateRandomLong(0, Long.MAX_VALUE);
      default -> RandomStringUtils.secure().nextAlphanumeric(randomLength);
    };
  }

  private static Object generateNewValue(Object value) {
    String valueAsString = String.valueOf(value);

    if (isUuid(valueAsString)) {
      return UUID.randomUUID().toString();
    }
    if (isLong(valueAsString)) {
      long longValue = Long.parseLong(valueAsString);
      return NumberGenerator.generateRandomLong(longValue - 10000L, longValue + 10000L);
    }
    return StringGenerator.generate(
        "[A-Za-z0-9]+", valueAsString.length() - 1, valueAsString.length());
  }

  private static boolean isUuid(String value) {
    try {
      UUID.fromString(value);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private static boolean isLong(String value) {
    try {
      Long.parseLong(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  @Override
  public String description() {
    return "Iterate through each path variable and send random resource identifiers";
  }

  @Override
  public String toString() {
    return ConsoleUtils.sanitizePlaybookName(this.getClass().getSimpleName());
  }

  @Override
  public List<HttpMethod> skipForHttpMethods() {
    return List.of(HttpMethod.HEAD, HttpMethod.TRACE);
  }
}
