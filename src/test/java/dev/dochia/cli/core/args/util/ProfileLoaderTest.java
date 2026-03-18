package dev.dochia.cli.core.args.util;

import io.quarkus.test.junit.QuarkusTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

@QuarkusTest
@DisplayName("ProfileLoader Tests")
class ProfileLoaderTest {

    ProfileLoader profileLoader;

    @BeforeEach
    void setUp() {
        // Create a fresh ProfileLoader instance for each test to avoid state pollution
        profileLoader = new ProfileLoader();
    }

    @Nested
    @DisplayName("Health-Check Profile Tests")
    class HealthCheckProfileTests {
        @Test
        @DisplayName("Should load health-check profile")
        void shouldLoadHealthCheckProfile() {
            Optional<ProfileLoader.Profile> profile = profileLoader.getProfile("health-check");

            Assertions.assertThat(profile).isPresent();
            Assertions.assertThat(profile.get().name()).isEqualTo("health-check");
            Assertions.assertThat(profile.get().description())
                    .contains("health check")
                    .containsIgnoringCase("reachable");
            Assertions.assertThat(profile.get().playbooks())
                    .containsExactly("HappyPath")
                    .hasSize(1);
        }

        @Test
        @DisplayName("Should have minimal playbooks for quick API verification")
        void shouldHaveMinimalPlaybooksForHealthCheck() {
            Optional<ProfileLoader.Profile> profile = profileLoader.getProfile("health-check");

            Assertions.assertThat(profile).isPresent();
            Assertions.assertThat(profile.get().playbooks())
                    .hasSize(1)
                    .contains("HappyPath");
        }
    }

    @Nested
    @DisplayName("Built-in Profiles")
    class BuiltInProfilesTests {

        @Test
        @DisplayName("Should load all built-in profiles")
        void shouldLoadBuiltInProfiles() {
            Set<String> availableProfiles = profileLoader.getAvailableProfiles();

            Assertions.assertThat(availableProfiles)
                    .containsExactlyInAnyOrder("type-coercion", "security", "quick", "compliance", "ci", "full", "health-check");
        }

        @Test
        @DisplayName("Should load security profile with correct playbooks")
        void shouldLoadSecurityProfile() {
            Optional<ProfileLoader.Profile> profile = profileLoader.getProfile("security");

            Assertions.assertThat(profile).isPresent();
            Assertions.assertThat(profile.get().name()).isEqualTo("security");
            Assertions.assertThat(profile.get().description()).contains("Security-focused");
            Assertions.assertThat(profile.get().playbooks())
                    .contains(
                            "SqlInjectionInStringFields",
                            "NoSqlInjectionInStringFields",
                            "XssInjectionInStringFields",
                            "BypassAuthentication",
                            "MassAssignment"
                    );
        }

        @Test
        @DisplayName("Should load quick profile with correct playbooks")
        void shouldLoadQuickProfile() {
            Optional<ProfileLoader.Profile> profile = profileLoader.getProfile("quick");

            Assertions.assertThat(profile).isPresent();
            Assertions.assertThat(profile.get().name()).isEqualTo("quick");
            Assertions.assertThat(profile.get().description()).contains("Fast smoke test");
            Assertions.assertThat(profile.get().playbooks())
                    .contains(
                            "HappyPath",
                            "RemoveFields",
                            "NullValuesInFields"
                    );
        }

        @Test
        @DisplayName("Should load compliance profile with correct playbooks")
        void shouldLoadComplianceProfile() {
            Optional<ProfileLoader.Profile> profile = profileLoader.getProfile("compliance");

            Assertions.assertThat(profile).isPresent();
            Assertions.assertThat(profile.get().name()).isEqualTo("compliance");
            Assertions.assertThat(profile.get().description()).contains("OWASP");
            Assertions.assertThat(profile.get().playbooks())
                    .contains(
                            "InsecureDirectObjectReferences",
                            "MassAssignment",
                            "SSRFInUrlFields"
                    );
        }

        @Test
        @DisplayName("Should load ci profile with correct playbooks")
        void shouldLoadCiProfile() {
            Optional<ProfileLoader.Profile> profile = profileLoader.getProfile("ci");

            Assertions.assertThat(profile).isPresent();
            Assertions.assertThat(profile.get().name()).isEqualTo("ci");
            Assertions.assertThat(profile.get().description()).contains("CI/CD");
            Assertions.assertThat(profile.get().playbooks())
                    .contains(
                            "HappyPath",
                            "MinGreaterThanMaxFields",
                            "DateRangeInversion"
                    );
        }

        @Test
        @DisplayName("Should load full profile with empty playbook list")
        void shouldLoadFullProfile() {
            Optional<ProfileLoader.Profile> profile = profileLoader.getProfile("full");

            Assertions.assertThat(profile).isPresent();
            Assertions.assertThat(profile.get().name()).isEqualTo("full");
            Assertions.assertThat(profile.get().description()).contains("Complete test suite");
            Assertions.assertThat(profile.get().playbooks()).isEmpty();
        }

        @Test
        @DisplayName("Should return empty for non-existent profile")
        void shouldReturnEmptyForNonExistentProfile() {
            Optional<ProfileLoader.Profile> profile = profileLoader.getProfile("non-existent");

            Assertions.assertThat(profile).isEmpty();
        }
    }

    @Nested
    @DisplayName("Custom Profiles")
    class CustomProfilesTests {

        @Test
        @DisplayName("Should load custom profile from file")
        void shouldLoadCustomProfile() throws IOException {
            // Given
            String customProfileContent = """
                    profiles:
                      custom-test:
                        description: "Custom test profile"
                        playbooks:
                          - HappyPath
                          - SqlInjectionInStringFields
                    """;

            Path tempFile = Files.createTempFile("custom-profile", ".yml");
            Files.writeString(tempFile, customProfileContent);

            try {
                // When
                profileLoader.loadCustomProfiles(tempFile);
                Optional<ProfileLoader.Profile> profile = profileLoader.getProfile("custom-test");

                // Then
                Assertions.assertThat(profile).isPresent();
                Assertions.assertThat(profile.get().name()).isEqualTo("custom-test");
                Assertions.assertThat(profile.get().description()).isEqualTo("Custom test profile");
                Assertions.assertThat(profile.get().playbooks())
                        .containsExactly("HappyPath", "SqlInjectionInStringFields");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("Should override built-in profile with custom profile")
        void shouldOverrideBuiltInProfile() throws IOException {
            // Given
            String customProfileContent = """
                    profiles:
                      security:
                        description: "My custom security profile"
                        playbooks:
                          - HappyPath
                    """;

            Path tempFile = Files.createTempFile("override-profile", ".yml");
            Files.writeString(tempFile, customProfileContent);

            try {
                // When
                profileLoader.loadCustomProfiles(tempFile);
                Optional<ProfileLoader.Profile> profile = profileLoader.getProfile("security");

                // Then
                Assertions.assertThat(profile).isPresent();
                Assertions.assertThat(profile.get().description()).isEqualTo("My custom security profile");
                Assertions.assertThat(profile.get().playbooks()).containsExactly("HappyPath");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }

        @Test
        @DisplayName("Should load multiple custom profiles")
        void shouldLoadMultipleCustomProfiles() throws IOException {
            // Given
            String customProfileContent = """
                    profiles:
                      profile1:
                        description: "First profile"
                        playbooks:
                          - HappyPath
                      profile2:
                        description: "Second profile"
                        playbooks:
                          - RemoveFields
                    """;

            Path tempFile = Files.createTempFile("multi-profile", ".yml");
            Files.writeString(tempFile, customProfileContent);

            try {
                // When
                profileLoader.loadCustomProfiles(tempFile);

                // Then
                Assertions.assertThat(profileLoader.getAvailableProfiles())
                        .contains("profile1", "profile2");

                Optional<ProfileLoader.Profile> profile1 = profileLoader.getProfile("profile1");
                Optional<ProfileLoader.Profile> profile2 = profileLoader.getProfile("profile2");

                Assertions.assertThat(profile1).isPresent();
                Assertions.assertThat(profile2).isPresent();
                Assertions.assertThat(profile1.get().playbooks()).containsExactly("HappyPath");
                Assertions.assertThat(profile2.get().playbooks()).containsExactly("RemoveFields");
            } finally {
                Files.deleteIfExists(tempFile);
            }
        }
    }
}

