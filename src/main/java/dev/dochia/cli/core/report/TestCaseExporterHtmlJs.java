package dev.dochia.cli.core.report;

import com.github.mustachejava.Mustache;
import dev.dochia.cli.core.args.ReportingArguments;
import dev.dochia.cli.core.context.GlobalContext;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * A concrete implementation of TestCaseExporter for exporting test case results in HTML format with JavaScript.
 * This class extends the base TestCaseExporter and provides specific functionality for HTML with JavaScript reporting.
 *
 * @see TestCaseExporter
 */
@Singleton
@Named("htmlJs")
public class TestCaseExporterHtmlJs extends TestCaseExporter {

    /**
     * Constructs a new instance of TestCaseExporterHtmlJs with the specified reporting arguments.
     *
     * @param reportingArguments the reporting arguments for configuring the TestCaseExporterHtmlJs
     * @param globalContext      the global context
     */
    public TestCaseExporterHtmlJs(ReportingArguments reportingArguments, GlobalContext globalContext) {
        super(reportingArguments, globalContext);
    }

    @Override
    public String[] getSpecificHelperFiles() {
        return new String[]{"styles.css", "scripts.js", "chart.js"};
    }

    @Override
    public ReportingArguments.ReportFormat reportFormat() {
        return ReportingArguments.ReportFormat.HTML_JS;
    }

    @Override
    protected boolean isJavascript() {
        return true;
    }

    @Override
    public Mustache getSummaryTemplate() {
        return SUMMARY_MUSTACHE;
    }

    @Override
    public String getSummaryReportTitle() {
        return REPORT_HTML;
    }
}
