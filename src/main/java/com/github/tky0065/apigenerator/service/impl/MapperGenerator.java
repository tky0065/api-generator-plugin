package com.github.tky0065.apigenerator.service.impl;

import com.github.tky0065.apigenerator.config.ApiGeneratorConfig;
import com.github.tky0065.apigenerator.model.EntityModel;
import com.github.tky0065.apigenerator.service.CodeGenerator;
import com.intellij.openapi.project.Project;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;

/**
 * Générateur de Mapper entre entités et DTOs utilisant MapStruct.
 */
public class MapperGenerator implements CodeGenerator {

    @Override
    public String generateCode(Project project, EntityModel entityModel, ApiGeneratorConfig config) {
        if (!config.isGenerateDto()) {
            // Si les DTOs ne sont pas générés, inutile de générer les mappers
            return null;
        }

        // Obtenir le nom complet de la classe d'entité
        ClassName entityClassName = ClassName.get(entityModel.getPackageName(), entityModel.getClassName());

        // Obtenir le nom complet de la classe DTO
        String dtoPackageName = getDtoPackageName(entityModel, config);
        String dtoClassName = entityModel.getClassName() + config.getDtoSuffix();
        ClassName dtoTypeName = ClassName.get(dtoPackageName, dtoClassName);

        // Créer l'interface Mapper
        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(getGeneratedClassName(entityModel, config))
                .addModifiers(Modifier.PUBLIC);

        // Ajouter les annotations MapStruct
        ClassName mapperAnnotation = ClassName.get("org.mapstruct", "Mapper");
        AnnotationSpec.Builder mapperAnnotationBuilder = AnnotationSpec.builder(mapperAnnotation)
                .addMember("componentModel", "$S", "spring");

        interfaceBuilder.addAnnotation(mapperAnnotationBuilder.build());

        // Ajouter les méthodes de mapping entre entité et DTO
        addEntityToDto(interfaceBuilder, entityClassName, dtoTypeName);
        addDtoToEntity(interfaceBuilder, entityClassName, dtoTypeName);
        addEntityListToDto(interfaceBuilder, entityClassName, dtoTypeName);
        addDtoListToEntity(interfaceBuilder, entityClassName, dtoTypeName);

        // Créer le fichier Java
        JavaFile javaFile = JavaFile.builder(getGeneratedPackageName(entityModel, config), interfaceBuilder.build())
                .indent("    ")
                .build();

        return javaFile.toString();
    }

    @Override
    public String getGeneratedClassName(EntityModel entityModel, ApiGeneratorConfig config) {
        return entityModel.getClassName() + config.getMapperSuffix();
    }

    @Override
    public String getGeneratedPackageName(EntityModel entityModel, ApiGeneratorConfig config) {
        String basePackage = config.getBasePackage();
        if (basePackage == null || basePackage.isEmpty()) {
            basePackage = entityModel.getPackageName();
        }

        if (basePackage.endsWith(".")) {
            return basePackage + config.getMapperPackage();
        } else {
            return basePackage + "." + config.getMapperPackage();
        }
    }

    /**
     * Obtient le nom du package pour le DTO.
     */
    private String getDtoPackageName(EntityModel entityModel, ApiGeneratorConfig config) {
        String basePackage = config.getBasePackage();
        if (basePackage == null || basePackage.isEmpty()) {
            basePackage = entityModel.getPackageName();
        }

        if (basePackage.endsWith(".")) {
            return basePackage + config.getDtoPackage();
        } else {
            return basePackage + "." + config.getDtoPackage();
        }
    }

    /**
     * Ajoute une méthode pour convertir une entité en DTO.
     */
    private void addEntityToDto(TypeSpec.Builder interfaceBuilder, ClassName entityType, ClassName dtoType) {
        MethodSpec entityToDto = MethodSpec.methodBuilder("toDto")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(entityType, "entity")
                .returns(dtoType)
                .build();

        interfaceBuilder.addMethod(entityToDto);
    }

    /**
     * Ajoute une méthode pour convertir un DTO en entité.
     */
    private void addDtoToEntity(TypeSpec.Builder interfaceBuilder, ClassName entityType, ClassName dtoType) {
        MethodSpec dtoToEntity = MethodSpec.methodBuilder("toEntity")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(dtoType, "dto")
                .returns(entityType)
                .build();

        interfaceBuilder.addMethod(dtoToEntity);
    }

    /**
     * Ajoute une méthode pour convertir une liste d'entités en liste de DTOs.
     */
    private void addEntityListToDto(TypeSpec.Builder interfaceBuilder, ClassName entityType, ClassName dtoType) {
        // Créer les types paramétrés pour les listes
        ClassName listClassName = ClassName.get("java.util", "List");
        TypeName entityListType = ParameterizedTypeName.get(listClassName, entityType);
        TypeName dtoListType = ParameterizedTypeName.get(listClassName, dtoType);

        MethodSpec entityListToDto = MethodSpec.methodBuilder("toDtoList")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(entityListType, "entities")
                .returns(dtoListType)
                .build();

        interfaceBuilder.addMethod(entityListToDto);
    }

    /**
     * Ajoute une méthode pour convertir une liste de DTOs en liste d'entités.
     */
    private void addDtoListToEntity(TypeSpec.Builder interfaceBuilder, ClassName entityType, ClassName dtoType) {
        // Créer les types paramétrés pour les listes
        ClassName listClassName = ClassName.get("java.util", "List");
        TypeName entityListType = ParameterizedTypeName.get(listClassName, entityType);
        TypeName dtoListType = ParameterizedTypeName.get(listClassName, dtoType);

        MethodSpec dtoListToEntity = MethodSpec.methodBuilder("toEntityList")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(dtoListType, "dtos")
                .returns(entityListType)
                .build();

        interfaceBuilder.addMethod(dtoListToEntity);
    }
}
