package dev.dochia.cli.core.report;

import dev.dochia.cli.core.args.IgnoreArguments;
import dev.dochia.cli.core.args.ReportingArguments;
import dev.dochia.cli.core.context.GlobalContext;
import dev.dochia.cli.core.exception.DochiaException;
import dev.dochia.cli.core.playbook.api.TestCasePlaybook;
import dev.dochia.cli.core.playbook.body.RandomResourcesPlaybook;
import dev.dochia.cli.core.http.HttpMethod;
import dev.dochia.cli.core.http.ResponseCodeFamily;
import dev.dochia.cli.core.http.ResponseCodeFamilyDynamic;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.model.DochiaConfiguration;
import dev.dochia.cli.core.model.HttpRequest;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.TestCase;
import dev.dochia.cli.core.model.TestCaseSummary;
import dev.dochia.cli.core.model.PlaybookData;
import com.google.gson.JsonParser;
import io.github.ludovicianul.prettylogger.PrettyLogger;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.Discriminator;
import io.swagger.v3.oas.models.media.StringSchema;
import jakarta.inject.Inject;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.slf4j.event.Level;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

@QuarkusTest
class TestCaseListenerTest {

    TestCaseListener testCaseListener;

    ExecutionStatisticsListener executionStatisticsListener;
    IgnoreArguments ignoreArguments;
    ReportingArguments reportingArguments;
    @Inject
    GlobalContext globalContext;

    private PrettyLogger logger;
    private TestCasePlaybook testCasePlaybook;
    private TestReportsGenerator testReportsGenerator;


    @BeforeEach
    void setup() {
        logger = Mockito.mock(PrettyLogger.class);
        testCasePlaybook = Mockito.mock(TestCasePlaybook.class);
        reportingArguments = Mockito.mock(ReportingArguments.class);
        testReportsGenerator = Mockito.mock(TestReportsGenerator.class);
        Mockito.when(reportingArguments.getReportFormat()).thenReturn(List.of(ReportingArguments.ReportFormat.HTML_JS));
        executionStatisticsListener = Mockito.mock(ExecutionStatisticsListener.class);
        ignoreArguments = Mockito.mock(IgnoreArguments.class);
        testCaseListener = new TestCaseListener(globalContext, executionStatisticsListener, testReportsGenerator, ignoreArguments, reportingArguments);
        globalContext.getDiscriminators().clear();
        globalContext.getPlaybooksConfiguration().clear();
        globalContext.getPostSuccessfulResponses().clear();
    }

    @AfterEach
    void tearDown() {
        TestCaseListener.TEST.set(0);
    }

    @Test
    void givenAFunction_whenExecutingATestCase_thenTheCorrectContextIsCreatedAndTheTestCaseIsWrittenToFile() {
        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
        }, PlaybookData.builder().build());

        Assertions.assertThat(testCaseListener.testCaseSummaryDetails.getFirst()).isNotNull();
        Mockito.verify(testReportsGenerator).writeTestCase(Mockito.any());
    }

    @Test
    void givenAFunction_whenExecutingATestCaseAndAddingDetails_thenTheDetailsAreCorrectlyAttachedToTheTestCase() {
        Assertions.assertThat(testCaseListener.testCaseSummaryDetails).isEmpty();

        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addScenario(logger, "Given a {} field", "string");
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("POST").build());
            testCaseListener.addResponse(HttpResponse.builder().build());
            testCaseListener.addFullRequestPath("fullPath");
            testCaseListener.addContractPath("path");
            testCaseListener.addExpectedResult(logger, "Should return {}", "2XX");
            testCaseListener.reportWarn(logger, "Warn {} happened", "1");
        }, PlaybookData.builder().build());

        TestCaseSummary testCase = testCaseListener.testCaseSummaryDetails.getFirst();
        Assertions.assertThat(testCase).isNotNull();
        Assertions.assertThat(testCase.getPath()).isEqualTo("path");
        Assertions.assertThat(testCase.getScenario()).isEqualTo("Given a string field");
    }

    @Test
    void givenATestCase_whenExecutingStartAndEndSession_thenTheSummaryAndReportFilesAreCreated() {
        ReflectionTestUtils.setField(testCaseListener, "appName", "dochia");
        testCaseListener.startSession();
        testCaseListener.endSession();

        Mockito.verify(testReportsGenerator, Mockito.times(1)).writeHelperFiles();
        Mockito.verify(testReportsGenerator, Mockito.times(1)).writeSummary(Mockito.anyList(), Mockito.any());
    }

    @Test
    void givenATestCase_whenExecutingItAndAWarnHappens_thenTheWarnIsCorrectlyReportedWithinTheTestCase() {
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);
        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            testCaseListener.reportWarn(logger, "Warn {} happened", "1");
        }, PlaybookData.builder().build());

        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseWarns(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseErrors(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());

        TestCaseSummary testCase = testCaseListener.testCaseSummaryDetails.getFirst();
        Assertions.assertThat(testCase.getResult()).isEqualTo("warning");
        Assertions.assertThat(testCase.getResultDetails()).isEqualTo("Warn 1 happened");
    }

    @ParameterizedTest
    @CsvSource({"401,1", "403,1", "200,0"})
    void shouldIncreaseTheNumberOfAuthErrors(int respCode, int times) {
        HttpResponse response = HttpResponse.builder().body("{}").responseCode(respCode).build();
        prepareTestCaseListenerSimpleSetup(response, () -> testCaseListener.reportError(logger, "Something happened: {}", "bad stuff!"));
        Mockito.verify(executionStatisticsListener, Mockito.times(times)).increaseAuthErrors();
    }

    @Test
    void shouldIncreaseTheNumberOfIOErrors() {
        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            throw new DochiaException("something bad", new IOException());
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseIoErrors();
    }

    @Test
    void shouldNotIncreaseIOErrorsForNonIOException() {
        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            throw new DochiaException("something bad", new IndexOutOfBoundsException());
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(0)).increaseIoErrors();
    }

    @ParameterizedTest
    @CsvSource({"true,false,false", "false,true,false", "true,true,true"})
    void shouldCallInfoInsteadOfWarnWhenIgnoreCodeSupplied(boolean ignoreResponseCodes, boolean ignoreUndocumentedRespCode, boolean ignoreResponseBodyCheck) {
        Mockito.when(ignoreArguments.isIgnoredResponseCode(Mockito.anyString())).thenReturn(ignoreResponseCodes);
        Mockito.when(ignoreArguments.isIgnoreResponseCodeUndocumentedCheck()).thenReturn(ignoreUndocumentedRespCode);
        Mockito.when(ignoreArguments.isIgnoreResponseBodyCheck()).thenReturn(ignoreResponseBodyCheck);

        HttpResponse response = HttpResponse.builder().body("{}").responseCode(200).build();
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("300", "400"));
        Mockito.when(data.getResponses()).thenReturn(Map.of("300", Collections.emptyList()));

        prepareTestCaseListenerSimpleSetup(response, () -> testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.TWOXX));


        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseWarns(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseErrors(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSuccess(Mockito.any());
        MDC.remove(TestCaseListener.ID);
    }

    @Test
    void shouldReportNotMatchingContentType() {
        Mockito.when(ignoreArguments.isIgnoreResponseContentTypeCheck()).thenReturn(false);
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);

        HttpResponse response = HttpResponse.builder().body("{}").responseCode(200).responseContentType("application/json").build();
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getContentTypesByResponseCode("200")).thenReturn(List.of("application/csv"));
        prepareTestCaseListenerSimpleSetup(response, () -> testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.TWOXX));
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseErrors(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseWarns(Mockito.any());
        MDC.remove(TestCaseListener.ID);
    }

    @Test
    void shouldReturnUnexpectedButDocumentedResponseCode() {
        Mockito.when(ignoreArguments.isIgnoreResponseContentTypeCheck()).thenReturn(true);
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);

        HttpResponse response = HttpResponse.builder().body("{}").responseCode(200).build();

        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("200"));

        prepareTestCaseListenerSimpleSetup(response, () -> testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.FOURXX));

        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseWarns(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseErrors(Mockito.any());
        MDC.remove(TestCaseListener.ID);
    }

    @Test
    void shouldSkipInsteadOfInfoWhenSkipReportingIsEnabledAndIgnoredResponseCode() {
        Mockito.when(ignoreArguments.isIgnoredResponse(Mockito.any())).thenReturn(true);
        Mockito.when(ignoreArguments.isSkipReportingForIgnoredArguments()).thenReturn(true);
        HttpResponse response = HttpResponse.builder().body("{}").responseCode(200).build();
        prepareTestCaseListenerSimpleSetup(response, () -> testCaseListener.reportInfo(logger, "Something was good"));

        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());

        MDC.remove(TestCaseListener.ID);
    }

    @Test
    void shouldSkipInfoWhenSkipSuccessIsEnabled() {
        Mockito.when(ignoreArguments.isSkipReportingForSuccess()).thenReturn(true);
        HttpResponse response = HttpResponse.builder().body("{}").responseCode(200).build();
        prepareTestCaseListenerSimpleSetup(response, () -> testCaseListener.reportInfo(logger, "Something was good"));

        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());

        MDC.remove(TestCaseListener.ID);
    }

    @Test
    void shouldSkipWarnWhenSkipWarningsIsEnabled() {
        Mockito.when(ignoreArguments.isSkipReportingForWarnings()).thenReturn(true);
        HttpResponse response = HttpResponse.builder().body("{}").responseCode(200).build();
        prepareTestCaseListenerSimpleSetup(response, () -> testCaseListener.reportWarn(logger, "Something was good"));

        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseWarns(Mockito.any());

        MDC.remove(TestCaseListener.ID);
    }

    @Test
    void shouldStorePostRequestAndRemoveAfterDelete() {
        HttpResponse response = HttpResponse.builder().body("{}").responseCode(200).build();
        PlaybookData data = Mockito.mock(PlaybookData.class);
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("300", "400"));
        Mockito.when(data.getResponses()).thenReturn(Map.of("300", Collections.emptyList()));
        Mockito.when(data.getMethod()).thenReturn(HttpMethod.POST);
        Mockito.when(data.getPath()).thenReturn("/test");
        MDC.put(TestCaseListener.ID, "1");
        testCaseListener.testCaseMap.put("1", new TestCase());
        testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());

        testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(globalContext.getPostSuccessfulResponses()).hasSize(1).containsKey("/test");
        Assertions.assertThat(globalContext.getPostSuccessfulResponses().get("/test")).isNotEmpty();

        Mockito.when(data.getMethod()).thenReturn(HttpMethod.DELETE);
        Mockito.when(data.getPath()).thenReturn("/test/{testId}");
        testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.TWOXX);
        Assertions.assertThat(globalContext.getPostSuccessfulResponses()).hasSize(1).containsKey("/test");
        Assertions.assertThat(globalContext.getPostSuccessfulResponses().get("/test")).isEmpty();

        MDC.remove(TestCaseListener.ID);
        testCaseListener.testCaseMap.clear();
    }

    @Test
    void shouldCallInfoInsteadOfErrorWhenIgnoreCodeSupplied() {
        Mockito.when(ignoreArguments.isIgnoredResponseCode("200")).thenReturn(true);
        prepareTestCaseListenerSimpleSetup(HttpResponse.builder().responseCode(200).build(), () -> testCaseListener.reportError(logger, "Warn"));
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseWarns(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseErrors(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSuccess(Mockito.any());
        MDC.remove(TestCaseListener.ID);
    }

    @Test
    void shouldSkipTestWhenErrorAndIgnoredCodeSuppliedAndSkipReportingEnabled() {
        Mockito.when(ignoreArguments.isIgnoredResponseCode("200")).thenReturn(true);
        Mockito.when(ignoreArguments.isSkipReportingForIgnoredArguments()).thenReturn(true);

        prepareTestCaseListenerSimpleSetup(HttpResponse.builder().responseCode(200).build(), () -> testCaseListener.reportError(logger, "Error"));

        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseWarns(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseErrors(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());
        MDC.remove(TestCaseListener.ID);
    }

    @Test
    void shouldSkipTestWhenWarnAndIgnoredCodeSuppliedAndSkipReportingEnabled() {
        Mockito.when(ignoreArguments.isIgnoredResponseCode("200")).thenReturn(true);
        Mockito.when(ignoreArguments.isSkipReportingForIgnoredArguments()).thenReturn(true);

        prepareTestCaseListenerSimpleSetup(HttpResponse.builder().responseCode(200).build(), () -> testCaseListener.reportWarn(logger, "Warn"));

        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseWarns(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseErrors(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());
        MDC.remove(TestCaseListener.ID);
    }


    @Test
    void givenATestCase_whenExecutingItAndAnErrorHappens_thenTheErrorIsCorrectlyReportedWithinTheTestCase() {
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);

        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> testCaseListener.reportError(logger, "Error {} happened", "1"), PlaybookData.builder().build());

        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseErrors(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseWarns(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());

        TestCaseSummary testCase = testCaseListener.testCaseSummaryDetails.getFirst();
        Assertions.assertThat(testCase.getResult()).isEqualTo(Level.ERROR.toString().toLowerCase(Locale.ROOT));
        Assertions.assertThat(testCase.getResultDetails()).isEqualTo("Error 1 happened");
    }

    @Test
    void givenATestCase_whenExecutingItAndASuccessHappens_thenTheSuccessIsCorrectlyReportedWithinTheTestCase() {
        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            testCaseListener.reportInfo(logger, "Success {} happened", "1");
        }, PlaybookData.builder().build());

        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSuccess(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseWarns(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseErrors(Mockito.any());

        TestCaseSummary testCase = testCaseListener.testCaseSummaryDetails.getFirst();
        Assertions.assertThat(testCase.getResult()).isEqualTo("success");
        Assertions.assertThat(testCase.getResultDetails()).isEqualTo("Success 1 happened");
    }

    @Test
    void givenATestCase_whenSkippingIt_thenTheTestCaseIsNotReported() {
        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> testCaseListener.skipTest(logger, "Skipper!"), PlaybookData.builder().build());

        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSkipped();
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseWarns(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseErrors(Mockito.any());

        Assertions.assertThat(testCaseListener.testCaseSummaryDetails).isEmpty();
    }

    @Test
    void givenADocumentedResponseThatMatchesTheResponseCodeAndSchema_whenReportingTheResult_thenTheResultIsCorrectlyReported() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getBody()).thenReturn("{}");
        Mockito.when(data.getResponseCodes()).thenReturn(Collections.singleton("200"));
        Mockito.when(data.getResponses()).thenReturn(Collections.singletonMap("200", Collections.singletonList("")));
        Mockito.when(response.responseCodeAsString()).thenReturn("200");

        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.TWOXX);
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSuccess(Mockito.any());
    }

    @Test
    void givenADocumentedResponseThatMatchesTheResponseCodeAndButNotSchema_whenReportingTheResult_thenTheResultIsCorrectlyReported() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getBody()).thenReturn("{'test':1}");
        Mockito.when(response.getJsonBody()).thenReturn(JsonParser.parseString("{'test':1}"));
        Mockito.when(data.getResponseCodes()).thenReturn(Collections.singleton("200"));
        Mockito.when(data.getResponses()).thenReturn(Collections.singletonMap("200", Collections.singletonList("nomatch")));
        Mockito.when(response.responseCodeAsString()).thenReturn("200");
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);

        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.TWOXX);
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseWarns(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());
        TestCaseSummary testCase = testCaseListener.testCaseSummaryDetails.getFirst();
        Assertions.assertThat(testCase.getResultDetails()).startsWith("Response does NOT match expected result. Response code");
    }

    @Test
    void givenAnUndocumentedResponseThatMatchesTheResponseCode_whenReportingTheResult_thenTheResultIsCorrectlyReported() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getBody()).thenReturn("{'test':1}");
        Mockito.when(data.getResponseCodes()).thenReturn(Collections.singleton("400"));
        Mockito.when(data.getResponses()).thenReturn(Collections.singletonMap("200", Collections.singletonList("test")));
        Mockito.when(response.responseCodeAsString()).thenReturn("200");
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);

        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.TWOXX);
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseWarns(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());
        TestCaseSummary testCase = testCaseListener.testCaseSummaryDetails.getFirst();
        Assertions.assertThat(testCase.getResultDetails()).startsWith("Response does NOT match expected result. Response code is from a list of expected codes for this PLAYBOOK");
    }

    @Test
    void givenADocumentedResponseThatIsNotExpected_whenReportingTheResult_thenTheResultIsCorrectlyReported() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getBody()).thenReturn("{'test':1}");
        Mockito.when(data.getResponseCodes()).thenReturn(Collections.singleton("400"));
        Mockito.when(data.getResponses()).thenReturn(Collections.singletonMap("200", Collections.singletonList("test")));
        Mockito.when(response.responseCodeAsString()).thenReturn("400");
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);

        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.TWOXX), PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseErrors(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());
    }

    @Test
    void givenAnUndocumentedResponseThatIsNotExpected_whenReportingTheResult_thenTheResultIsCorrectlyReported() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getBody()).thenReturn("{'test':1}");
        Mockito.when(data.getResponseCodes()).thenReturn(Collections.singleton("200"));
        Mockito.when(data.getResponses()).thenReturn(Collections.singletonMap("200", Collections.singletonList("test")));
        Mockito.when(response.responseCodeAsString()).thenReturn("400");
        Mockito.when(response.responseCodeAsResponseRange()).thenReturn("4XX");
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);

        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> testCaseListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.TWOXX), PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseErrors(Mockito.any());
        Mockito.verify(executionStatisticsListener, Mockito.never()).increaseSuccess(Mockito.any());
        TestCaseSummary testCase = testCaseListener.testCaseSummaryDetails.getFirst();
        Assertions.assertThat(testCase.getResultDetails()).startsWith("Unexpected behaviour");
    }

    @ParameterizedTest
    @CsvSource({",", "test", "anEnum"})
    void shouldReportInfoWhenResponseCode400IsExpectedAndResponseBodyMatchesAndFuzzedFieldNullOrPresent(String fuzzedField) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        StringSchema enumSchema = new StringSchema();
        List<String> enumList = new ArrayList<>();
        enumList.add(null);
        enumList.add("value");
        enumSchema.setEnum(enumList);
        Mockito.when(response.getBody()).thenReturn("{'test':1,'anEnum':null}");
        Mockito.when(response.getJsonBody()).thenReturn(JsonParser.parseString("{'test':1,'anEnum':null}"));
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("200", "400"));
        Mockito.when(data.getResponses()).thenReturn(Map.of("400", Collections.singletonList("{'test':'4','anEnum':'value'}"), "200", Collections.singletonList("{'other':'2'}")));
        Mockito.when(data.getRequestPropertyTypes()).thenReturn(Map.of("anEnum", enumSchema));
        Mockito.when(response.responseCodeAsString()).thenReturn("400");
        Mockito.when(response.getTestedField()).thenReturn(fuzzedField);

        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            spyListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.FOURXX);
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSuccess(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportInfo(logger, "Response matches expected result. Response code [400] is documented and response body matches the corresponding schema.");
    }

    @ParameterizedTest
    @CsvSource({"{}", "[]", "''", "' '"})
    void shouldReportInfoWhenResponseCode200IsExpectedAndResponseBodyIsEmpty(String body) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        Mockito.when(response.getBody()).thenReturn(body);
        Mockito.when(response.getJsonBody()).thenReturn(JsonParser.parseString(body));
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("200", "400"));
        Mockito.when(data.getResponses()).thenReturn(Collections.emptyMap());
        Mockito.when(response.responseCodeAsString()).thenReturn("400");
        Mockito.when(response.responseCodeAsResponseRange()).thenReturn("4XX");

        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            spyListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.FOURXX);
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSuccess(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportInfo(logger, "Response matches expected result. Response code [400] is documented and response body matches the corresponding schema.");
    }

    @Test
    void shouldReportInfoWhenResponseCode200IsExpectedAndResponseBodyIsArray() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        Mockito.when(response.getBody()).thenReturn("[{'test':1},{'test':2}]");
        Mockito.when(response.getJsonBody()).thenReturn(JsonParser.parseString("[{'test':1},{'test':2}]"));
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("200", "400"));
        Mockito.when(data.getResponses()).thenReturn(Map.of("400", Collections.singletonList("{'test':'4'}"), "200", Collections.singletonList("{'other':'2'}")));
        Mockito.when(response.responseCodeAsString()).thenReturn("400");

        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            spyListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.FOURXX);
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSuccess(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportInfo(logger, "Response matches expected result. Response code [400] is documented and response body matches the corresponding schema.");
    }

    @ParameterizedTest
    @CsvSource(value = {"[]|[{'test':'4'},{'test':'4'}]", "[{'test':1},{'test':2}]|{'test':'4'}"}, delimiter = '|')
    void shouldReportInfoWhenResponseCodeIsExpectedAndResponseBodyAndDocumentedResponsesAreArrays(String returnedBody, String documentedResponses) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        Mockito.when(response.getBody()).thenReturn(returnedBody);
        Mockito.when(response.getJsonBody()).thenReturn(JsonParser.parseString(returnedBody));
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("200", "400"));
        Mockito.when(data.getResponses()).thenReturn(Map.of("400", Collections.singletonList(documentedResponses), "200", Collections.singletonList("{'other':'2'}")));
        Mockito.when(response.responseCodeAsString()).thenReturn("400");

        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            spyListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.FOURXX);
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSuccess(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportInfo(logger, "Response matches expected result. Response code [400] is documented and response body matches the corresponding schema.");
    }

    @Test
    void shouldReportInfoWhenResponseCode200IsExpectedAndResponseBodyIsEmptyArrayButResponseIsNotArray() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        Mockito.when(response.getBody()).thenReturn("[]");
        Mockito.when(response.getJsonBody()).thenReturn(JsonParser.parseString("[]"));
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("200", "400"));
        Mockito.when(data.getResponses()).thenReturn(Map.of("400", Collections.singletonList("{'test':'4'}"), "200", Collections.singletonList("{'other':'2'}")));
        Mockito.when(response.responseCodeAsString()).thenReturn("400");
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);

        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            spyListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.FOURXX);
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSuccess(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportInfo(logger, "Response matches expected result. Response code [400] is documented and response body matches the corresponding schema.");
    }

    @ParameterizedTest
    @CsvSource({"application/csv", "application/pdf"})
    void shouldReportInfoWhenResponseCode200AndResponseContentTypeIsAFile(String contentType) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        Mockito.when(response.getBody()).thenReturn("column1,column2,column3");
        Mockito.when(response.getJsonBody()).thenReturn(JsonParser.parseString("{'notAJson': 'column1,column2,column3'}"));
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("200", "400"));
        Mockito.when(data.getResponses()).thenReturn(Map.of("400", Collections.singletonList("{'test':'4'}"), "200", Collections.singletonList("{'other':'2'}")));
        Mockito.when(data.getContentTypesByResponseCode(Mockito.any())).thenReturn(List.of(contentType));
        Mockito.when(response.responseCodeAsString()).thenReturn("200");
        Mockito.when(response.getResponseContentType()).thenReturn(contentType);
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);

        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            spyListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.TWOXX);
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSuccess(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportInfo(logger, "Response matches expected result. Response code [200] is documented and response body matches the corresponding schema.");
    }

    @Test
    void shouldReportWarnWhenResponseCode400IsExpectedAndResponseBodyMatchesButFuzzedFieldNotPresent() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        Mockito.when(response.getBody()).thenReturn("{'test':1}");
        Mockito.when(response.getJsonBody()).thenReturn(JsonParser.parseString("{'test':1}"));
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("200", "400"));
        Mockito.when(data.getResponses()).thenReturn(Map.of("400", Collections.singletonList("{'test':'4'}"), "200", Collections.singletonList("{'other':'2'}")));
        Mockito.when(response.responseCodeAsString()).thenReturn("400");
        Mockito.when(response.getTestedField()).thenReturn("someField");
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);

        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            spyListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.FOURXX);
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseWarns(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportWarn(logger, "Response does NOT match expected result. Response code [400] is documented, but response body does NOT match the corresponding schema.");
    }

    @ParameterizedTest
    @CsvSource({"406,FOURXX_MT", "415,FOURXX_MT", "400,FOURXX"})
    void shouldReportInfoWhenResponseCodeNotNecessarilyDocumentedIsExpectedAndResponseBodyMatchesButFuzzedFieldNotPresent(String responseCode, ResponseCodeFamilyPredefined family) {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        Mockito.when(response.getBody()).thenReturn("{'test':1}");
        Mockito.when(response.getJsonBody()).thenReturn(JsonParser.parseString("{'test':1}"));
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("200", "4xx"));
        Mockito.when(data.getResponses()).thenReturn(new TreeMap<>(Map.of("4xx", Collections.singletonList("{'test':'4'}"), "200", Collections.singletonList("{'other':'2'}"))));
        Mockito.when(response.responseCodeAsString()).thenReturn(responseCode);
        Mockito.when(response.responseCodeAsResponseRange()).thenReturn("4XX");
        Mockito.when(response.getTestedField()).thenReturn("test");

        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            spyListener.reportResult(logger, data, response, family);
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseSuccess(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportInfo(logger, "Response matches expected result. Response code [%s] is documented and response body matches the corresponding schema.".formatted(responseCode));
    }

    @Test
    void shouldReportErrorWhenPlaybookSuccessfulButResponseTimeExceedsMax() {
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        Mockito.when(ignoreArguments.isSkipReportingForSuccess()).thenReturn(false);
        Mockito.when(ignoreArguments.isSkipReportingForIgnoredArguments()).thenReturn(false);
        Mockito.when(reportingArguments.getMaxResponseTime()).thenReturn(10);

        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addResponse(HttpResponse.builder().responseTimeInMs(100).build());
            spyListener.reportInfo(logger, "Response code expected", "200");
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseErrors(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportError(logger, "Test case executed successfully, but response time exceeds --maxResponseTimeInMs: actual 100, max 10");
    }

    @Test
    void shouldReportErrorWhenKeywordsDetectedInResponse() {
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        Mockito.when(ignoreArguments.isSkipReportingForSuccess()).thenReturn(false);
        Mockito.when(ignoreArguments.isSkipReportingForIgnoredArguments()).thenReturn(false);
        Mockito.when(ignoreArguments.isIgnoreErrorLeaksCheck()).thenReturn(false);


        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addResponse(HttpResponse.builder().body("PermissionDenied").responseTimeInMs(100).build());
            spyListener.reportInfo(logger, "Response code expected", "200");
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseErrors(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportError(logger, "The following keywords were detected in the response which might suggest an error details leak: [PermissionDenied]");
    }

    @Test
    void shouldReportWarnWhenResponseCode400IsUndocumentedAndResponseBodyMatches() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        Mockito.when(reportingArguments.isPrintExecutionStatistics()).thenReturn(true);
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);
        Mockito.when(response.getBody()).thenReturn("{'test':1}");
        Mockito.when(data.getResponseCodes()).thenReturn(new TreeSet<>(Set.of("200", "401")));
        Mockito.when(data.getResponses()).thenReturn(Map.of("401", Collections.singletonList("{'test':'4'}"), "200", Collections.singletonList("{'other':'2'}")));
        Mockito.when(response.responseCodeAsString()).thenReturn("400");
        Mockito.when(response.responseCodeAsResponseRange()).thenReturn("4XX");
        Mockito.when(response.isValidErrorCode()).thenReturn(true);

        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            spyListener.addResponse(response);
            spyListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.FOURXX);
        }, PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseWarns(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportWarn(logger, "Response does NOT match expected result. Response code is from a list of expected codes for this PLAYBOOK, but it is undocumented: expected %s, actual [400], documented response codes: [200, 401]".formatted(ResponseCodeFamilyPredefined.FOURXX.allowedResponseCodes().toString()));
    }

    @Test
    void shouldReportNotFound() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);
        Mockito.when(response.getBody()).thenReturn("");
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("401"));
        Mockito.when(data.getResponses()).thenReturn(Map.of("401", Collections.singletonList("{'test':'4'}")));
        Mockito.when(response.responseCodeAsString()).thenReturn("404");
        Mockito.when(response.getResponseCode()).thenReturn(404);
        Mockito.when(response.responseCodeAsResponseRange()).thenReturn("4XX");

        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> spyListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.FOURXX), PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseWarns(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportWarn(logger, "Response does NOT match expected result. Response code is from a list of expected codes for this PLAYBOOK, but it is undocumented: expected [400, 404, 413, 414, 422, 431], actual [404], documented response codes: [401]");
    }

    @Test
    void shouldReportNotImplemented() {
        PlaybookData data = Mockito.mock(PlaybookData.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        TestCaseListener spyListener = Mockito.spy(testCaseListener);
        Mockito.when(ignoreArguments.isNotIgnoredResponse(Mockito.any())).thenReturn(true);
        Mockito.when(response.getBody()).thenReturn("");
        Mockito.when(data.getResponseCodes()).thenReturn(Set.of("401"));
        Mockito.when(data.getResponses()).thenReturn(Map.of("401", Collections.singletonList("{'test':'4'}")));
        Mockito.when(response.responseCodeAsString()).thenReturn("501");
        Mockito.when(response.getResponseCode()).thenReturn(501);
        Mockito.when(response.responseCodeAsResponseRange()).thenReturn("501");

        spyListener.createAndExecuteTest(logger, testCasePlaybook, () -> spyListener.reportResult(logger, data, response, ResponseCodeFamilyPredefined.TWOXX), PlaybookData.builder().build());
        Mockito.verify(executionStatisticsListener, Mockito.times(1)).increaseWarns(Mockito.any());
        Mockito.verify(spyListener, Mockito.times(1)).reportWarn(logger, "Response HTTP code 501: you forgot to implement this functionality!");
    }


    @Test
    void shouldReturnIsDiscriminator() {
        Discriminator discriminator = new Discriminator();
        discriminator.setPropertyName("field");
        globalContext.getDiscriminators().clear();
        globalContext.recordDiscriminator("", discriminator, List.of());

        Assertions.assertThat(testCaseListener.isFieldNotADiscriminator("field")).isFalse();
    }

    @Test
    void shouldReturnIsNotDiscriminator() {
        Discriminator discriminator = new Discriminator();
        discriminator.setPropertyName("field");
        globalContext.getDiscriminators().clear();
        globalContext.getDiscriminators().add(discriminator);

        Assertions.assertThat(testCaseListener.isFieldNotADiscriminator("additionalField")).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"DELETE,200", "DELETE,204"})
    void shouldStoreDeleteResponse(String httpMethod, int code) {
        TestCase testCase = new TestCase();
        testCase.setResponse(HttpResponse.builder().responseCode(code).build());
        testCase.setRequest(HttpRequest.builder().httpMethod(httpMethod).url("test").build());

        Assertions.assertThat(globalContext.getSuccessfulDeletes()).isEmpty();
        testCaseListener.storeSuccessfulDelete(testCase);
        Assertions.assertThat(globalContext.getSuccessfulDeletes()).contains("test");
        globalContext.getSuccessfulDeletes().clear();
    }

    @ParameterizedTest
    @CsvSource({"DELETE,400", "GET,204", "GET,400"})
    void shouldNotStoreDeleteResponse(String httpMethod, int code) {
        TestCase testCase = new TestCase();
        testCase.setResponse(HttpResponse.builder().responseCode(code).build());
        testCase.setRequest(HttpRequest.builder().httpMethod(httpMethod).url("test").build());

        Assertions.assertThat(globalContext.getSuccessfulDeletes()).isEmpty();
        testCaseListener.storeSuccessfulDelete(testCase);
        Assertions.assertThat(globalContext.getSuccessfulDeletes()).isEmpty();
    }

    @Test
    void shouldReturnDefaultResponseCodeFamilyWhenConfigNotFound() {
        globalContext.getPlaybooksConfiguration().put("AnotherDummy.expectedResponseCode", "999");
        ResponseCodeFamily resultCodeFromFile = testCaseListener.getExpectedResponseCodeConfiguredFor("Dummy", ResponseCodeFamilyPredefined.TWOXX);

        Assertions.assertThat(resultCodeFromFile).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
    }

    @ParameterizedTest
    @CsvSource({"application/json,application/v1+json,true", "application/v2+json,application/v3+json,false", "application/v3+json,application/json,true",
            "application/vnd+json,application/json,true", "application/json,application/xml,false", "application/json; charset=utf,application/json; charset=iso,true",
            "*/*,application/json,true", "application/application,application/application/json,false"})
    void shouldCheckContentTypesEquivalence(String firstContentType, String secondContentType, boolean expected) {
        boolean result = testCaseListener.areContentTypesEquivalent(firstContentType, secondContentType);
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    void shouldLoadPlaybookSpecificResponseCode() {
        globalContext = new GlobalContext();
        globalContext.getPlaybooksConfiguration().setProperty("playbook.expectedResponseCode", "201,202");
        ReflectionTestUtils.setField(testCaseListener, "globalContext", globalContext);
        ResponseCodeFamily family = testCaseListener.getExpectedResponseCodeConfiguredFor("playbook", ResponseCodeFamilyPredefined.TWOXX);

        Assertions.assertThat(family).isInstanceOf(ResponseCodeFamilyDynamic.class);

        ResponseCodeFamilyDynamic familyDynamic = (ResponseCodeFamilyDynamic) family;
        Assertions.assertThat(familyDynamic.allowedResponseCodes()).containsOnly("201", "202");
    }

    @Test
    void shouldReturnDefaultResponseCodeWhenNoConfiguration() {
        ReflectionTestUtils.setField(testCaseListener, "globalContext", new GlobalContext());
        ResponseCodeFamily family = testCaseListener.getExpectedResponseCodeConfiguredFor("playbook", ResponseCodeFamilyPredefined.TWOXX);

        Assertions.assertThat(family).isInstanceOf(ResponseCodeFamilyPredefined.class);

        ResponseCodeFamilyPredefined familyPredefined = (ResponseCodeFamilyPredefined) family;
        Assertions.assertThat(familyPredefined).isEqualTo(ResponseCodeFamilyPredefined.TWOXX);
    }

    @Test
    void shouldReturnCurrentTestNumber() {
        prepareTestCaseListenerSimpleSetup(HttpResponse.builder().build(), () -> {
        });
        Assertions.assertThat(testCaseListener.getCurrentTestCaseNumber()).isEqualTo(1);
    }

    @Test
    void shouldGenerateTraceId() {
        prepareTestCaseListenerSimpleSetup(HttpResponse.builder().build(), () -> Assertions.assertThat(testCaseListener.getTestIdentifier()).isNotNull());
    }

    @Test
    void shouldReturnCurrentPlaybook() {
        testCaseListener.beforeFuzz(RandomResourcesPlaybook.class, "test", "post");
        String currentPlaybook = testCaseListener.getCurrentPlaybook();
        Assertions.assertThat(currentPlaybook).isEqualTo("RandomResources");
    }

    @Test
    void shouldStartUnknownProgress() {
        PlaybookData data = PlaybookData.builder().contractPath("/test").method(HttpMethod.POST).path("/test").build();
        Mockito.when(reportingArguments.isSummaryInConsole()).thenReturn(true);
        ReflectionTestUtils.setField(globalContext, "dochiaConfiguration",
                new DochiaConfiguration("/test", "test", "test", List.of(), 1, 2, 2, 3));
        TestCaseListener testCaseListenerSpy = Mockito.spy(testCaseListener);
        testCaseListenerSpy.updateUnknownProgress(data);
        Mockito.verify(testCaseListenerSpy).notifySummaryObservers("/test");
    }

    private void prepareTestCaseListenerSimpleSetup(HttpResponse build, Runnable runnable) {
        testCaseListener.createAndExecuteTest(logger, testCasePlaybook, () -> {
            testCaseListener.addScenario(logger, "Given a {} field", "string");
            testCaseListener.addRequest(HttpRequest.builder().httpMethod("method").build());
            testCaseListener.addResponse(build);
            testCaseListener.addFullRequestPath("fullPath");
            testCaseListener.addPath("path");
            testCaseListener.addExpectedResult(logger, "Should return {}", "2XX");
            runnable.run();
        }, PlaybookData.builder().build());
    }
}
