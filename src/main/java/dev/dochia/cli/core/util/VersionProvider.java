package dev.dochia.cli.core.util;

import picocli.CommandLine;

import java.util.Properties;

/**
 * Provides information about application version to PicoCli.
 */
public class VersionProvider implements CommandLine.IVersionProvider {

    private static final String LOGO = """
                 888                   888      d8b
                 888                   888      Y8P
                 888                   888
             .d88888  .d88b.   .d8888b 88888b.  888  8888b.
            d88" 888 d88""88b d88P"    888 "88b 888     "88b
            888  888 888  888 888      888  888 888 .d888888
            Y88b 888 Y88..88P Y88b.    888  888 888 888  888
             "Y88888  "Y88P"   "Y8888P 888  888 888 "Y888888
            
            """;

    private static final String DESCRIPTION = """
            
            @|bold dochia|@ - bringing chaos with love!
            
            @|bold,underline Because nobody wants to debug why their "enterprise-grade" API can't handle a simple 🤷‍♀️|@
            
            @|magenta dochia|@ finds bugs, security vulnerabilities, and contract violations in your API - so you and your
            AI agents can focus on building, not writing endless test cases or debugging edge-case failures.
            
            @|yellow GitHub:|@        https://github.com/dochia-dev/dochia-cli
            @|yellow Docs:|@          https://docs.dochia.dev
            @|yellow Agent Skills:|@  @|magenta dochia|@ init-skills
            """;

    @Override
    public String[] getVersion() throws Exception {
        Properties properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream("version.properties"));
        String version = properties.getProperty("app.version") + " (Free)";
        String buildTime = properties.getProperty("app.buildTime");

        return new String[]{
                LOGO,
                "@|bold,green " + properties.getProperty("app.name") + " version " + version + "|@",
                "@|faint Built on: " + buildTime + "|@",
                DESCRIPTION
        };
    }
}
