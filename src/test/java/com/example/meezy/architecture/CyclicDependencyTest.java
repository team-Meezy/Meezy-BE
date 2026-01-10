package com.example.meezy.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.alwaysTrue;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@DisplayName("순환 의존성 테스트")
class CyclicDependencyTest {

    private static JavaClasses classes;

    private static final String BASE_PACKAGE = "com.example.meezy";

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(BASE_PACKAGE);
    }

    @Test
    @DisplayName("패키지 간 순환 의존성이 없어야 한다")
    void no_cyclic_dependencies_between_packages() {
        slices()
                .matching(BASE_PACKAGE + ".(*)..")
                .should().beFreeOfCycles()
                .because("패키지 간 순환 의존성이 없어야 합니다")
                .check(classes);
    }

    @Test
    @DisplayName("Bounded Context 간 순환 의존성이 없어야 한다 (sharedkernel 제외)")
    void no_cyclic_dependencies_between_bounded_contexts() {
        // sharedkernel은 모든 BC에서 공유되는 패키지이므로 순환 의존성 검사에서 제외
        slices()
                .matching(BASE_PACKAGE + ".bc.(*)..")
                .should().beFreeOfCycles()
                .ignoreDependency(
                        resideInAPackage("..sharedkernel.."),
                        alwaysTrue()
                )
                .ignoreDependency(
                        alwaysTrue(),
                        resideInAPackage("..sharedkernel..")
                )
                .because("Bounded Context 간 순환 의존성이 없어야 합니다 (sharedkernel은 공유 패키지로 제외)")
                .check(classes);
    }

    @Test
    @DisplayName("Application Service 간 순환 의존성이 없어야 한다")
    void no_cyclic_dependencies_between_services() {
        slices()
                .matching(BASE_PACKAGE + ".bc.user.application.service.(*)")
                .should().beFreeOfCycles()
                .because("Application Service 간 순환 의존성이 없어야 합니다")
                .check(classes);
    }
}
