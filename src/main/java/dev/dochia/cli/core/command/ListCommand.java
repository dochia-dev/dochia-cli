package dev.dochia.cli.core.command;

import dev.dochia.cli.core.command.model.*;
import dev.dochia.cli.core.generator.format.api.OpenAPIFormat;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.api.*;
import dev.dochia.cli.core.playbook.special.mutators.api.CustomMutatorConfig;
import dev.dochia.cli.core.playbook.special.mutators.api.Mutator;
import dev.dochia.cli.core.util.ConsoleUtils;
import dev.dochia.cli.core.util.JsonUtils;
import dev.dochia.cli.core.util.OpenApiUtils;
import dev.dochia.cli.core.util.VersionProvider;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.quarkus.arc.Unremovable;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import org.fusesource.jansi.Ansi;
import org.springframework.core.annotation.AnnotationUtils;
import picocli.CommandLine;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * List various information such as: OpenAPI paths, playbooks list, format supported, etc.
 */
@CommandLine.Command(
        name = "list",
        mixinStandardHelpOptions = true,
        usageHelpAutoWidth = true,
        exitCodeOnInvalidInput = 191,
        exitCodeOnExecutionException = 192,
        description = "List Playbooks, OpenAPI paths, OpenAPI formats, Mutators, Custom Mutator Types and FieldFuzzing strategies.",
        exitCodeListHeading = "%n@|bold,underline Exit Codes:|@%n",
        exitCodeList = {"@|bold  0|@:Successful program execution",
                "@|bold 191|@:Usage error: user input for the command was incorrect",
                "@|bold 192|@:Internal execution error: an exception occurred when executing command"},
        footerHeading = "%n@|bold,underline Examples:|@%n",
        footer = {"  List all paths for an OpenAPI contract:",
                "    dochia list --paths -c openapi.yml",
                "", "  List all available playbooks:",
                "    dochia list --playbooks"},
        versionProvider = VersionProvider.class)
@Unremovable
public class ListCommand implements Runnable {
    private final PrettyLogger logger = PrettyLoggerFactory.getConsoleLogger();
    private final List<TestCasePlaybook> playbooksList;

    private final List<String> formats;
    private final List<MutatorEntry> mutators;

    @CommandLine.ArgGroup(multiplicity = "1")
    ListCommandGroups listCommandGroups;
    @CommandLine.Option(names = {"-j", "--json"},
            description = "Output to console in JSON format")
    private boolean json;

    @CommandLine.Mixin
    HelpFullOption helpFullOption;

    /**
     * Constructs a new instance of the {@code ListCommand} class.
     *
     * @param playbooksList an instance containing a list of playbooks, excluding those annotated with {@code ValidateAndTrim} or {@code ValidateAndSanitize}
     * @param formats       an instance containing a list of OpenAPI formats, including their matching formats
     */
    public ListCommand(@Any Instance<TestCasePlaybook> playbooksList, @Any Instance<OpenAPIFormat> formats, @Any Instance<Mutator> mutators) {
        this.playbooksList = playbooksList.stream()
                .filter(playbook -> AnnotationUtils.findAnnotation(playbook.getClass(), ValidateAndTrim.class) == null)
                .filter(playbook -> AnnotationUtils.findAnnotation(playbook.getClass(), ValidateAndSanitize.class) == null)
                .toList();
        this.formats = formats.stream().flatMap(format -> format.matchingFormats().stream()).sorted().toList();
        this.mutators = mutators.stream().map(m -> new MutatorEntry(m.getClass().getSimpleName(), m.description())).sorted().toList();
    }

    @Override
    public void run() {
        if (listCommandGroups.listPlaybooksGroup != null && listCommandGroups.listPlaybooksGroup.playbooks) {
            listPlaybooks();
        }
        if (listCommandGroups.listStrategiesGroup != null && listCommandGroups.listStrategiesGroup.strategies) {
            listPlaybookStrategies();
        }
        if (listCommandGroups.listContractOptions != null && listCommandGroups.listContractOptions.paths) {
            listContractPaths();
        }
        if (listCommandGroups.listFormats != null && listCommandGroups.listFormats.formats) {
            listFormats();
        }

        if (listCommandGroups.listMutatorsGroup != null && listCommandGroups.listMutatorsGroup.mutators) {
            listMutators();
        }

        if (listCommandGroups.listMutatorsGroup != null && listCommandGroups.listMutatorsGroup.customMutatorTypes) {
            listMutatorsTypes();
        }
    }

    void listFormats() {
        if (json) {
            PrettyLoggerFactory.getConsoleLogger().noFormat(JsonUtils.GSON.toJson(formats));
        } else {
            logger.noFormat("Registered OpenAPI formats: {}", formats);
        }
    }

    void listMutatorsTypes() {
        if (json) {
            PrettyLoggerFactory.getConsoleLogger().noFormat(JsonUtils.GSON.toJson(CustomMutatorConfig.Type.values()));
        } else {
            String message = ansi().bold().fg(Ansi.Color.GREEN).a("dochia has {} registered custom mutator types: {}").reset().toString();
            logger.noFormat(message, CustomMutatorConfig.Type.values().length, Arrays.toString(CustomMutatorConfig.Type.values()));
        }
    }

    void listMutators() {
        if (json) {
            PrettyLoggerFactory.getConsoleLogger().noFormat(JsonUtils.GSON.toJson(mutators));
        } else {
            String message = ansi().bold().fg(Ansi.Color.GREEN).a("dochia has {} registered Mutators:").reset().toString();
            logger.noFormat(message, mutators.size());
            mutators.stream()
                    .map(m -> " ◼ " + ansi().bold().fg(Ansi.Color.GREEN).a(m.name()).reset() + " - " + m.description())
                    .forEach(logger::noFormat);
        }
    }

    void listContractPaths() {
        try {
            OpenAPI openAPI = OpenApiUtils.readOpenApi(listCommandGroups.listContractOptions.contract);
            if (listCommandGroups.listContractOptions.path == null) {
                this.listAllPaths(openAPI);
            } else {
                this.listPath(openAPI, listCommandGroups.listContractOptions.path);
            }
        } catch (IOException e) {
            logger.debug("Exception while reading contract!", e);
            logger.error("Error while reading contract. The file might not exist or is not reachable: {}. Error message: {}",
                    listCommandGroups.listContractOptions.contract, e.getMessage());
        }
    }

    void listPath(OpenAPI openAPI, String path) {
        PathItem pathItem = openAPI.getPaths().entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(path))
                .map(Map.Entry::getValue)
                .findFirst().orElseThrow();
        List<PathDetailsEntry.OperationDetails> operations = new ArrayList<>();
        for (Map.Entry<PathItem.HttpMethod, Operation> entry : pathItem.readOperationsMap().entrySet()) {
            PathItem.HttpMethod httpMethod = entry.getKey();
            Operation operation = entry.getValue();
            Set<String> responseCodes = OpenApiUtils.responseCodesFromOperations(operation);
            Set<String> headers = OpenApiUtils.headersFromOperation(new String[]{""}, operation);
            Set<String> queryParams = OpenApiUtils.queryParametersFromOperations(operation);

            PathDetailsEntry.OperationDetails operationDetails = PathDetailsEntry.OperationDetails.builder()
                    .operationId(operation.getOperationId())
                    .queryParams(queryParams)
                    .responses(responseCodes)
                    .headers(headers)
                    .httpMethod(httpMethod.name())
                    .build();

            operations.add(operationDetails);
        }
        PathDetailsEntry pathDetailsEntry = PathDetailsEntry.builder()
                .path(path)
                .operations(operations)
                .build();

        if (json) {
            logger.noFormat(JsonUtils.GSON.toJson(pathDetailsEntry));
        } else {
            logger.noFormat(ansi().bold().a(path + ":").reset().toString());
            for (PathDetailsEntry.OperationDetails operation : pathDetailsEntry.getOperations()) {
                logger.noFormat(ConsoleUtils.SEPARATOR);
                logger.noFormat(" ◼ Operation: " + operation.getOperationId());
                logger.noFormat(" ◼ HTTP Method: " + operation.getHttpMethod());
                logger.noFormat(" ◼ Response Codes: " + operation.getResponses());
                logger.noFormat(" ◼ HTTP Headers: " + operation.getHeaders());
                logger.noFormat(" ◼ Query Params: " + operation.getQueryParams());
            }
        }
    }

    private void listAllPaths(OpenAPI openAPI) {
        PathListEntry pathListEntry = createPathEntryList(openAPI);

        if (json) {
            PrettyLoggerFactory.getConsoleLogger().noFormat(JsonUtils.GSON.toJson(pathListEntry));
        } else {
            logger.noFormat("{} paths and {} operations:", pathListEntry.getNumberOfPaths(), pathListEntry.getNumberOfOperations());
            pathListEntry.getPathDetailsList()
                    .stream()
                    .sorted()
                    .map(item -> " ◼ " + item.getPath() + ": " + item.getMethods())
                    .forEach(logger::noFormat);
        }
    }

    private PathListEntry createPathEntryList(OpenAPI openAPI) {
        Map<String, PathItem> filteredPaths = Optional.ofNullable(openAPI.getPaths()).orElse(new io.swagger.v3.oas.models.Paths())
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().readOperationsMap().values()
                        .stream()
                        .anyMatch(operation -> listCommandGroups.listContractOptions.tag == null ||
                                Optional.ofNullable(operation.getTags()).orElse(Collections.emptyList())
                                        .contains(listCommandGroups.listContractOptions.tag)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        int numberOfPaths = filteredPaths.size();
        int numberOfOperations = filteredPaths.values().stream().mapToInt(OpenApiUtils::countPathOperations).sum();
        List<PathListEntry.PathDetails> pathDetailsList = new ArrayList<>();

        filteredPaths
                .forEach((pathName, pathItem) -> {
                    List<HttpMethod> httpMethods = HttpMethod.OPERATIONS.entrySet()
                            .stream()
                            .filter(entry -> entry.getValue().apply(pathItem) != null)
                            .map(Map.Entry::getKey)
                            .toList();

                    pathDetailsList.add(PathListEntry.PathDetails
                            .builder()
                            .methods(httpMethods)
                            .path(pathName)
                            .build());
                });
        return PathListEntry.builder()
                .numberOfPaths(numberOfPaths)
                .numberOfOperations(numberOfOperations)
                .pathDetailsList(pathDetailsList)
                .build();
    }

    void listPlaybookStrategies() {
        logger.noFormat("Registered fieldsPlaybookStrategies: {}", Arrays.asList(PlaybookData.SetFuzzingStrategy.values()));
    }

    void listPlaybooks() {
        List<TestCasePlaybook> fieldTestCasePlaybooks = filterPlaybooks(FieldPlaybook.class);
        List<TestCasePlaybook> headerTestCasePlaybooks = filterPlaybooks(HeaderPlaybook.class);
        List<TestCasePlaybook> httpTestCasePlaybooks = filterPlaybooks(BodyPlaybook.class);

        if (json) {
            List<PlaybookListEntry> playbookEntries = List.of(
                    new PlaybookListEntry().category("Field").playbooks(fieldTestCasePlaybooks),
                    new PlaybookListEntry().category("Header").playbooks(headerTestCasePlaybooks),
                    new PlaybookListEntry().category("Body").playbooks(httpTestCasePlaybooks));
            PrettyLoggerFactory.getConsoleLogger().noFormat(JsonUtils.GSON.toJson(playbookEntries));
        } else {
            String message = ansi().bold().fg(Ansi.Color.GREEN).a("dochia has {} registered playbooks:").reset().toString();
            logger.noFormat(message, playbooksList.size());
            displayPlaybooks(fieldTestCasePlaybooks, FieldPlaybook.class);
            displayPlaybooks(headerTestCasePlaybooks, HeaderPlaybook.class);
            displayPlaybooks(httpTestCasePlaybooks, BodyPlaybook.class);
        }
    }

    List<TestCasePlaybook> filterPlaybooks(Class<? extends Annotation> annotation) {
        return playbooksList.stream()
                .filter(playbook -> AnnotationUtils.findAnnotation(playbook.getClass(), annotation) != null)
                .sorted(Comparator.comparing(Object::toString))
                .toList();
    }

    void displayPlaybooks(List<TestCasePlaybook> testCasePlaybooks, Class<? extends Annotation> annotation) {
        String message = ansi().bold().fg(Ansi.Color.CYAN).a("{} {}:").reset().toString();
        String typeOfPlaybooks = annotation.getSimpleName().replace("Playbook", "");
        logger.noFormat(" ");
        logger.noFormat(message, testCasePlaybooks.size(), typeOfPlaybooks);
        testCasePlaybooks.stream().map(playbook -> " ◼ " + ansi().bold().fg(Ansi.Color.GREEN).a(ConsoleUtils.removeTrimSanitize(playbook.toString())).reset().a(" - " + playbook.description()).reset()).forEach(logger::noFormat);
    }

    static class ListCommandGroups {
        @CommandLine.ArgGroup(exclusive = false, heading = "List OpenAPI Contract Paths%n")
        ListContractOptions listContractOptions;

        @CommandLine.ArgGroup(multiplicity = "1", heading = "List Playbooks%n")
        ListPlaybooksGroup listPlaybooksGroup;

        @CommandLine.ArgGroup(exclusive = false, heading = "List Mutators%n")
        ListMutatorsGroup listMutatorsGroup;

        @CommandLine.ArgGroup(exclusive = false, heading = "List Fields Playbook Strategies%n")
        ListStrategies listStrategiesGroup;

        @CommandLine.ArgGroup(exclusive = false, heading = "List Supported OpenAPI Formats%n")
        ListFormats listFormats;
    }

    static class ListFormats {
        @CommandLine.Option(
                names = {"--formats", "formats"},
                description = "Display all formats supported by dochia generators",
                required = true)
        boolean formats;
    }

    static class ListStrategies {
        @CommandLine.Option(
                names = {"-s", "--fieldsPlaybookStrategies", "fieldsPlaybookStrategies"},
                description = "Display all current registered Fields Fuzzing Strategies",
                required = true)
        boolean strategies;
    }

    static class ListPlaybooksGroup {
        @CommandLine.Option(
                names = {"-f", "--playbooks", "playbooks"},
                description = "Display all current registered Playbooks")
        boolean playbooks;
    }

    static class ListMutatorsGroup {
        @CommandLine.Option(
                names = {"-m", "--mutators", "mutators"},
                description = "Display all current registered Mutators")
        boolean mutators;

        @CommandLine.Option(
                names = {"--cmt", "--customMutatorTypes"},
                description = "Display types supported by the Custom Mutator")
        boolean customMutatorTypes;
    }

    static class ListContractOptions {
        @CommandLine.Option(
                names = {"-p", "--paths", "paths"},
                description = "Display all paths from the OpenAPI contract/spec",
                required = true)
        boolean paths;

        @CommandLine.Option(names = {"--path"},
                description = "A path to display more info")
        private String path;

        @CommandLine.Option(names = {"--tag"},
                description = "Tag to filter paths")
        private String tag;

        @CommandLine.Option(
                names = {"-c", "--contract"},
                description = "The OpenAPI contract/spec",
                required = true)
        String contract;
    }

}
