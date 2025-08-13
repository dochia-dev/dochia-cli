package dev.dochia.cli.core.args;

import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.model.HttpResponse;
import jakarta.inject.Singleton;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Arguments used to ignore response matching. The difference between Filter and Ignore arguments
 * is that Filter arguments are focused on input filtering while Ignore are focused on response filtering.
 */
@Singleton
@Getter
public class IgnoreArguments {
    @CommandLine.Option(names = {"-b", "--blackbox"},
            description = "Ignore all response codes except for @|bold,underline 5XX|@ and report them as @|bold,underline error|@ (equivalent to --filter-codes=\"2xx,4xx\")")
    public void setBlackbox(boolean blackbox) {
        if (blackbox) {
            setFilterResponseCodes(List.of("2xx", "4xx", "501"));
        }
    }

    @CommandLine.Option(names = {"-i", "--ignore-codes"}, paramLabel = "<code>",
            description = "Treat these response codes as success (still reported), even if the Playbook will typically report them as @|bold,underline warn|@ or @|bold,underline error|@", split = ",")
    private List<String> ignoreResponseCodes;

    @CommandLine.Option(names = {"--filter-codes", "--fc"}, paramLabel = "<code>",
            description = "Treat these response codes as success (not reported), even if the Playbook will typically report them as @|bold,underline warn|@ or @|bold,underline error|@ Equivalent to --i <codes> -k", split = ",")
    public void setFilterResponseCodes(List<String> filterResponseCodes) {
        this.ignoreResponseCodes = filterResponseCodes;
        skipReportingForIgnoredArguments = true;
    }

    @CommandLine.Option(names = {"--ignore-undocumented-codes"}, description = "Don't flag undocumented response codes as warnings")
    private boolean ignoreResponseCodeUndocumentedCheck;

    @CommandLine.Option(names = {"--ignore-body-validation"}, description = "Don't validate response body against schema")
    private boolean ignoreResponseBodyCheck;

    @CommandLine.Option(names = {"--ignore-error-leak-check"}, description = "Don't check for error information leaks")
    private boolean ignoreErrorLeaksCheck;

    @CommandLine.Option(names = {"--ignore-content-type-check"}, description = "Don't validate response content type")
    private boolean ignoreResponseContentTypeCheck;


    @CommandLine.Option(names = {"--ignore-size", "--is"}, paramLabel = "<sizes>",
            description = "Treat these response sizes as success (still reported), even if the Playbook will typically report them as @|bold,underline warn|@ or @|bold,underline error|@", split = ",")
    private List<Long> ignoreResponseSizes;

    @CommandLine.Option(names = {"--ignore-words", "--iw"}, paramLabel = "<counts>",
            description = "Treat these word counts as success (still reported), even if the Playbook will typically report them as @|bold,underline warn|@ or @|bold,underline error|@", split = ",")
    private List<Long> ignoreResponseWords;

    @CommandLine.Option(names = {"--ignore-lines", "--il"}, paramLabel = "<counts>",
            description = "Treat these line counts as success (still reported), even if the Playbook will typically report them as @|bold,underline warn|@ or @|bold,underline error|@", split = ",")
    private List<Long> ignoreResponseLines;

    @CommandLine.Option(names = {"--ignore-regex", "--ir"}, paramLabel = "<regex>",
            description = "Treat responses matching this pattern as success (still reported), even if the Playbook will typically report it as @|bold,underline warn|@ or @|bold,underline error|@")
    private String ignoreResponseRegex;

    /*Creating equivalent filtering options for all ignored arguments*/
    @CommandLine.Option(names = {"--filter-size", "--fs"}, paramLabel = "<sizes>",
            description = "Treat these response sizes as success (not reported)", split = ",")
    public void setFilterResponseSize(List<Long> filterResponseSizes) {
        this.ignoreResponseSizes = filterResponseSizes;
        skipReportingForIgnoredArguments = true;
    }

    @CommandLine.Option(names = {"--filter-words", "--fw"}, paramLabel = "<count>",
            description = "Treat these word counts as success (not reported)", split = ",")
    public void setFilterResponseWords(List<Long> filterResponseWords) {
        this.ignoreResponseWords = filterResponseWords;
        skipReportingForIgnoredArguments = true;
    }

    @CommandLine.Option(names = {"--filter-lines", "--fl"}, paramLabel = "<counts>",
            description = "Treat these line counts as success (not reported)", split = ",")
    public void setFilterResponseLines(List<Long> filterResponseLines) {
        this.ignoreResponseLines = filterResponseLines;
        skipReportingForIgnoredArguments = true;
    }

    @CommandLine.Option(names = {"--filter-regex", "--fr"}, paramLabel = "<regex>",
            description = "Treat responses matching this pattern as success (not reported)")
    public void setFilterResponseRegex(String filterResponseRegex) {
        this.ignoreResponseRegex = filterResponseRegex;
        skipReportingForIgnoredArguments = true;
    }
    /*End of filtering options*/


    @CommandLine.Option(names = {"--hide-success"},
            description = "Don't report successful test results. Default: @|bold false|@ ")
    private boolean skipReportingForSuccess;

    @CommandLine.Option(names = {"--hide-warnings"},
            description = "Don't report warning results. Default: @|bold false|@ ")
    private boolean skipReportingForWarnings;


    private boolean skipReportingForIgnoredArguments;


    /**
     * Returns a list with all response codes.
     *
     * @return a list of response codes to ignore
     */
    public List<String> getIgnoreResponseCodes() {
        return Optional.ofNullable(this.ignoreResponseCodes).orElse(Collections.emptyList());
    }

    /**
     * Checks if the supplied response code is ignored.
     *
     * @param receivedResponseCode the response code to check
     * @return true if the response code should be ignored, false otherwise
     */
    public boolean isIgnoredResponseCode(String receivedResponseCode) {
        return StringUtils.isNotBlank(receivedResponseCode) &&
                getIgnoreResponseCodes().stream().anyMatch(code -> ResponseCodeFamily.matchAsCodeOrRange(code, receivedResponseCode));
    }

    /**
     * Checks if the response length received in response are not ignored.
     *
     * @param length the length of the http response
     * @return true if the length of the response does not match the --ignoreResponseSizes argument, false otherwise
     */
    public boolean isNotIgnoredResponseLength(long length) {
        return !Optional.ofNullable(ignoreResponseSizes).orElse(Collections.emptyList()).contains(length);
    }

    /**
     * Checks if the numbers of words received in response are not ignored.
     *
     * @param words the number of words received in the response
     * @return true if the number of words in the response to not match the --ignoreResponseWords argument, false otherwise
     */
    public boolean isNotIgnoredResponseWords(long words) {
        return !Optional.ofNullable(ignoreResponseWords).orElse(Collections.emptyList()).contains(words);
    }

    /**
     * Checks if the numbers of lines received in response are not ignored.
     *
     * @param lines the number of lines received in the response
     * @return true if the number of lines in the response to not match the --ignoreResponseLines argument, false otherwise
     */
    public boolean isNotIgnoredResponseLines(long lines) {
        return !Optional.ofNullable(ignoreResponseLines).orElse(Collections.emptyList()).contains(lines);
    }

    /**
     * Checks the --ignoreResponseRegex against the response body.
     *
     * @param body the http response body
     * @return true if the regex is not found in the body, false otherwise
     */
    public boolean isNotIgnoredRegex(String body) {
        return !body.matches(Optional.ofNullable(ignoreResponseRegex).orElse("dochia_body"));
    }

    /**
     * Check the response is not ignored based on response code, size, number of lines or number of words.
     *
     * @param httpResponse the response received from the service
     * @return true if the response should not be ignored, false otherwise
     */
    public boolean isNotIgnoredResponse(HttpResponse httpResponse) {
        return !this.isIgnoredResponseCode(httpResponse.responseCodeAsString()) &&
                this.isNotIgnoredResponseLength(httpResponse.getContentLengthInBytes()) &&
                this.isNotIgnoredResponseLines(httpResponse.getNumberOfLinesInResponse()) &&
                this.isNotIgnoredResponseWords(httpResponse.getNumberOfWordsInResponse()) &&
                this.isNotIgnoredRegex(httpResponse.getBody());
    }

    public boolean isAnyIgnoreArgumentSupplied() {
        return ignoreResponseCodes != null || ignoreResponseSizes != null || ignoreResponseWords != null ||
                ignoreResponseLines != null || ignoreResponseRegex != null;
    }

    /**
     * Check the response is ignored based on response code, size, number of lines or number of words.
     *
     * @param httpResponse the response received from the service
     * @return true if the response should be ignored, false otherwise
     */
    public boolean isIgnoredResponse(HttpResponse httpResponse) {
        return !this.isNotIgnoredResponse(httpResponse);
    }
}
