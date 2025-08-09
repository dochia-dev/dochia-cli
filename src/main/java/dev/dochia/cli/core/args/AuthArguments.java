package dev.dochia.cli.core.args;

import jakarta.inject.Singleton;
import lombok.Getter;
import picocli.CommandLine;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Holds all args related to Authentication details.
 */
@Singleton
@Getter
public class AuthArguments {
    @CommandLine.Option(names = {"--ssl-keystore"}, paramLabel = "<keystore>",
            description = "Location of the keystore holding certificates used when authenticating calls using one-way or two-way SSL")
    private String sslKeystore;

    @CommandLine.Option(names = {"--ssl-keystore-pwd"}, paramLabel = "<password>",
            description = "The password of the sslKeystore")
    private String sslKeystorePwd;

    @CommandLine.Option(names = {"--ssl-key-pwd"}, paramLabel = "<password>",
            description = "The password of the private key from the sslKeystore")
    private String sslKeyPwd;

    @CommandLine.Option(names = {"--user", "-u"}, paramLabel = "<user:password>",
            description = "A username:password pair, when using basic auth")
    private String basicAuth;

    @CommandLine.Option(names = {"--proxy-host"}, paramLabel = "<host>",
            description = "The proxy server's host name")
    private String proxyHost;

    @CommandLine.Option(names = {"--proxy-port"}, paramLabel = "<port>",
            description = "The proxy server's port number")
    private int proxyPort;

    @CommandLine.Option(names = {"--proxy"}, paramLabel = "<host>",
            description = "Full address of proxy: http(s)://host:port. If --proxy-host and --proxy-port are not supplied, this will be used to set the proxy.")
    private String proxy;

    @CommandLine.Option(names = {"--auth-refresh-script"}, paramLabel = "<auth-refresh-script>",
            description = "Script to get executed after --auth-refresh-interval in order to get new auth credentials. " +
                    "The script will replace any headers that have @|bold,underline auth_script|@ as value. " +
                    "If a --auth-refresh-interval is not supplied, but a script is, the script " +
                    "will be used to get the initial auth credentials.")
    private String authRefreshScript = "";

    @CommandLine.Option(names = {"--auth-refresh"}, paramLabel = "<interval>",
            description = "Amount of time in seconds after which to get new auth credentials")
    private int authRefreshInterval;


    /**
     * Checks if basic auth details were supplied via the {@code --basicAuth} argument.
     *
     * @return true if basic auth details were supplied, false otherwise
     */
    public boolean isBasicAuthSupplied() {
        return basicAuth != null;
    }

    /**
     * Checks if SSL keystore was supplied via the {@code --sslKeystore} argument.
     *
     * @return true if a SSL keystore was supplied, false otherwise
     */
    public boolean isMutualTls() {
        return sslKeystore != null;
    }

    /**
     * Returns the Proxy if set via --proxy or --proxyXXX arguments, or NO_PROXY otherwise.
     *
     * @return the Proxy settings supplied through args
     */
    public Proxy getProxy() {
        if (proxy != null && !proxy.isEmpty()) {
            try {
                String proxyUrl = proxy.replaceFirst("^(https?://)?", "");
                String[] parts = proxyUrl.split(":");
                if (parts.length == 2) {
                    String host = parts[0];
                    int port = Integer.parseInt(parts[1]);
                    return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
                }
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid proxy format. Expected format: http(s)://host:port", e);
            }
        } else if (proxyHost != null && proxyPort != 0) {
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        }
        return Proxy.NO_PROXY;
    }

    /**
     * Creates a basic auth header based on the supplied --basicAuth argument.
     *
     * @return base64 encoded basic auth header
     */
    public String getBasicAuthHeader() {
        byte[] encodedAuth = Base64.getEncoder().encode(this.basicAuth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth, StandardCharsets.UTF_8);
    }

    /**
     * Creates a Map with the following elements "auth_script"=--authRefreshScript argument
     * and "auth_refresh"=--authRefreshInterval.
     *
     * @return a Map with auth refresh details
     */
    public Map<String, String> getAuthScriptAsMap() {
        return Map.of("auth_script", this.getAuthRefreshScript(), "auth_refresh", String.valueOf(getAuthRefreshInterval()));
    }
}
