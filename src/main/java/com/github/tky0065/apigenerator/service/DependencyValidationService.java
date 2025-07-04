package com.github.tky0065.apigenerator.service;

import com.github.tky0065.apigenerator.config.ApiGeneratorConfig;
import com.intellij.openapi.project.Project;

import java.util.List;
import java.util.Map;

/**
 * Service de validation des dépendances requises dans le projet utilisateur.
 * Permet de vérifier si les bibliothèques nécessaires pour le code généré sont présentes.
 */
public interface DependencyValidationService {

    /**
     * Résultat de la validation des dépendances.
     */
    class DependencyValidationResult {
        private final boolean valid;
        private final Map<String, Boolean> dependencies;
        private final List<String> missingDependencies;

        public DependencyValidationResult(boolean valid, Map<String, Boolean> dependencies, List<String> missingDependencies) {
            this.valid = valid;
            this.dependencies = dependencies;
            this.missingDependencies = missingDependencies;
        }

        public boolean isValid() {
            return valid;
        }

        public Map<String, Boolean> getDependencies() {
            return dependencies;
        }

        public List<String> getMissingDependencies() {
            return missingDependencies;
        }

        public boolean hasMissingDependencies() {
            return missingDependencies != null && !missingDependencies.isEmpty();
        }
    }

    /**
     * Valide les dépendances requises pour le code généré dans le projet utilisateur.
     *
     * @param project Le projet à valider
     * @param config La configuration de génération utilisée
     * @return Le résultat de la validation des dépendances
     */
    DependencyValidationResult validateDependencies(Project project, ApiGeneratorConfig config);

    /**
     * Vérifie si une classe spécifique est disponible dans le classpath du projet.
     *
     * @param project Le projet à vérifier
     * @param className Le nom de la classe à rechercher
     * @return true si la classe est disponible, false sinon
     */
    boolean isClassAvailable(Project project, String className);

    /**
     * Génère une suggestion Maven pour les dépendances manquantes.
     *
     * @param missingDependencies La liste des dépendances manquantes
     * @return Un extrait XML à ajouter au fichier pom.xml
     */
    String generateMavenDependencySuggestions(List<String> missingDependencies);

    /**
     * Génère une suggestion Gradle pour les dépendances manquantes.
     *
     * @param missingDependencies La liste des dépendances manquantes
     * @return Un extrait Groovy à ajouter au fichier build.gradle
     */
    String generateGradleDependencySuggestions(List<String> missingDependencies);
}
