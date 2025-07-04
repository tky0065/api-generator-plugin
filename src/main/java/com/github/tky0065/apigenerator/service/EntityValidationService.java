package com.github.tky0065.apigenerator.service;

import com.github.tky0065.apigenerator.model.EntityModel;
import com.intellij.psi.PsiClass;

import java.util.List;

/**
 * Service de validation des entités JPA.
 * Permet de vérifier si une entité est bien formée avant de générer le code.
 */
public interface EntityValidationService {

    /**
     * Résultat de la validation d'une entité.
     */
    class ValidationResult {
        private final boolean valid;
        private final List<String> errors;
        private final List<String> warnings;

        public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public boolean hasWarnings() {
            return warnings != null && !warnings.isEmpty();
        }

        public boolean hasErrors() {
            return errors != null && !errors.isEmpty();
        }
    }

    /**
     * Valide une classe d'entité JPA.
     *
     * @param psiClass La classe à valider
     * @return Le résultat de la validation
     */
    ValidationResult validateEntity(PsiClass psiClass);

    /**
     * Valide un modèle d'entité.
     *
     * @param entityModel Le modèle à valider
     * @return Le résultat de la validation
     */
    ValidationResult validateEntityModel(EntityModel entityModel);
}
