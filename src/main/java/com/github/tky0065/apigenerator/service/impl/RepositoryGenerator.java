package com.github.tky0065.apigenerator.service.impl;

import com.github.tky0065.apigenerator.config.ApiGeneratorConfig;
import com.github.tky0065.apigenerator.model.EntityModel;
import com.github.tky0065.apigenerator.service.CodeGenerator;
import com.intellij.openapi.project.Project;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

/**
 * Générateur d'interfaces Repository JPA.
 */
public class RepositoryGenerator implements CodeGenerator {

    @Override
    public String generateCode(Project project, EntityModel entityModel, ApiGeneratorConfig config) {
        // Déterminer le type de la clé primaire
        TypeName idType = getIdTypeName(entityModel);

        // Créer l'interface Repository
        ClassName entityClassName = ClassName.get(entityModel.getPackageName(), entityModel.getClassName());
        ClassName jpaRepositoryClassName = ClassName.get("org.springframework.data.jpa.repository", "JpaRepository");

        // Créer le type paramétré JpaRepository<Entity, IdType>
        TypeName superInterface = ParameterizedTypeName.get(jpaRepositoryClassName, entityClassName, idType);

        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(getGeneratedClassName(entityModel, config))
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(superInterface);

        // Ajouter des annotations Spring
        ClassName repositoryAnnotation = ClassName.get("org.springframework.stereotype", "Repository");
        interfaceBuilder.addAnnotation(repositoryAnnotation);

        // Ne pas générer de méthodes personnalisées pour éviter les doublons
        // Spring Data JPA génère déjà automatiquement les méthodes de base

        // Créer le fichier Java
        JavaFile javaFile = JavaFile.builder(getGeneratedPackageName(entityModel, config), interfaceBuilder.build())
                .indent("    ")
                .build();

        return javaFile.toString();
    }

    @Override
    public String getGeneratedClassName(EntityModel entityModel, ApiGeneratorConfig config) {
        return entityModel.getClassName() + config.getRepositorySuffix();
    }

    @Override
    public String getGeneratedPackageName(EntityModel entityModel, ApiGeneratorConfig config) {
        String basePackage = config.getBasePackage();
        if (basePackage == null || basePackage.isEmpty()) {
            basePackage = entityModel.getPackageName();
        }

        if (basePackage.endsWith(".")) {
            return basePackage + config.getRepositoryPackage();
        } else {
            return basePackage + "." + config.getRepositoryPackage();
        }
    }

    /**
     * Détermine le type de la clé primaire de l'entité.
     */
    private TypeName getIdTypeName(EntityModel entityModel) {
        // Utiliser le type du champ @Id s'il existe
        if (entityModel.getIdField() != null) {
            String idType = entityModel.getIdField().getType();
            return determineTypeName(idType);
        }

        // Par défaut, utiliser Long
        return ClassName.get("java.lang", "Long");
    }

    private TypeName determineTypeName(String type) {
        switch (type) {
            case "int": return TypeName.INT;
            case "long": return TypeName.LONG;
            case "double": return TypeName.DOUBLE;
            case "float": return TypeName.FLOAT;
            case "boolean": return TypeName.BOOLEAN;
            case "byte": return TypeName.BYTE;
            case "char": return TypeName.CHAR;
            case "short": return TypeName.SHORT;
            case "Integer": return ClassName.get("java.lang", "Integer");
            case "Long": return ClassName.get("java.lang", "Long");
            case "Double": return ClassName.get("java.lang", "Double");
            case "Float": return ClassName.get("java.lang", "Float");
            case "Boolean": return ClassName.get("java.lang", "Boolean");
            case "Byte": return ClassName.get("java.lang", "Byte");
            case "Character": return ClassName.get("java.lang", "Character");
            case "Short": return ClassName.get("java.lang", "Short");
            case "String": return ClassName.get("java.lang", "String");
            case "BigDecimal": return ClassName.get("java.math", "BigDecimal");
            case "BigInteger": return ClassName.get("java.math", "BigInteger");
            case "LocalDate": return ClassName.get("java.time", "LocalDate");
            case "LocalDateTime": return ClassName.get("java.time", "LocalDateTime");
            case "LocalTime": return ClassName.get("java.time", "LocalTime");
            case "ZonedDateTime": return ClassName.get("java.time", "ZonedDateTime");
            case "OffsetDateTime": return ClassName.get("java.time", "OffsetDateTime");
            case "UUID": return ClassName.get("java.util", "UUID");
            default: return ClassName.bestGuess(type);
        }
    }
}
