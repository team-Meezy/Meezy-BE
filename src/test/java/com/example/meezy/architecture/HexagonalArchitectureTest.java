package com.example.meezy.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

@DisplayName("Hexagonal Architecture 규칙 테스트")
class HexagonalArchitectureTest {

    private static JavaClasses classes;

    private static final String BASE_PACKAGE = "com.example.meezy";
    private static final String DOMAIN_PACKAGE = "..domain..";
    private static final String APPLICATION_PACKAGE = "..application..";
    private static final String INFRASTRUCTURE_PACKAGE = "..infrastructure..";
    private static final String PRESENTATION_PACKAGE = "..presentation..";
    private static final String CONFIG_PACKAGE = "..config..";

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Nested
    @DisplayName("레이어 의존성 규칙")
    class LayerDependencyTest {

        @Test
        @DisplayName("레이어 간 의존성은 안쪽 방향으로만 허용된다")
        void layer_dependencies_are_respected() {
            layeredArchitecture()
                    .consideringAllDependencies()
                    .layer("Domain").definedBy("..domain..")
                    .layer("Application").definedBy("..application..")
                    .layer("Infrastructure").definedBy("..infrastructure..")
                    .layer("Presentation").definedBy("..presentation..")
                    .layer("Config").definedBy("..config..")
                    .layer("SharedKernel").definedBy("..sharedkernel..")

                    .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
                    .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Config")
                    .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation", "Infrastructure", "Config")
                    .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Config", "SharedKernel")
                    .whereLayer("SharedKernel").mayOnlyBeAccessedByLayers("Domain", "Application", "Infrastructure", "Config", "Presentation")

                    .check(classes);
        }

        @Test
        @DisplayName("Domain은 Application 레이어에 의존하지 않는다")
        void domain_should_not_depend_on_application() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(APPLICATION_PACKAGE)
                    .because("Domain 레이어는 Application 레이어에 의존하면 안 됩니다")
                    .check(classes);
        }

        @Test
        @DisplayName("Domain은 Infrastructure 레이어에 의존하지 않는다 (JPA/Lombok 제외)")
        void domain_should_not_depend_on_infrastructure() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .because("Domain 레이어는 Infrastructure 레이어에 의존하면 안 됩니다")
                    .check(classes);
        }

        @Test
        @DisplayName("Domain은 Presentation 레이어에 의존하지 않는다")
        void domain_should_not_depend_on_presentation() {
            noClasses()
                    .that().resideInAPackage(DOMAIN_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(PRESENTATION_PACKAGE)
                    .because("Domain 레이어는 Presentation 레이어에 의존하면 안 됩니다")
                    .check(classes);
        }

        @Test
        @DisplayName("Application은 Infrastructure 레이어에 의존하지 않는다")
        void application_should_not_depend_on_infrastructure() {
            noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(INFRASTRUCTURE_PACKAGE)
                    .because("Application 레이어는 Infrastructure 레이어에 의존하면 안 됩니다")
                    .check(classes);
        }

        @Test
        @DisplayName("Application은 Presentation 레이어에 의존하지 않는다")
        void application_should_not_depend_on_presentation() {
            noClasses()
                    .that().resideInAPackage(APPLICATION_PACKAGE)
                    .should().dependOnClassesThat().resideInAPackage(PRESENTATION_PACKAGE)
                    .because("Application 레이어는 Presentation 레이어에 의존하면 안 됩니다")
                    .check(classes);
        }
    }
}
