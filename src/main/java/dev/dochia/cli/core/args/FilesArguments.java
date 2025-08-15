package dev.dochia.cli.core.args;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import dev.dochia.cli.core.exception.DochiaException;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import org.fusesource.jansi.Ansi;
import picocli.CommandLine;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * Holds all arguments related to different files used, such as: headers, reference data, etc.
 */
@Singleton
public class FilesArguments {
    private static final String ALL = "all";
    private final PrettyLogger log = PrettyLoggerFactory.getLogger(this.getClass());
    private Map<String, Map<String, Object>> headers;
    private Map<String, Map<String, Object>> queryParams;
    private Map<String, Map<String, Object>> refData;
    private List<String> urlParams;
    private List<String> pathsOrder;

    @Getter
    private Set<String> errorLeaksKeywordsList;
    @Getter
    private Properties playbookConfigProperties;
    @Getter

    @CommandLine.Option(names = {"--error-keywords", "-K"}, paramLabel = "<file>",
            description = "A properties file with error leaks keywords that will be used when processing responses to detect potential error leaks. If one of these keyword is found, the test case will be marked as error")
    @Setter
    private File errorLeaksKeywords;

    @CommandLine.Option(names = {"--playbooks-config"}, paramLabel = "<file>",
            description = "A properties file with Playbooks configuration that changes default Playbook's expected HTTP response codes. Configuration keys are prefixed with the Playbook name")
    @Setter
    private File playbooksConfig;

    @CommandLine.Option(names = {"--url-params"},
            description = "A comma separated list of @|bold name:value|@ pairs of parameters to be replaced inside the URLs", split = ",")
    @Setter
    private List<String> params;

    @CommandLine.Option(names = {"-U"}, paramLabel = "<name=value>",
            description = "Specifies the url/path params to be replaced in request paths")
    @Setter
    Map<String, Object> urlParamsArguments;

    @CommandLine.Option(names = {"--headers"}, paramLabel = "<file>",
            description = "A YAML file with custom headers that will be passed along with the request. Root elements are paths, while children are header_name:header_value pairs. This can be used to pass oauth or JWT tokens for authentication")
    @Setter
    private File headersFile;

    @CommandLine.Option(names = {"-H", "--header"}, paramLabel = "<name=value>",
            description = "Specifies the headers that will be passed along with the request. When supplied it will be applied to ALL paths. For per-path control, the `--headers` arg must be used")
    @Setter
    Map<String, Object> headersMap;

    @CommandLine.Option(names = {"--query-params"}, paramLabel = "<file>",
            description = "A YAML file with additional query parameters that will be passed along with the request. This can be used to pass non-documented query params")
    @Setter
    private File queryFile;

    @CommandLine.Option(names = {"--execution-order"}, paramLabel = "<file>",
            description = "A file with the order in which the paths will be executed. The paths are on each line. The order from file will drive the execution order")
    @Setter
    private File pathsOrderFile;

    @CommandLine.Option(names = {"-Q"}, paramLabel = "<name=value>",
            description = "Specifies additional query parameters that will be passed along with the request. This can be used to pass non-documented query params. When supplied it will be applied to ALL paths. " +
                    "For per-path control, the `--query-params` argument must be used")
    @Setter
    Map<String, Object> queryParamsArguments;

    @CommandLine.Option(names = {"--reference-data"}, paramLabel = "<file>",
            description = "A YAML file with fields that must have a fixed value in order for requests to succeed. ")
    @Setter
    @Getter
    private File refDataFile;

    @CommandLine.Option(names = {"-R"}, paramLabel = "<name=value>",
            description = "Specifies fields that must have a fixed value in order for requests to succeed. When supplied it will be applied to ALL paths. " +
                    "For per-path control, the `--reference-data` argument must be used")
    @Setter
    Map<String, Object> refDataArguments;

    @CommandLine.Option(names = {"--mutators", "-m"}, paramLabel = "<file>",
            description = "A folder containing custom mutators. This argument is taken in consideration only when using the `dochia random` sub-command")
    @Getter
    @Setter
    private File mutatorsFolder;


    /**
     * Loads all supplied files for --security-playbook-file, --custom-playbook-file, --ref-data, --url-params and --headers.
     *
     * @throws IOException if something happens during file processing
     */
    public void loadConfig() throws IOException {
        loadRefData();
        loadURLParams();
        loadHeaders();
        loadQueryParams();
        loadPlaybooksConfigProperties();
        loadErrorLeaksKeywords();
        loadMutators();
        loadPathsOrder();
    }

    public void loadPathsOrder() throws IOException {
        if (pathsOrderFile == null) {
            log.debug("No paths order file provided");
            return;
        }
        pathsOrder = Files.readAllLines(pathsOrderFile.toPath());

        log.config(Ansi.ansi().bold().a("Paths order file: {}").reset().toString(),
                Ansi.ansi().fg(Ansi.Color.BLUE).a(pathsOrderFile.getCanonicalPath()));
    }

    public void loadMutators() throws IOException {
        if (mutatorsFolder == null) {
            log.debug("No custom Mutators folder provided");
            return;
        }
        log.config(Ansi.ansi().bold().a("Custom Mutators folder: {}").reset().toString(),
                Ansi.ansi().fg(Ansi.Color.BLUE).a(mutatorsFolder.getCanonicalPath()));
    }

    /**
     * Loads the supplied reference data file into a Map.
     *
     * @throws IOException if something happens while reading the file
     */
    public void loadRefData() throws IOException {
        this.refData = this.loadFileAsMapOfMapsOfStrings(refDataFile, "Reference Data");
        this.refData.merge(ALL, Optional.ofNullable(refDataArguments).orElse(Collections.emptyMap()), (existingValue, newValue) -> {
            existingValue.putAll(newValue);
            return existingValue;
        });
    }

    /**
     * Loads the supplied query params file into a Map.
     *
     * @throws IOException if something happens while reading the file
     */
    public void loadQueryParams() throws IOException {
        this.queryParams = this.loadFileAsMapOfMapsOfStrings(queryFile, "Query Params");
        this.queryParams.merge(ALL, Optional.ofNullable(queryParamsArguments).orElse(Collections.emptyMap()), (existingValue, newValue) -> {
            existingValue.putAll(newValue);
            return existingValue;
        });
    }

    /**
     * Loads the supplied url params into a Map.
     */
    public void loadURLParams() {
        urlParams = new ArrayList<>(Optional.ofNullable(this.params).orElse(new ArrayList<>()));
        urlParams.addAll(Optional.ofNullable(urlParamsArguments).orElse(Collections.emptyMap()).entrySet()
                .stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .toList());

        if (!urlParams.isEmpty()) {
            log.config(Ansi.ansi().bold().a("URL parameters: {}").reset().toString(),
                    Ansi.ansi().fg(Ansi.Color.BLUE).a(urlParams));
        }
    }

    /**
     * Loads the supplied headers file into a Map. The method also merges the headers supplied
     * in the file with the ones supplied via the {@code -H} argument.
     *
     * @throws IOException if something happens while reading the file
     */
    public void loadHeaders() throws IOException {
        this.headers = this.loadFileAsMapOfMapsOfStrings(headersFile, "Headers");

        /*Merge headers from file with the ones supplied using the -H argument*/
        if (headersMap != null) {
            headers.merge(ALL, headersMap, (stringStringMap, stringStringMap2) -> {
                Map<String, Object> mergedMap = new HashMap<>(stringStringMap);
                mergedMap.putAll(stringStringMap2);
                return mergedMap;
            });
        }
    }

    /**
     * Returns a list of the --urlParams supplied. The list will contain strings in the {@code name:value} pairs.
     *
     * @return a name:value list of url params to be replaced when calling the service
     */
    public List<String> getUrlParamsList() {
        return Optional.ofNullable(this.urlParams).orElse(Collections.emptyList());
    }

    /**
     * Gets a specific value of the provided url param name provided through the {@code --urlParams} argument.
     * If the url parameter is not found, an empty string will be returned.
     *
     * @param urlParamName the name of the URL parameter
     * @return the value of the url parameter if provided or empty string otherwise
     */
    public String getUrlParam(String urlParamName) {
        return this.getUrlParamsList().stream()
                .filter(param -> param.startsWith(urlParamName))
                .findFirst()
                .orElse(":")
                .split(":", 2)[1];
    }

    /**
     * Tests if a given parameter is not present in the --urlParams argument.
     *
     * @param parameter the parameter to be tested
     * @return true if the parameter is not present in the --urlParams argument, false otherwise
     */
    public boolean isNotUrlParam(String parameter) {
        return this.getUrlParamsList()
                .stream()
                .noneMatch(urlParam -> urlParam.startsWith(parameter));
    }

    /**
     * Replaces the current URL parameters with the {@code --urlParams} arguments supplied.
     * The URL parameters are expected to be included in curly brackets.
     *
     * @param startingUrl the base url: {@code http://localhost:8080/{petId}}
     * @return the initial URL with the parameters replaced with --urlParams supplied values: {@code http://localhost:8080/123}
     */
    public String replacePathWithUrlParams(String startingUrl) {
        for (String nameValueParam : this.getUrlParamsList()) {
            final int name = 0;
            final int value = 1;

            String[] urlParam = nameValueParam.split(":", -1);
            String pathVar = "{" + urlParam[name] + "}";

            startingUrl = startingUrl.replace(pathVar, urlParam[value]);
        }
        return startingUrl;
    }

    /**
     * Returns the header values supplied in the --headers argument.
     * <p>
     * The map keys are the contract paths or {@code all} if headers are applied to all paths.
     *
     * @param path the current path
     * @return a Map representation of the --headers file with paths being the Map keys
     */
    public Map<String, Object> getHeaders(String path) {
        return mergePathAndAll(headers, path);
    }

    /**
     * Returns the reference data from the --refData file supplied as argument.
     * <p>
     * It returns the reference data for both the given path and the {@code all} key.
     *
     * @param currentPath the current API path
     * @return a Map with the supplied --refData
     */
    public Map<String, Object> getRefData(String currentPath) {
        return mergePathAndAll(refData, currentPath);
    }

    /**
     * Returns a map with key-value pairs for all additional query parameters corresponding
     * to the given path as well as the ALL entry.
     *
     * @param path the given path
     * @return a key-value map with all additional query params
     */
    public Map<String, Object> getAdditionalQueryParamsForPath(String path) {
        return mergePathAndAll(queryParams, path);
    }

    /**
     * Returns a list of paths as they are ordered in the pathsOrder file.
     *
     * @return a list of paths in the order they are defined in the pathsOrder file
     */
    public List<String> getPathsOrder() {
        return Optional.ofNullable(pathsOrder).orElse(Collections.emptyList());
    }

    /**
     * Loads the content of the file provided as argument in the {@code --playbooks-config} argument.
     *
     * @throws IOException if something goes wrong
     */
    public void loadPlaybooksConfigProperties() throws IOException {
        playbookConfigProperties = new Properties();
        if (playbooksConfig == null) {
            log.debug("No Playbook custom configuration provided!");
            return;
        }

        log.config(Ansi.ansi().bold().a("Playbooks custom configuration: {}").reset().toString(),
                Ansi.ansi().fg(Ansi.Color.BLUE).a(playbooksConfig));
        try (InputStream stream = new FileInputStream(playbooksConfig)) {
            playbookConfigProperties.load(stream);
        }
    }

    /**
     * Loads the content of the file provided as argument in the {@code --errorLeaksKeywords} argument.
     *
     * @throws IOException if something goes wrong
     */
    public void loadErrorLeaksKeywords() throws IOException {
        errorLeaksKeywordsList = new HashSet<>();
        if (errorLeaksKeywords == null) {
            log.debug("No error leaks keywords file provided!");
            return;
        }

        errorLeaksKeywordsList = new HashSet<>(Files.readAllLines(errorLeaksKeywords.toPath()));

        log.config(Ansi.ansi().bold().a("Error leaks keywords file: {}").reset().toString(),
                Ansi.ansi().fg(Ansi.Color.BLUE).a(errorLeaksKeywords.getCanonicalPath()));
    }


    Map<String, Object> mergePathAndAll(Map<String, Map<String, Object>> collection, String path) {
        Map<String, Object> allMerged = new HashMap<>();
        collection.forEach((k, v) -> {
            if (ALL.equalsIgnoreCase(k)) {
                v.forEach(allMerged::putIfAbsent);
            }
        });

        Map<String, Object> result = new HashMap<>(collection.getOrDefault(path, Map.of()));
        allMerged.forEach(result::putIfAbsent);

        return result;
    }

    private Map<String, Map<String, Object>> loadFileAsMapOfMapsOfStrings(File file, String fileType) throws IOException {
        try {
            if (file == null) {
                log.debug("No {} file provided!", fileType);
                return new HashMap<>();
            }

            log.config(Ansi.ansi().bold().a("{} file: {}").reset().toString(), fileType,
                    Ansi.ansi().fg(Ansi.Color.BLUE).a(file.getCanonicalPath()));
            Map<String, Map<String, Object>> fromFile = parseYaml(file.getCanonicalPath());
            log.debug("{} file loaded successfully: {}", fileType, fromFile);

            return fromFile;
        } catch (IllegalArgumentException e) {
            throw new DochiaException("File format is wrong for " + fileType + ". Make sure you supply a valid yaml file!", e);
        }
    }

    static Map<String, Map<String, Object>> parseYaml(String yaml) throws IOException {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (Reader reader = new InputStreamReader(new FileInputStream(yaml), StandardCharsets.UTF_8)) {
            JsonNode node = mapper.reader().readTree(reader);
            Map<String, Object> paths = mapper.convertValue(node, Map.class);

            for (Map.Entry<String, Object> entry : Optional.ofNullable(paths).orElse(Collections.emptyMap()).entrySet()) {
                Map<String, Object> properties = mapper.convertValue(entry.getValue(), LinkedHashMap.class);
                result.put(entry.getKey(), properties);
            }
        }
        return result;
    }
}
