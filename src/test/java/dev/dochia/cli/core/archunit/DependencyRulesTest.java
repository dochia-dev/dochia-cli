package dev.dochia.cli.core.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import dev.dochia.cli.core.playbook.api.BodyPlaybook;
import dev.dochia.cli.core.playbook.api.FieldPlaybook;
import dev.dochia.cli.core.playbook.api.HeaderPlaybook;
import io.quarkus.test.junit.QuarkusTest;
import io.swagger.v3.oas.models.media.Schema;

import static com.tngtech.archunit.lang.conditions.ArchConditions.have;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.DependencyRules.NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;
import static com.tngtech.archunit.library.GeneralCodingRules.DEPRECATED_API_SHOULD_NOT_BE_USED;
import static com.tngtech.archunit.library.GeneralCodingRules.testClassesShouldResideInTheSamePackageAsImplementation;

@AnalyzeClasses(packages = "dev.dochia.cli")
class DependencyRulesTest {

    @ArchTest
    static final ArchRule noInterfaceNamingForInterfaces =
            noClasses()
                    .that()
                    .areInterfaces()
                    .should()
                    .haveNameMatching(".*Interface");

    @ArchTest
    static final ArchRule interfacesMustNotBePlacedInImplementationPackages =
            noClasses()
                    .that()
                    .resideInAPackage("..impl..")
                    .should()
                    .beInterfaces();

    @ArchTest
    static ArchRule fieldsPlaybooksShouldBeSuffixed =
            classes()
                    .that()
                    .resideInAPackage("..field..")
                    .and()
                    .areAnnotatedWith(FieldPlaybook.class)
                    .should()
                    .haveSimpleNameEndingWith("Playbook");

    @ArchTest
    static ArchRule headersPlaybooksShouldBeSuffixed =
            classes()
                    .that()
                    .resideInAPackage("..header..")
                    .and()
                    .areAnnotatedWith(HeaderPlaybook.class)
                    .should()
                    .haveSimpleNameEndingWith("HeadersPlaybook");

    @ArchTest
    static ArchRule bodyPlaybooksShouldBeSuffixed =
            classes()
                    .that()
                    .resideInAPackage("..body..")
                    .and()
                    .areAnnotatedWith(BodyPlaybook.class)
                    .should()
                    .haveSimpleNameEndingWith("Playbook");

    @ArchTest
    static ArchRule dontRelyOnUpperPackages = NO_CLASSES_SHOULD_DEPEND_UPPER_PACKAGES;

    @ArchTest
    static ArchRule dontUseDeprecatedApis = DEPRECATED_API_SHOULD_NOT_BE_USED;

    @ArchTest
    static ArchRule testClassesInSamePackage = testClassesShouldResideInTheSamePackageAsImplementation();


    @ArchTest
    static final ArchRule noClassShouldDependOnModelUtils =
            noClasses().that()
                    .resideInAPackage("..")
                    .and()
                    .areNotAssignableFrom("dev.dochia.cli.core.util.DochiaModelUtils")
                    .should()
                    .accessClassesThat()
                    .resideInAPackage("org.openapitools.codegen.utils");

    @ArchTest
    static final ArchRule noClassesShouldUseConcreteSchemas =
            noClasses().that()
                    .resideInAPackage("..")
                    .and()
                    .haveSimpleNameNotEndingWith("Test")
                    .and()
                    .areNotAssignableFrom("dev.dochia.cli.core.util.DochiaModelUtils")
                    .and()
                    .areNotAssignableFrom("dev.dochia.cli.core.ReflectionConfig")
                    .should()
                    .dependOnClassesThat(new DescribedPredicate<>("not using extensions of Schema") {
                        @Override
                        public boolean test(JavaClass javaClass) {
                            return !javaClass.getSimpleName().equalsIgnoreCase("Schema") &&
                                    javaClass.isAssignableTo(Schema.class);
                        }
                    });

    @ArchTest
    static final ArchRule allUtilClassesAbstract =
            classes().that().resideInAPackage("dev.dochia.cli.core.util")
                    .and()
                    .haveSimpleNameContaining("Util")
                    .and()
                    .haveSimpleNameNotEndingWith("Test")
                    .should()
                    .haveModifier(JavaModifier.ABSTRACT);

    @ArchTest
    static final ArchRule utilClassesHaveOnlyStaticMethods =
            classes().that().resideInAPackage("dev.dochia.cli.core.util")
                    .and()
                    .haveSimpleNameContaining("Util")
                    .and()
                    .haveSimpleNameNotEndingWith("Test")
                    .should(have(DescribedPredicate.describe("only have static methods",
                            javaClass -> javaClass.getMethods().stream().filter(method -> method.getModifiers().contains(JavaModifier.STATIC)).count() == javaClass.getMethods().size())));

    @ArchTest
    static final ArchRule testClassesAnnotatedWithQuarkusTest = classes()
            .that().haveSimpleNameEndingWith("Test")
            .and().haveSimpleNameNotContaining("DependencyRulesTest")
            .should().beAnnotatedWith(QuarkusTest.class);

}
