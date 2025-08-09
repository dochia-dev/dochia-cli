package dev.dochia.cli.core.context;

import dev.dochia.cli.core.model.DochiaConfiguration;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@QuarkusTest
class GlobalContextTest {

    @Test
    void shouldAddExample() {
        GlobalContext context = new GlobalContext();
        context.addGeneratedExample("/test", List.of("example"));

        Assertions.assertThat(context.getGeneratedExamplesCache().get("/test")).containsExactly("example");
    }

    @Test
    void shouldResolveParametersReferences() {
        GlobalContext context = new GlobalContext();
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Components components = Mockito.mock(Components.class);
        Mockito.when(openAPI.getComponents()).thenReturn(components);
        Map<String, Parameter> parameterMap = Map.of("Example", new Parameter());
        Mockito.when(components.getParameters()).thenReturn(parameterMap);
        context.init(openAPI, List.of(), new Properties(), new DochiaConfiguration("1", "2", "3", List.of(), 4, 4, 5, 6), Set.of());

        String reference = "#/components/parameters/Example";
        Object resolvedObject = context.getObjectFromPathsReference(reference);

        Assertions.assertThat(resolvedObject).isNotNull();

        String otherReference = "#/components/parameters/OtherExample";
        Object otherResolvedObject = context.getObjectFromPathsReference(otherReference);
        Assertions.assertThat(otherResolvedObject).isNull();
    }

    @Test
    void shouldResolveHeadersReferences() {
        GlobalContext context = new GlobalContext();
        OpenAPI openAPI = Mockito.mock(OpenAPI.class);
        Components components = Mockito.mock(Components.class);
        Mockito.when(openAPI.getComponents()).thenReturn(components);
        Map<String, io.swagger.v3.oas.models.headers.Header> headerMap = Map.of("Example", new Header());
        Mockito.when(components.getHeaders()).thenReturn(headerMap);
        context.init(openAPI, List.of(), new Properties(), new DochiaConfiguration("1", "2", "3", List.of(), 4, 4, 5, 6), Set.of());

        String reference = "#/components/headers/Example";
        Object resolvedObject = context.getObjectFromPathsReference(reference);

        Assertions.assertThat(resolvedObject).isNotNull();

        String otherReference = "#/components/headers/OtherExample";
        Object otherResolvedObject = context.getObjectFromPathsReference(otherReference);
        Assertions.assertThat(otherResolvedObject).isNull();
    }
}
