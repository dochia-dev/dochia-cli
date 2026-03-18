package dev.dochia.cli.core.report;


import com.github.mustachejava.Mustache;
import dev.dochia.cli.core.args.QualityGateArguments;
import dev.dochia.cli.core.args.ReportingArguments;
import dev.dochia.cli.core.context.GlobalContext;
import jakarta.inject.Inject;
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
     * @param reportingArguments          the reporting arguments
     * @param globalContext               the global context
     * @param qualityGateArguments        the quality gate arguments
     * @param executionStatisticsListener the execution statistics listener
     */
    @Inject
    public TestCaseExporterHtmlJsBuckets(ReportingArguments reportingArguments, GlobalContext globalContext,
                                         QualityGateArguments qualityGateArguments,
                                         ExecutionStatisticsListener executionStatisticsListener) {
        super(reportingArguments, globalContext, qualityGateArguments, executionStatisticsListener);
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
