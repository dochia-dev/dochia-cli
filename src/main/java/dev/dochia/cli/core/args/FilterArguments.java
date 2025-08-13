package dev.dochia.cli.core.args;

import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.playbook.api.*;
import dev.dochia.cli.core.util.CommonUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.annotation.AnnotationUtils;
import picocli.CommandLine;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Arguments used to restrict run footprint. The difference between Filter and Ignore arguments
 * is that Filter arguments are focused on input filtering while Ignore are focused on response filtering.
 */
@Singleton
public class FilterArguments {
    /* local caches to avoid recompute */
    static final List<String> PLAYBOOKS_TO_BE_RUN = new ArrayList<>();
    static final List<TestCasePlaybook> SECOND_PHASE_PLAYBOOKS_TO_BE_RUN = new ArrayList<>();
    static final List<TestCasePlaybook> ALL_TEST_CASE_PLAYBOOKS = new ArrayList<>();
    static final List<String> PATHS_TO_INCLUDE = new ArrayList<>();
    private static final String EXCLUDE_FROM_ALL_PLAYBOOKS_MARK = "!";
    private boolean playbooksToBeRunComputed = false;
    private final PrettyLogger logger = PrettyLoggerFactory.getLogger(FilterArguments.class);

    @Inject
    Instance<TestCasePlaybook> playbooks;
    @Inject
    @Getter
    CheckArguments checkArguments;
    @Inject
    @Getter
    ProcessingArguments processingArguments;

    enum FieldType {
        STRING, NUMBER, INTEGER, BOOLEAN
    }

    enum FormatType {
        FLOAT, DOUBLE, INT32, INT64, DATE, DATE_TIME, PASSWORD, BYTE, BINARY, EMAIL, UUID, URI, URL, HOSTNAME, IPV4, IPV6
    }

    @CommandLine.Option(names = {"-P", "--playbooks", "--playbook"}, paramLabel = "<playbook>",
            description = "A comma separated list of playbooks to be run. They can be full or partial Playbook names. All available can be listed using: @|bold dochia list -f|@", split = ",")
    private List<String> suppliedPlaybooks;

    @CommandLine.Option(names = {"-p", "--paths", "--path"}, paramLabel = "<path>",
            description = "A comma separated list of paths to test. If no path is supplied, all paths will be considered. All available paths can be listed using: @|bold dochia list -p -c api.yml|@", split = ",")
    @Setter
    private List<String> paths;

    @CommandLine.Option(names = {"--skip-paths", "--skip-path"}, paramLabel = "<path>",
            description = "A comma separated list of paths to ignore. If no path is supplied, no path will be ignored. All available paths can be listed using: @|bold dochia list -p -c api.yml|@", split = ",")
    private List<String> skipPaths;

    @CommandLine.Option(names = {"--skip-playbooks", "--skip-playbook"}, paramLabel = "<playbook>",
            description = "A comma separated list of playbooks to ignore. They can be full or partial Playbook names. All available can be listed using: @|bold dochia list -f|@", split = ",")
    @Setter
    private List<String> skipPlaybooks;

    @CommandLine.Option(names = {"-X", "--http-methods", "--http-method"}, paramLabel = "<method>",
            description = "A comma separated list of HTTP methods to include. Default: @|bold,underline ${DEFAULT-VALUE}|@", split = ",")
    @Setter
    private List<HttpMethod> httpMethods = HttpMethod.restMethods();

    @CommandLine.Option(names = {"--skip-http-methods", "--skip-http-method"}, paramLabel = "<method>",
            description = "A comma separated list of HTTP methods to skip. Default: @|bold,underline ${DEFAULT-VALUE}|@", split = ",")
    @Setter
    private List<HttpMethod> skippedHttpMethods = Collections.emptyList();

    @CommandLine.Option(names = {"-d", "--dry-run"},
            description = "Simulate a possible run without actually invoking the service. This will print how many tests will actually be executed and with which Playbooks")
    @Getter
    private boolean dryRun;

    @CommandLine.Option(names = {"--field-types", "--field-type"}, paramLabel = "<type>",
            description = "A comma separated list of OpenAPI data types to include. It only supports standard types: @|underline https://swagger.io/docs/specification/data-models/data-types|@", split = ",")
    private List<FieldType> fieldTypes;

    @CommandLine.Option(names = {"--skip-field-types", "--skip-field-type"}, paramLabel = "<type>",
            description = "A comma separated list of OpenAPI data types to skip. It only supports standard types: @|underline https://swagger.io/docs/specification/data-models/data-types|@", split = ",")
    private List<FieldType> skipFieldTypes;

    @CommandLine.Option(names = {"--field-formats", "--field-format"}, paramLabel = "<format>",
            description = "A comma separated list of OpenAPI data formats to include.", split = ",")
    private List<FormatType> fieldFormats;

    @CommandLine.Option(names = {"--skip-field-formats", "--skip-field-format"}, paramLabel = "<format>",
            description = "A comma separated list of OpenAPI data formats to skip.", split = ",")
    private List<FormatType> skipFieldFormats;

    @CommandLine.Option(names = {"--skip-fields", "--skip-field"}, paramLabel = "<field>",
            description = "A comma separated list of fields that will be skipped by replacement Playbooks like @|bold EmptyStringsInFields|@, @|bold NullValuesInFields|@, etc. " +
                    "If the field name starts with @|bold !|@ the field will be skipped by @|bold all|@ playbooks. ", split = ",")
    private List<String> skipFields;

    @CommandLine.Option(names = {"--skip-headers", "--skip-header"}, paramLabel = "<header>",
            description = "A comma separated list of headers that will be skipped by all Playbooks", split = ",")
    private List<String> skipHeaders;

    @CommandLine.Option(names = {"--skip-deprecated-operations"},
            description = "Skip deprecated API operations. Default: @|bold,underline ${DEFAULT-VALUE}|@")
    @Getter
    private boolean skipDeprecated;

    @CommandLine.Option(names = {"-t", "--tags", "--tag"}, paramLabel = "<tag>",
            description = "A comma separated list of tags to include. If no tag is supplied, all tags will be considered. All available tags can be listed using: @|bold dochia list --tags -c api.yml|@", split = ",")
    private List<String> tags;

    @CommandLine.Option(names = {"--skip-tags", "--skip-tag"}, paramLabel = "<tag>",
            description = "A comma separated list of tags to ignore. If no tag is supplied, no tag will be ignored. All available tags can be listed using: @|bold dochia list --tags -c api.yml|@", split = ",")
    private List<String> skipTags;

    private Map<String, List<String>> skipPathPlaybooks = new HashMap<>();

    /**
     * Gets the list of fields to skip during processing. If the list is not set, an empty list is returned.
     *
     * @return the list of fields to skip, or an empty list if not set
     */
    public List<String> getSkipFields() {
        return Optional.ofNullable(this.skipFields).orElse(Collections.emptyList());
    }

    /**
     * Return the fields that must be skipped by all playbooks. Fields that must be skipped
     * by all playbooks are marked with {@code !};
     *
     * @return a list of fields that can be skipped by all playbooks
     */
    public List<String> getSkipFieldsToBeSkippedForAllPlaybooks() {
        return this.getSkipFields().stream()
                .filter(field -> field.startsWith(EXCLUDE_FROM_ALL_PLAYBOOKS_MARK))
                .map(field -> field.substring(1))
                .toList();
    }

    /**
     * Gets the list of headers to skip during processing. If the list is not set, an empty list is returned.
     *
     * @return the list of headers to skip, or an empty list if not set
     */
    public List<String> getSkipHeaders() {
        return Optional.ofNullable(this.skipHeaders).orElse(Collections.emptyList());
    }

    /**
     * Creates a list with the field data formats to be skipped based on the supplied {@code --skipFieldFormats} argument.
     *
     * @return the list of field data formats to skip if any supplied, an empty list otherwise
     */
    public List<String> getSkipFieldFormats() {
        return mapToString(Optional.ofNullable(this.skipFieldFormats).orElse(Collections.emptyList()));
    }

    /**
     * Creates a list with the field data formats to be included based on the supplied {@code --fieldFormats} argument.
     *
     * @return the list of field data formats to include if any supplied, an empty list otherwise
     */
    public List<String> getFieldFormats() {
        return mapToString(Optional.ofNullable(this.fieldFormats).orElse(Collections.emptyList()));
    }

    /**
     * Creates a list with the field data types to be skipped based on the supplied {@code --skipFieldTypes} argument.
     *
     * @return the list of field data types to skip if any supplied, an empty list otherwise
     */
    public List<String> getSkipFieldTypes() {
        return mapToString(Optional.ofNullable(this.skipFieldTypes).orElse(Collections.emptyList()));
    }

    /**
     * Creates a list with the field data types to be included based on the supplied {@code --fieldTypes} argument.
     *
     * @return the list of field type to include if any supplied, an empty list otherwise
     */
    public List<String> getFieldTypes() {
        return mapToString(Optional.ofNullable(this.fieldTypes).orElse(Collections.emptyList()));
    }

    /**
     * Creates a list with the playbooks to be skipped based on the supplied {@code --skipPlaybooks} argument.
     *
     * @return the list of playbooks to skip if any supplied, an empty list otherwise
     */
    public List<String> getSkipPlaybooks() {
        List<String> skipPlaybookListWithPathPlaybooks = Optional.ofNullable(this.skipPlaybooks).orElse(Collections.emptyList());
        skipPathPlaybooks = skipPlaybookListWithPathPlaybooks.stream()
                .filter(string -> string.contains("="))
                .map(String::trim)
                .map(string -> string.split("=", 2))
                .collect(Collectors.toMap(stringArray -> stringArray[0],
                        stringArray -> Stream.of(stringArray[1].split(","))
                                .map(String::trim)
                                .toList()));
        return skipPlaybookListWithPathPlaybooks.stream()
                .filter(string -> !string.contains("="))
                .toList();
    }

    /**
     * Creates a list with the paths to be skipped based on the supplied {@code --skipPaths} argument.
     *
     * @return the list of paths to skip if any supplied, an empty list otherwise
     */
    public List<String> getSkipPaths() {
        return Optional.ofNullable(this.skipPaths).orElse(Collections.emptyList());
    }

    /**
     * Creates a list with the playbooks to be included based on the supplied {@code --playbooks} argument.
     * If no playbook is explicitly supplied, ALL playbooks will be considered.
     *
     * @return the list of playbooks to include if any supplied, an empty list otherwise
     */
    public List<String> getSuppliedPlaybooks() {
        return Optional.ofNullable(this.suppliedPlaybooks).orElse(Collections.emptyList());
    }

    /**
     * Creates a list of tags to be included based on the supplied {@code --tags} argument.
     * If no tag is supplied, all tags will be considered.
     *
     * @return the list of tags to include if any supplied, or an empty list otherwise
     */
    public List<String> getTags() {
        return Optional.ofNullable(this.tags).orElse(Collections.emptyList());
    }

    /**
     * Creates a list of tags to be skipped based on the supplied {@code --skipTags} argument.
     * If no tag is supplied, all tags will be considered.
     *
     * @return the list of tags to skip if any supplied, or an empty list otherwise
     */
    public List<String> getSkippedTags() {
        return Optional.ofNullable(this.skipTags).orElse(Collections.emptyList());
    }

    /**
     * Creates a list with the paths to be included based on the supplied {@code --paths} argument.
     * If no path is explicitly supplied, ALL playbooks will be considered.
     *
     * @return the list of paths to include if any supplied, or an empty list otherwise
     */
    public List<String> getPaths() {
        return Optional.ofNullable(this.paths).orElse(Collections.emptyList());
    }

    /**
     * Returns the playbooks to be run initially as a group as list of {@code Playbook}.
     *
     * @return a list of playbooks to be run in phase 1
     */
    public List<TestCasePlaybook> getFirstPhasePlaybooksAsPlaybooks() {
        return this.getAllRegisteredPlaybooks().stream()
                .filter(playbook -> this.getFirstPhasePlaybooksForPath().contains(playbook.toString()))
                .toList();
    }

    /**
     * Excludes playbooks that are meant to be skipped for all the provided http methods list.
     *
     * @param httpMethods the list of http methods
     * @return a filtered list with playbooks that can be run against at least one of the provided http method
     */
    public List<TestCasePlaybook> filterOutPlaybooksNotMatchingHttpMethodsAndPath(Set<HttpMethod> httpMethods, String path) {
        return this.getFirstPhasePlaybooksAsPlaybooks().stream()
                .filter(playbook -> !new HashSet<>(playbook.skipForHttpMethods()).containsAll(httpMethods))
                .filter(playbook -> Optional.ofNullable(skipPathPlaybooks.get(path))
                        .orElse(List.of())
                        .stream()
                        .noneMatch(string -> playbook.toString().contains(string)))
                .toList();
    }

    /**
     * Returns the playbooks to be run initially as a list of playbook names.
     *
     * @return a list of playbooks to be run in phase 1
     */
    public List<String> getFirstPhasePlaybooksForPath() {
        if (!playbooksToBeRunComputed) {
            PLAYBOOKS_TO_BE_RUN.clear();
            List<String> allowedPlaybooks = processSuppliedPlaybooks();
            allowedPlaybooks = this.removeSkippedPlaybooksGlobally(allowedPlaybooks);
            allowedPlaybooks = this.removeSpecialPlaybooks(allowedPlaybooks);
            allowedPlaybooks = this.removeBasedOnTrimStrategy(allowedPlaybooks);
            allowedPlaybooks = this.removeBasedOnSanitizationStrategy(allowedPlaybooks);

            PLAYBOOKS_TO_BE_RUN.addAll(allowedPlaybooks);
            playbooksToBeRunComputed = true;
        }

        //second phase playbooks are removed
        PLAYBOOKS_TO_BE_RUN.removeIf(this.getSecondPhasePlaybooks().stream().map(Object::toString).toList()::contains);

        return PLAYBOOKS_TO_BE_RUN;
    }

    /**
     * Returns a list of playbooks to be run as a group in phase 2.
     *
     * @return a list of playbooks to be run in phase 2
     */
    public List<TestCasePlaybook> getSecondPhasePlaybooks() {
        if (SECOND_PHASE_PLAYBOOKS_TO_BE_RUN.isEmpty()) {
            List<String> secondPhasePlaybooksAsString = this.filterPlaybooksByAnnotationWhenCheckArgumentSupplied(true, StatefulPlaybook.class);
            List<String> playbooksExcludingSkipped = secondPhasePlaybooksAsString.stream().filter(Predicate.not(this.getSkipPlaybooks()::contains))
                    .filter(playbook -> this.getSuppliedPlaybooks().isEmpty() || this.getSuppliedPlaybooks().contains(playbook))
                    .toList();
            SECOND_PHASE_PLAYBOOKS_TO_BE_RUN.addAll(this.getAllRegisteredPlaybooks().stream()
                    .filter(playbook -> playbooksExcludingSkipped.contains(playbook.toString()))
                    .toList());
        }
        if (containsOnlySpecialPlaybooks(this.getSuppliedPlaybooks())) {
            return Collections.emptyList();
        }
        return List.copyOf(SECOND_PHASE_PLAYBOOKS_TO_BE_RUN);
    }

    private List<String> removeSpecialPlaybooks(List<String> allowedPlaybooks) {
        if (containsOnlySpecialPlaybooks(allowedPlaybooks)) {
            return allowedPlaybooks;
        }
        List<String> specialPlaybooks = this.filterPlaybooksByAnnotationWhenCheckArgumentSupplied(true, SpecialPlaybook.class);

        return allowedPlaybooks.stream().filter(playbook -> !specialPlaybooks.contains(playbook)).toList();
    }

    private boolean containsOnlySpecialPlaybooks(List<String> candidates) {
        List<String> specialPlaybooks = this.filterPlaybooksByAnnotationWhenCheckArgumentSupplied(true, SpecialPlaybook.class);

        return !candidates.isEmpty() && new HashSet<>(specialPlaybooks).containsAll(candidates);
    }


    /**
     * Returns a list with ALL registered playbooks.
     *
     * @return a list with all the playbooks
     */
    public List<TestCasePlaybook> getAllRegisteredPlaybooks() {
        if (ALL_TEST_CASE_PLAYBOOKS.isEmpty()) {
            List<String> interimPlaybooksList = playbooks.stream().map(Object::toString).toList();
            interimPlaybooksList = this.removeBasedOnTrimStrategy(interimPlaybooksList);

            List<String> finalPlaybooksList = this.removeBasedOnSanitizationStrategy(interimPlaybooksList);

            ALL_TEST_CASE_PLAYBOOKS.addAll(playbooks.stream().filter(playbook -> finalPlaybooksList.contains(playbook.toString()))
                    .sorted(Comparator.comparing(playbook -> playbook.getClass().getSimpleName())).toList());
        }

        return ALL_TEST_CASE_PLAYBOOKS;
    }

    List<String> removeBasedOnSanitizationStrategy(List<String> currentPlaybooks) {
        Class<? extends Annotation> filterAnnotation = processingArguments.isSanitizeFirst() ? ValidateAndSanitize.class : SanitizeAndValidate.class;
        List<String> trimPlaybooks = this.filterPlaybooksByAnnotationWhenCheckArgumentSupplied(true, filterAnnotation);

        return currentPlaybooks.stream().filter(playbook -> !trimPlaybooks.contains(playbook)).toList();
    }

    List<String> removeBasedOnTrimStrategy(List<String> currentPlaybooks) {
        Class<? extends Annotation> filterAnnotation = processingArguments.getEdgeSpacesStrategy() == ProcessingArguments.TrimmingStrategy.TRIM_AND_VALIDATE
                ? ValidateAndTrim.class : TrimAndValidate.class;
        List<String> trimPlaybooks = this.filterPlaybooksByAnnotationWhenCheckArgumentSupplied(true, filterAnnotation);

        return currentPlaybooks.stream().filter(playbook -> !trimPlaybooks.contains(playbook)).toList();
    }

    private List<String> processSuppliedPlaybooks() {
        List<String> initialPlaybooksList = this.constructPlaybooksListFromCheckArguments();

        if (!this.getSuppliedPlaybooks().isEmpty()) {
            List<String> suppliedPlaybookNames = suppliedPlaybooks.stream().map(String::trim).toList();
            initialPlaybooksList = initialPlaybooksList.stream()
                    .filter(playbook -> suppliedPlaybookNames.stream().anyMatch(playbook::contains))
                    .toList();
        }

        return initialPlaybooksList;
    }

    private List<String> constructPlaybooksListFromCheckArguments() {
        List<String> finalList = new ArrayList<>();
        finalList.addAll(this.filterPlaybooksByAnnotationWhenCheckArgumentSupplied(checkArguments.isCheckFields(), FieldPlaybook.class));
        finalList.addAll(this.filterPlaybooksByAnnotationWhenCheckArgumentSupplied(checkArguments.isCheckHeaders(), HeaderPlaybook.class));
        finalList.addAll(this.filterPlaybooksByAnnotationWhenCheckArgumentSupplied(checkArguments.isCheckHttp(), BodyPlaybook.class));

        if (finalList.isEmpty()) {
            finalList = playbooks.stream().map(Object::toString).toList();
        }

        finalList = this.filterByAnnotationAndIncludeArgument(checkArguments.isIncludeEmojis(), EmojiPlaybook.class, finalList);

        return finalList;
    }

    private List<String> filterByAnnotationAndIncludeArgument(boolean includeArgument, Class<? extends Annotation> annotation, List<String> currentPlaybooks) {
        if (includeArgument) {
            return currentPlaybooks;
        }

        return currentPlaybooks.stream()
                .filter(playbookStr -> playbooks.stream()
                        .noneMatch(playbook -> AnnotationUtils.findAnnotation(playbook.getClass(), annotation) != null
                                && playbook.toString().equals(playbookStr)))
                .toList();
    }

    private List<String> filterPlaybooksByAnnotationWhenCheckArgumentSupplied(boolean checkArgument, Class<? extends Annotation> annotation) {
        if (checkArgument) {
            return playbooks.stream()
                    .filter(playbook -> AnnotationUtils.isAnnotationDeclaredLocally(annotation, playbook.getClass()))
                    .map(Object::toString)
                    .toList();
        }
        return Collections.emptyList();
    }

    private List<String> removeSkippedPlaybooksGlobally(List<String> allowedPlaybooks) {
        List<String> playbooksToExclude = this.getSkipPlaybooks();

        return allowedPlaybooks.stream()
                .filter(playbook ->
                        playbooksToExclude.stream().noneMatch(playbook::contains))
                .toList();
    }

    /**
     * Configures only one playbook to be run.
     *
     * @param specialPlaybook the name of the special playbook to be run
     */
    public void customFilter(String specialPlaybook) {
        this.suppliedPlaybooks = List.of(specialPlaybook);
        this.paths = Collections.emptyList();
        this.skipPlaybooks = new ArrayList<>();
        this.skipPaths = Collections.emptyList();
        this.httpMethods = HttpMethod.restMethods();
        this.dryRun = false;
    }


    /**
     * Determines whether the current run includes the {@code Linter} playbook.
     * <p>
     * This is useful to distinguish whether a linting-specific analysis will be performed.
     *
     * @return {@code true} if the supplied playbooks include "Linter", {@code false} otherwise
     */
    public boolean isLinting() {
        return this.getSuppliedPlaybooks().stream()
                .anyMatch(playbook -> playbook.equalsIgnoreCase("Linter"));
    }

    /**
     * Convert list of enums to list of strings.
     *
     * @param fieldTypes types of fields
     * @param <E>        type of enum
     * @return a list of strings with enum names
     */
    public static <E extends Enum<E>> List<String> mapToString(List<E> fieldTypes) {
        return fieldTypes.stream().map(value -> value.name().toLowerCase(Locale.ROOT)).toList();
    }

    /**
     * Returns the paths to be run considering --paths and --skipPaths arguments.
     *
     * @param openAPI the OpenAPI spec
     * @return a list of paths to be run
     */
    public List<String> getPathsToRun(OpenAPI openAPI) {
        if (PATHS_TO_INCLUDE.isEmpty()) {
            PATHS_TO_INCLUDE.addAll(this.matchSuppliedPathsWithContractPaths(openAPI));
        }

        return PATHS_TO_INCLUDE;
    }

    /**
     * Check if there are any supplied paths and match them against the contract
     *
     * @param openAPI the OpenAPI object parsed from the contract
     * @return the list of paths from the contract matching the supplied list
     */
    public List<String> matchSuppliedPathsWithContractPaths(OpenAPI openAPI) {
        List<String> allSuppliedPaths = new ArrayList<>(matchWildCardPaths(this.getPaths(), openAPI));

        if (this.getPaths().isEmpty()) {
            allSuppliedPaths.addAll(openAPI.getPaths().keySet());
        }

        List<String> allSkippedPaths = matchWildCardPaths(this.getSkipPaths(), openAPI);
        allSuppliedPaths = allSuppliedPaths.stream().filter(path -> !allSkippedPaths.contains(path)).toList();

        logger.debug("Supplied paths before filtering {}", allSuppliedPaths);
        allSuppliedPaths = CommonUtils.filterAndPrintNotMatching(
                allSuppliedPaths,
                path -> openAPI.getPaths().containsKey(path),
                logger,
                "Supplied path is not matching the contract {}",
                Object::toString);
        logger.debug("Supplied paths after filtering {}", allSuppliedPaths);

        return allSuppliedPaths;
    }

    private List<String> matchWildCardPaths(List<String> paths, OpenAPI openAPI) {
        Set<String> allContractPaths = openAPI.getPaths().keySet();

        List<String> result = paths.stream()
                .flatMap(path -> {
                    if (!path.contains("*")) {
                        return Stream.of(path);
                    } else {
                        String regex = path
                                .replace("*", ".*")
                                .replace("{", "\\{")
                                .replace("}", "\\}");
                        Pattern pattern = Pattern.compile(regex);

                        return allContractPaths.stream()
                                .filter(contractPath -> pattern.matcher(contractPath).matches());
                    }
                })
                .distinct()
                .toList();

        logger.debug("Final list of matching wildcard paths: {}", result);
        return result;
    }

    /**
     * Checks if the supplied http method was supplied through arguments.
     *
     * @param method the HTTP method to check
     * @return true if the http method is supplied, false otherwise
     */
    public boolean isHttpMethodSupplied(HttpMethod method) {
        return this.getHttpMethods().contains(method);
    }

    /**
     * Returns the list of HTTP methods to be run excluding the ones that are skipped.
     *
     * @return a list of HTTP methods to be run
     */
    public List<HttpMethod> getHttpMethods() {
        return this.httpMethods
                .stream()
                .filter(httpMethod -> !skippedHttpMethods.contains(httpMethod))
                .toList();
    }

    /**
     * Returns the total number of playbooks that will be run, excluding those that are annotated with
     * {@link TrimAndValidate}, {@link SanitizeAndValidate}, {@link SpecialPlaybook}.
     *
     * @return the total number of playbooks to be run
     */
    public long getTotalPlaybooks() {
        return playbooks.stream()
                .filter(playbook -> AnnotationUtils.findAnnotation(playbook.getClass(), TrimAndValidate.class) == null)
                .filter(playbook -> AnnotationUtils.findAnnotation(playbook.getClass(), SanitizeAndValidate.class) == null)
                .filter(playbook -> AnnotationUtils.findAnnotation(playbook.getClass(), SpecialPlaybook.class) == null)
                .filter(playbook -> AnnotationUtils.findAnnotation(playbook.getClass(), Linter.class) == null)
                .count();
    }

    /**
     * Returns the total number of playbooks that are annotated with {@link Linter}.
     *
     * @return the total number of linters to be run
     */
    public long getTotalLinters() {
        return playbooks.stream()
                .filter(playbook -> AnnotationUtils.findAnnotation(playbook.getClass(), Linter.class) != null)
                .count();
    }
}
