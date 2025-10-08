package dev.dochia.cli.core.args;

import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.OpenApiServerExtractor;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import io.swagger.v3.oas.models.OpenAPI;
import jakarta.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import picocli.CommandLine;

import java.util.List;

/**
 * Holds all arguments related to API details.
 */
@Getter
@Singleton
public class ApiArguments {
    private final PrettyLogger log = PrettyLoggerFactory.getLogger(this.getClass());

    @CommandLine.Option(
            names = {"--max-requests-per-minute"}, paramLabel = "<max>",
            description = "Maximum number of requests per minute; this is useful when APIs have rate limiting implemented. Default: @|bold,underline ${DEFAULT-VALUE}|@",
            defaultValue = "10000")
    private int maxRequestsPerMinute = 10000;

    @CommandLine.Option(
            names = {"--connection-timeout"}, paramLabel = "<timeout>",
            description = "Time period in seconds within which a connection should be established with the server. Default: @|bold,underline ${DEFAULT-VALUE}|@",
            defaultValue = "10")
    private int connectionTimeout = 10;

    @CommandLine.Option(
            names = {"--write-timeout"}, paramLabel = "<timeout>",
            description = "Maximum time of inactivity in seconds between two data packets when sending the request to the server. Default: @|bold,underline ${DEFAULT-VALUE}|@",
            defaultValue = "10")
    private int writeTimeout = 10;

    @CommandLine.Option(
            names = {"--read-timeout"}, paramLabel = "<timeout>",
            description = "Maximum time of inactivity in seconds between two data packets when waiting for the server's response. Default: @|bold,underline ${DEFAULT-VALUE}|@",
            defaultValue = "10")
    private int readTimeout = 10;

    @CommandLine.Option(
            names = {"--user-agent"}, paramLabel = "<user-agent>",
            description = "The user agent to be set in the User-Agent HTTP header. Default: @|bold,underline dochia/${app.version}|@")
    private String userAgent;

    @Setter
    @CommandLine.Option(
            names = {"-c", "--contract"},
            description = "The API contract/spec")
    private String contract;

    @Setter
    @CommandLine.Option(
            names = {"-s", "--server"},
            description = "Base URL of the service")
    private String server;

    @ConfigProperty(name = "quarkus.application.version", defaultValue = "1.0.0")
    String appVersion;

    /**
     * Verifies if the supplied OpenAPI spec is from a local location or a http one.
     *
     * @return true if the contract is from a http location, false otherwise
     */
    public boolean isRemoteContract() {
        return contract != null && contract.startsWith("http");
    }

    /**
     * Validates the required {@code --contract} and {@code --server} arguments are present.
     *
     * @param spec the PicoCli command spec
     */
    public void validateRequired(CommandLine.Model.CommandSpec spec) {
        if (this.contract == null) {
            throw new CommandLine.ParameterException(
                    spec.commandLine(), "Missing required option --contract=<contract>");
        }
    }

    public void validateValidServer(CommandLine.Model.CommandSpec spec, OpenAPI openAPI) {
        String serverFromInput = this.server;

        if (openAPI != null) {
            List<String> servers = OpenApiServerExtractor.getServerUrls(openAPI);
            log.debug("--server not provided. Loaded from OpenAPI: {}", servers);
            servers.stream().findFirst().ifPresent(theServer -> this.server = theServer);
        }

        if (this.server != null && serverFromInput != null) {
            if (this.server.contains("{")) {
                this.server = this.server.replaceAll("\\{[^}]*+}", serverFromInput);
            }

            if (!this.server.startsWith("http")) {
                this.server = serverFromInput + this.server;
            }
        }

        if (this.server == null) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "Missing required option --server=<server>");
        }

        if (!CommonUtils.isValidURL(this.server)) {
            throw new CommandLine.ParameterException(spec.commandLine(),
                    "You must provide a valid <server> URL which must start with http or https");
        }
    }

    /**
     * Returns the full user agent, appending the testId and the recipe name.
     *
     * @param testId the id of the test case being run
     * @param recipe the name of the recipe being run
     * @return a full name for the user agent containing both version data and testId and recipe name
     */
    public String getUserAgent(int testId, String recipe) {
        if (userAgent == null) {
            userAgent = "dochia/" + appVersion;
        }
        return this.userAgent + " (Test " + testId + " - " + recipe + ")";
    }
}
