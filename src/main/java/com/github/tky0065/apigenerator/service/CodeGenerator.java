package com.github.tky0065.apigenerator.service;

import com.github.tky0065.apigenerator.config.ApiGeneratorConfig;
import com.github.tky0065.apigenerator.model.EntityModel;
import com.intellij.openapi.project.Project;

/**
 * Interface pour les générateurs de code.
 * Chaque type de composant (DTO, Repository, Service, Controller, etc.) aura sa propre implémentation.
 */
public interface CodeGenerator {

    /**
     * Génère le code source pour un composant spécifique à partir d'un modèle d'entité.
     *
     * @param project le projet IntelliJ courant
     * @param entityModel le modèle de l'entité analysée
     * @param config la configuration du générateur
     * @return le code source généré
     */
    String generateCode(Project project, EntityModel entityModel, ApiGeneratorConfig config);

    /**
     * Retourne le nom de la classe générée.
     *
     * @param entityModel le modèle de l'entité
     * @param config la configuration du générateur
     * @return le nom de la classe générée
     */
    String getGeneratedClassName(EntityModel entityModel, ApiGeneratorConfig config);

    /**
     * Retourne le package pour la classe générée.
     *
     * @param entityModel le modèle de l'entité
     * @param config la configuration du générateur
     * @return le nom du package pour la classe générée
     */
    String getGeneratedPackageName(EntityModel entityModel, ApiGeneratorConfig config);

    /**
     * Retourne le chemin relatif complet du fichier à générer.
     *
     * @param entityModel le modèle de l'entité
     * @param config la configuration du générateur
     * @return le chemin relatif du fichier (par exemple, "com/example/dto/UserDto.java")
     */
    default String getRelativeFilePath(EntityModel entityModel, ApiGeneratorConfig config) {
        String packagePath = getGeneratedPackageName(entityModel, config).replace('.', '/');
        String className = getGeneratedClassName(entityModel, config);
        return packagePath + "/" + className + ".java";
    }
}
