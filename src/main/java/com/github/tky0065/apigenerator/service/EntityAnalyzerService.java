package com.github.tky0065.apigenerator.service;

import com.github.tky0065.apigenerator.model.EntityModel;
import com.intellij.psi.PsiClass;

/**
 * Service responsable de l'analyse des classes Java pour identifier et extraire
 * les informations des entités JPA.
 */
public interface EntityAnalyzerService {

    /**
     * Vérifie si la classe donnée est une entité JPA valide.
     *
     * @param psiClass la classe à vérifier
     * @return true si la classe est une entité JPA valide
     */
    boolean isJpaEntity(PsiClass psiClass);

    /**
     * Analyse une classe PSI et extrait les informations nécessaires pour construire
     * un modèle d'entité.
     *
     * @param psiClass la classe PSI à analyser (doit être une entité JPA)
     * @return le modèle de l'entité avec ses champs et propriétés
     * @throws IllegalArgumentException si la classe n'est pas une entité JPA
     */
    EntityModel analyzeEntity(PsiClass psiClass);
}
