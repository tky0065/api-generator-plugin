package com.github.tky0065.apigenerator.service.impl;

import com.github.tky0065.apigenerator.config.ApiGeneratorConfig;
import com.github.tky0065.apigenerator.service.DependencyValidationService;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.*;

/**
 * Implémentation du service de validation des dépendances.
 * Vérifie la présence des bibliothèques nécessaires pour le code généré.
 */
public class DependencyValidationServiceImpl implements DependencyValidationService {

    // Mapping des fonctionnalités avec leurs classes requises
    private static final Map<String, List<String>> REQUIRED_CLASSES_BY_FEATURE = Map.of(
            "JPA Entity", Arrays.asList(
                    "javax.persistence.Entity",
                    "jakarta.persistence.Entity"),
            "Spring Data JPA", Arrays.asList(
                    "org.springframework.data.jpa.repository.JpaRepository"),
            "Spring Web", Arrays.asList(
                    "org.springframework.web.bind.annotation.RestController",
                    "org.springframework.http.ResponseEntity"),
            "MapStruct", Arrays.asList(
                    "org.mapstruct.Mapper"),
            "Lombok", Arrays.asList(
                    "lombok.Data")
    );

    // Mapping des dépendances avec leurs coordonnées Maven
    private static final Map<String, String> MAVEN_DEPENDENCIES = Map.of(
            "JPA Entity", "<dependency>\n    <groupId>jakarta.persistence</groupId>\n    <artifactId>jakarta.persistence-api</artifactId>\n    <version>3.1.0</version>\n</dependency>",
            "Spring Data JPA", "<dependency>\n    <groupId>org.springframework.boot</groupId>\n    <artifactId>spring-boot-starter-data-jpa</artifactId>\n</dependency>",
            "Spring Web", "<dependency>\n    <groupId>org.springframework.boot</groupId>\n    <artifactId>spring-boot-starter-web</artifactId>\n</dependency>",
            "MapStruct", "<dependency>\n    <groupId>org.mapstruct</groupId>\n    <artifactId>mapstruct</artifactId>\n    <version>1.5.3.Final</version>\n</dependency>\n<dependency>\n    <groupId>org.mapstruct</groupId>\n    <artifactId>mapstruct-processor</artifactId>\n    <version>1.5.3.Final</version>\n    <scope>provided</scope>\n</dependency>",
            "Lombok", "<dependency>\n    <groupId>org.projectlombok</groupId>\n    <artifactId>lombok</artifactId>\n    <version>1.18.28</version>\n    <scope>provided</scope>\n</dependency>"
    );

    // Mapping des dépendances avec leurs coordonnées Gradle
    private static final Map<String, String> GRADLE_DEPENDENCIES = Map.of(
            "JPA Entity", "implementation 'jakarta.persistence:jakarta.persistence-api:3.1.0'",
            "Spring Data JPA", "implementation 'org.springframework.boot:spring-boot-starter-data-jpa'",
            "Spring Web", "implementation 'org.springframework.boot:spring-boot-starter-web'",
            "MapStruct", "implementation 'org.mapstruct:mapstruct:1.5.3.Final'\nannotationProcessor 'org.mapstruct:mapstruct-processor:1.5.3.Final'",
            "Lombok", "compileOnly 'org.projectlombok:lombok:1.18.28'\nannotationProcessor 'org.projectlombok:lombok:1.18.28'"
    );

    @Override
    public DependencyValidationResult validateDependencies(Project project, ApiGeneratorConfig config) {
        Map<String, Boolean> dependencyStatus = new HashMap<>();
        List<String> missingDependencies = new ArrayList<>();

        // Vérifier JPA Entity (obligatoire)
        boolean hasJpaEntity = isFeatureAvailable(project, "JPA Entity");
        dependencyStatus.put("JPA Entity", hasJpaEntity);
        if (!hasJpaEntity) {
            missingDependencies.add("JPA Entity");
        }

        // Vérifier Spring Data JPA (obligatoire si on génère des Repository)
        if (config.isGenerateRepository()) {
            boolean hasSpringDataJpa = isFeatureAvailable(project, "Spring Data JPA");
            dependencyStatus.put("Spring Data JPA", hasSpringDataJpa);
            if (!hasSpringDataJpa) {
                missingDependencies.add("Spring Data JPA");
            }
        }

        // Vérifier Spring Web (obligatoire si on génère des Controller)
        if (config.isGenerateController()) {
            boolean hasSpringWeb = isFeatureAvailable(project, "Spring Web");
            dependencyStatus.put("Spring Web", hasSpringWeb);
            if (!hasSpringWeb) {
                missingDependencies.add("Spring Web");
            }
        }

        // Vérifier MapStruct (obligatoire si on génère des Mapper)
        if (config.isGenerateMapper()) {
            boolean hasMapStruct = isFeatureAvailable(project, "MapStruct");
            dependencyStatus.put("MapStruct", hasMapStruct);
            if (!hasMapStruct) {
                missingDependencies.add("MapStruct");
            }
        }

        // Vérifier Lombok (obligatoire si on génère des DTO avec Lombok)
        if (config.isGenerateDto() && config.isUseLombok()) {
            boolean hasLombok = isFeatureAvailable(project, "Lombok");
            dependencyStatus.put("Lombok", hasLombok);
            if (!hasLombok) {
                missingDependencies.add("Lombok");
            }
        }

        return new DependencyValidationResult(missingDependencies.isEmpty(), dependencyStatus, missingDependencies);
    }

    /**
     * Vérifie si une fonctionnalité est disponible dans le classpath du projet.
     */
    private boolean isFeatureAvailable(Project project, String featureName) {
        List<String> classNames = REQUIRED_CLASSES_BY_FEATURE.get(featureName);
        if (classNames == null || classNames.isEmpty()) {
            return true; // Pas de classes requises, on considère la fonctionnalité comme disponible
        }

        // Pour les fonctionnalités avec plusieurs alternatives (ex: javax.persistence ou jakarta.persistence),
        // il suffit qu'une seule alternative soit disponible
        return classNames.stream().anyMatch(className -> isClassAvailable(project, className));
    }

    @Override
    public boolean isClassAvailable(Project project, String className) {
        JavaPsiFacade psiFacade = JavaPsiFacade.getInstance(project);
        Module[] modules = ModuleManager.getInstance(project).getModules();

        for (Module module : modules) {
            GlobalSearchScope scope = GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
            PsiClass psiClass = psiFacade.findClass(className, scope);
            if (psiClass != null) {
                return true;
            }
        }

        return false;
    }

    @Override
    public String generateMavenDependencySuggestions(List<String> missingDependencies) {
        if (missingDependencies == null || missingDependencies.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("<!-- Dépendances manquantes pour le code généré -->\n");

        for (String dependency : missingDependencies) {
            String mavenDependency = MAVEN_DEPENDENCIES.get(dependency);
            if (mavenDependency != null) {
                builder.append(mavenDependency).append("\n");
            }
        }

        return builder.toString();
    }

    @Override
    public String generateGradleDependencySuggestions(List<String> missingDependencies) {
        if (missingDependencies == null || missingDependencies.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("// Dépendances manquantes pour le code généré\n");

        for (String dependency : missingDependencies) {
            String gradleDependency = GRADLE_DEPENDENCIES.get(dependency);
            if (gradleDependency != null) {
                builder.append(gradleDependency).append("\n");
            }
        }

        return builder.toString();
    }
}
