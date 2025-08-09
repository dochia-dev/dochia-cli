package dev.dochia.cli.core.playbook.header.base;

import dev.dochia.cli.core.args.FilterArguments;
import dev.dochia.cli.core.args.MatchArguments;
import dev.dochia.cli.core.http.ResponseCodeFamilyPredefined;
import dev.dochia.cli.core.io.ServiceCaller;
import dev.dochia.cli.core.model.DochiaHeader;
import dev.dochia.cli.core.model.HttpResponse;
import dev.dochia.cli.core.model.PlaybookData;
import dev.dochia.cli.core.playbook.executor.HeadersIteratorExecutor;
import dev.dochia.cli.core.report.TestCaseListener;
import dev.dochia.cli.core.report.TestReportsGenerator;
import dev.dochia.cli.core.strategy.FuzzingStrategy;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.swagger.v3.oas.models.media.StringSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

@QuarkusTest
class BaseHeadersPlaybookTest {
    private ServiceCaller serviceCaller;
    @InjectSpy
    private TestCaseListener testCaseListener;

    private BaseHeadersPlaybook baseHeadersPlaybook;

    private FilterArguments filterArguments;

    private HttpResponse httpResponse;


    @BeforeEach
    void setup() {
        serviceCaller = Mockito.mock(ServiceCaller.class);
        filterArguments = Mockito.mock(FilterArguments.class);
        HeadersIteratorExecutor headersIteratorExecutor = new HeadersIteratorExecutor(serviceCaller, testCaseListener, Mockito.mock(MatchArguments.class), filterArguments);
        baseHeadersPlaybook = new MyBaseHeadersPlaybook(headersIteratorExecutor);
        ReflectionTestUtils.setField(testCaseListener, "testReportsGenerator", Mockito.mock(TestReportsGenerator.class));
        httpResponse = HttpResponse.builder().body("{}").responseCode(200).build();
    }

    @Test
    void givenAConcreteBaseHeadersPlaybookInstanceWithNoMandatoryHeader_whenExecutingTheRunMethod_thenTheFuzzingLogicIsProperlyExecuted() {
        PlaybookData data = this.createData(false);
        baseHeadersPlaybook.run(data);
        Mockito.verify(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.TWOXX), Mockito.eq(true), Mockito.eq(true));
    }

    @Test
    void givenAConcreteBaseHeadersPlaybookInstanceWithMandatoryHeader_whenExecutingTheRunMethod_thenTheFuzzingLogicIsProperlyExecuted() {
        PlaybookData data = this.createData(true);
        baseHeadersPlaybook.run(data);
        Mockito.verify(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX), Mockito.eq(true), Mockito.eq(true));
    }

    @Test
    void shouldNotRunWhenNoHeaders() {
        PlaybookData data = PlaybookData.builder().headers(Set.of(DochiaHeader.builder().name("jwt").value("jwt").build())).reqSchema(new StringSchema())
                .requestContentTypes(List.of("application/json")).build();
        Mockito.doCallRealMethod().when(serviceCaller).isAuthenticationHeader(Mockito.any());
        baseHeadersPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(0)).reportResult(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.eq(true));
    }

    @Test
    void shouldNotRunForIgnoredHeaders() {
        PlaybookData data = createData(true);
        data.getHeaders().add(DochiaHeader.builder().name("skippedHeader").value("skippedValue").required(true).build());
        baseHeadersPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(2)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX), Mockito.eq(true), Mockito.eq(true));

        Mockito.when(filterArguments.getSkipHeaders()).thenReturn(List.of("skippedHeader"));
        baseHeadersPlaybook.run(data);
        Mockito.verify(testCaseListener, Mockito.times(3)).reportResult(Mockito.any(), Mockito.eq(data), Mockito.eq(httpResponse), Mockito.eq(ResponseCodeFamilyPredefined.FOURXX), Mockito.eq(true), Mockito.eq(true));
    }

    private PlaybookData createData(boolean requiredHeaders) {
        Map<String, List<String>> responses = new HashMap<>();
        responses.put("200", Collections.singletonList("response"));
        PlaybookData data = PlaybookData.builder().headers(new HashSet<>(Set.of(DochiaHeader.builder().name("header").value("value").required(requiredHeaders).build())))
                .responseCodes(Set.of("200", "202")).reqSchema(new StringSchema())
                .responses(responses).requestContentTypes(List.of("application/json")).build();
        Mockito.when(serviceCaller.call(Mockito.any())).thenReturn(httpResponse);

        Mockito.doNothing().when(testCaseListener).reportResult(Mockito.any(), Mockito.eq(data), Mockito.any(), Mockito.any());

        return data;
    }

    static class MyBaseHeadersPlaybook extends BaseHeadersPlaybook {

        public MyBaseHeadersPlaybook(HeadersIteratorExecutor headersIteratorExecutor) {
            super(headersIteratorExecutor);
        }

        @Override
        public BaseHeadersPlaybookContext createPlaybookContext() {
            return BaseHeadersPlaybookContext.builder()
                    .matchResponseSchema(true)
                    .fuzzStrategy(Collections.singletonList(FuzzingStrategy.replace()))
                    .expectedHttpCodeForRequiredHeadersFuzzed(ResponseCodeFamilyPredefined.FOURXX)
                    .expectedHttpForOptionalHeadersFuzzed(ResponseCodeFamilyPredefined.TWOXX)
                    .typeOfDataSentToTheService("my data")
                    .build();
        }
    }
}
