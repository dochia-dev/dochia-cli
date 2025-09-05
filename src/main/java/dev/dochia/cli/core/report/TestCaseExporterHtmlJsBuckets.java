package dev.dochia.cli.core.report;


import com.github.mustachejava.Mustache;
import dev.dochia.cli.core.args.ReportingArguments;
import dev.dochia.cli.core.context.GlobalContext;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * A concrete implementation of TestCaseExporter for exporting test case results in HTML format with JavaScript and group issues by clusters.
 * This class extends the base TestCaseExporter and provides specific functionality for HTML with JavaScript reporting.
 *
 * @see TestCaseExporter
 */
@Singleton
@Named("htmlJsBuckets")
public class TestCaseExporterHtmlJsBuckets extends TestCaseExporter {
    /**
     * Constructs a new instance of TestCaseExporterHtmlJs with the specified reporting arguments.
     *
     * @param reportingArguments the reporting arguments for configuring the TestCaseExporterHtmlJs
     * @param catsGlobalContext  the global context for the CATS application
     */
    public TestCaseExporterHtmlJsBuckets(ReportingArguments reportingArguments, GlobalContext catsGlobalContext) {
        super(reportingArguments, catsGlobalContext);
    }

    @Override
    public String[] getSpecificHelperFiles() {
        return new String[]{"styles.css", "scripts.js", "chart.js", "styles-buckets.css"};
    }

    @Override
    public ReportingArguments.ReportFormat reportFormat() {
        return ReportingArguments.ReportFormat.BUCKETS;
    }

    @Override
    protected boolean isJavascript() {
        return true;
    }

    @Override
    public Mustache getSummaryTemplate() {
        return mustacheFactory.compile("summary-buckets.mustache");
    }

    @Override
    public String getSummaryReportTitle() {
        return REPORT_HTML;
    }
}
