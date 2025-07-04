package com.github.tky0065.apigenerator.config;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration globale pour le plugin API Generator.
 * Stocke les préférences de l'utilisateur pour la génération de code.
 */
@Data
@NoArgsConstructor
public class ApiGeneratorConfig {
    // Options de génération
    private boolean generateDto = true;
    private boolean generateRepository = true;
    private boolean generateService = true;
    private boolean generateController = true;
    private boolean generateMapper = true;

    // Préfixes et suffixes
    private String dtoSuffix = "Dto";
    private String repositorySuffix = "Repository";
    private String serviceSuffix = "Service";
    private String controllerSuffix = "Controller";
    private String mapperSuffix = "Mapper";

    // Packages
    private String basePackage = "";
    private String dtoPackage = "dto";
    private String repositoryPackage = "repository";
    private String servicePackage = "service";
    private String controllerPackage = "controller";
    private String mapperPackage = "mapper";

    // Options avancées
    private boolean useLombok = true;
    private boolean overwriteExistingFiles = false;
}
