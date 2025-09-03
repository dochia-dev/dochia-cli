package dev.dochia.cli.core.args;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class AuthArgumentsTest {

    @Test
    void shouldReturnEmptyBasicAuth() {
        AuthArguments args = new AuthArguments();
        Assertions.assertThat(args.isBasicAuthSupplied()).isFalse();
    }


    @Test
    void shouldReturnBasicAuthHeader() {
        AuthArguments args = new AuthArguments();
        ReflectionTestUtils.setField(args, "basicAuth", "user:pwd");
        Assertions.assertThat(args.isBasicAuthSupplied()).isTrue();
        Assertions.assertThat(args.getBasicAuthHeader()).isEqualTo("Basic dXNlcjpwd2Q=");

    }

    @Test
    void shouldReturnFalseMutualTls() {
        AuthArguments args = new AuthArguments();
        Assertions.assertThat(args.isMutualTls()).isFalse();
    }

    @Test
    void shouldReturnTrueMutualTls() {
        AuthArguments args = new AuthArguments();
        ReflectionTestUtils.setField(args, "sslKeystore", "keystore.jks");
        Assertions.assertThat(args.isMutualTls()).isTrue();
    }


    @ParameterizedTest
    @CsvSource(value = {"null,0", "localhost,0", "null,8080"}, nullValues = "null")
    void shouldReturnNotIsProxyHost(String host, int port) {
        AuthArguments args = new AuthArguments();

        ReflectionTestUtils.setField(args, "proxyHost", host);
        ReflectionTestUtils.setField(args, "proxyPort", port);

        Assertions.assertThat(args.getProxy().type()).isEqualTo(Proxy.Type.DIRECT);
    }


    @Test
    void shouldReturnProxy() {
        AuthArguments args = new AuthArguments();
        ReflectionTestUtils.setField(args, "proxyHost", "localhost");
        ReflectionTestUtils.setField(args, "proxyPort", 8080);

        Assertions.assertThat(args.getProxy().type()).isEqualTo(Proxy.Type.HTTP);
    }

    @Test
    void shouldReturnProxyFromFullProxyString() {
        AuthArguments args = new AuthArguments();
        ReflectionTestUtils.setField(args, "proxy", "http://myhost:1234");
        Proxy proxy = args.getProxy();
        assertThat(proxy.type()).isEqualTo(Proxy.Type.HTTP);
        assertThat(((java.net.InetSocketAddress) proxy.address()).getHostName()).isEqualTo("myhost");
        assertThat(((java.net.InetSocketAddress) proxy.address()).getPort()).isEqualTo(1234);
    }

    @Test
    void shouldReturnProxyFromFullProxyStringWithoutScheme() {
        AuthArguments args = new AuthArguments();
        ReflectionTestUtils.setField(args, "proxy", "myhost:4321");
        Proxy proxy = args.getProxy();
        assertThat(proxy.type()).isEqualTo(Proxy.Type.HTTP);
        assertThat(((java.net.InetSocketAddress) proxy.address()).getHostName()).isEqualTo("myhost");
        assertThat(((java.net.InetSocketAddress) proxy.address()).getPort()).isEqualTo(4321);
    }

    @Test
    void shouldReturnNoProxyForInvalidProxyFormat() {
        AuthArguments args = new AuthArguments();
        ReflectionTestUtils.setField(args, "proxy", "badformat");
        assertThat(args.getProxy()).isEqualTo(Proxy.NO_PROXY);
    }

    @Test
    void shouldReturnProxyFromHostAndPort() {
        AuthArguments args = new AuthArguments();
        ReflectionTestUtils.setField(args, "proxyHost", "localhost");
        ReflectionTestUtils.setField(args, "proxyPort", 8080);
        Proxy proxy = args.getProxy();
        assertThat(proxy.type()).isEqualTo(Proxy.Type.HTTP);
        assertThat(((java.net.InetSocketAddress) proxy.address()).getHostName()).isEqualTo("localhost");
        assertThat(((java.net.InetSocketAddress) proxy.address()).getPort()).isEqualTo(8080);
    }

    @Test
    void shouldReturnNoProxyIfNotSet() {
        AuthArguments args = new AuthArguments();
        Proxy proxy = args.getProxy();
        assertThat(proxy.type()).isEqualTo(Proxy.Type.DIRECT);
    }
}
