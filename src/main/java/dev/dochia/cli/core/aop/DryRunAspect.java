package dev.dochia.cli.core.aop;

import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.args.ReportingArguments;
import dev.dochia.cli.core.playbook.api.DryRun;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.util.CommonUtils;
import dev.dochia.cli.core.util.JsonUtils;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.github.ludovicianul.prettylogger.PrettyLoggerFactory;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Aspect used to suspend dochia logic when running in dryRun mode.
 * The aspect will suppress all calls to the service and any reporting.
 * As Quarkus does not support true AOP, to keep the code as clean as possible @DryRun was used to annotate
 * classes which are required to suspend their execution.
 */
@DryRun
@Interceptor
public class DryRunAspect {

    private final PrettyLogger logger = PrettyLoggerFactory.getConsoleLogger();
    /**
     * Holds number of tests executed per path + http method.
     */
    private final Map<String, Integer> paths = new TreeMap<>();

    @Inject
    FilterArguments filterArguments;

    @Inject
    ReportingArguments reportingArguments;

    private int counter;

    /**
     * Intercepts the startSession from the TestCaseListener.
     *
     * @param context invocation context
     * @return result of the real method
     * @throws Exception if something goes wrong
     */
    public Object startSession(InvocationContext context) throws Exception {
        if (reportingArguments.isJsonOutput()) {
            CommonUtils.setDochiaLogLevel("OFF");
        }
        Object result = context.proceed();
        CommonUtils.setDochiaLogLevel("OFF");
        return result;
    }

    /**
     * Doesn't do anything.
     *
     * @return empty HttpResponse
     */
    public Object dontInvokeService() {
        return HttpResponse.empty();
    }

    /**
     * Prevents test files from being written.
     *
     * @return null
     */
    public Object dontWriteTestCase() {
        return null;
    }

    /**
     * Logic to be executed instead of TestCaseListener.endSession()
     *
     * @return nothing
     */
    public Object endSession() {
        if (reportingArguments.isJsonOutput()) {
            List<DryRunEntry> pathTests = paths.entrySet().stream()
                    .map(entry -> {
                        int splitIndex = entry.getKey().lastIndexOf("_");
                        String path = entry.getKey().substring(0, splitIndex);
                        String httpMethod = entry.getKey().substring(splitIndex + 1);

                        return new DryRunEntry(path, httpMethod, String.valueOf(entry.getValue()));
                    })
                    .toList();
            logger.noFormat(JsonUtils.GSON.toJson(pathTests));
        } else {
            logger.noFormat("\n");
            CommonUtils.setDochiaLogLevel("INFO");
            logger.noFormat("Number of tests that will be run with this configuration: {}", paths.values().stream().reduce(0, Integer::sum));
            paths.forEach((s, integer) -> logger.noFormat(ansi().fgBrightYellow().bold().a(" -> path {}: {} tests").toString(), s, integer));
        }
        return null;
    }

    /**
     * Logic to be executed instead of TestCaseListener.reportXXX methods.
     *
     * @param context invocation context
     * @return nothing
     */
    public Object report(InvocationContext context) {
        Object data = context.getParameters()[1];

        if (data instanceof PlaybookData playbookData) {
            paths.merge(playbookData.getPath() + "_" + playbookData.getMethod(), 1, Integer::sum);
        }
        counter++;
        return null;
    }

    /**
     * Intercepts all calls annotated with DryRun
     *
     * @param context invocation context
     * @return mostly nothing
     * @throws Exception in case something happens
     */
    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        if (!filterArguments.isDryRun()) {
            return context.proceed();
        }

        String methodName = context.getMethod().getName();
        return switch (methodName) {
            case String s when s.startsWith("report") -> report(context);
            case String s when s.startsWith("endSession") -> endSession();
            case String s when s.startsWith("startSession") -> startSession(context);
            case String s when s.startsWith("call") -> dontInvokeService();
            case String s when s.startsWith("writeTestCase") -> dontWriteTestCase();
            case String s when s.startsWith("getErrors") ||
                    s.startsWith("initReportingPath") ||
                    s.startsWith("renderFuzzingHeader") ||
                    s.startsWith("notifySummaryObservers") -> 0;
            default -> context.proceed();
        };
    }
}
