package dev.dochia.cli.core.report;

import com.github.mustachejava.Mustache;
import dev.dochia.cli.core.args.ReportingArguments;
import dev.dochia.cli.core.context.GlobalContext;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * A concrete implementation of TestCaseExporter for exporting test case results in HTML format only.
 * This class extends the base TestCaseExporter and provides specific functionality for HTML-only reporting.
 *
 * @see TestCaseExporter
 */
@Singleton
@Named("htmlOnly")
public class TestCaseExporterHtmlOnly extends TestCaseExporter {

    /**
     * Constructs a new instance of TestCaseExporterHtmlOnly with the specified reporting arguments.
     *
     * @param reportingArguments the reporting arguments for configuring the TestCaseExporterHtmlOnly
     * @param globalContext      the global context
     */
    public TestCaseExporterHtmlOnly(ReportingArguments reportingArguments, GlobalContext globalContext) {
        super(reportingArguments, globalContext);
    }

    @Override
    public String[] getSpecificHelperFiles() {
        return new String[]{"styles.css"};
    }

    @Override
    public ReportingArguments.ReportFormat reportFormat() {
        return ReportingArguments.ReportFormat.HTML_ONLY;
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
