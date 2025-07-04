package com.github.tky0065.apigenerator.service.impl;

import com.github.tky0065.apigenerator.config.ApiGeneratorConfig;
import com.github.tky0065.apigenerator.model.EntityModel;
import com.github.tky0065.apigenerator.service.CodeGenerator;
import com.intellij.openapi.project.Project;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.Optional;

/**
 * Générateur de classe Service avec opérations CRUD.
 */
public class ServiceGenerator implements CodeGenerator {

    @Override
    public String generateCode(Project project, EntityModel entityModel, ApiGeneratorConfig config) {
        // Déterminer le type de la clé primaire
        TypeName idType = getIdTypeName(entityModel);

        // Obtenir le nom complet de la classe d'entité
        ClassName entityClassName = ClassName.get(entityModel.getPackageName(), entityModel.getClassName());

        // Obtenir le nom complet de la classe Repository
        String repositoryPackageName = getRepositoryPackageName(entityModel, config);
        String repositoryClassName = entityModel.getClassName() + config.getRepositorySuffix();
        ClassName repositoryTypeName = ClassName.get(repositoryPackageName, repositoryClassName);

        // Créer la classe Service
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(getGeneratedClassName(entityModel, config))
                .addModifiers(Modifier.PUBLIC);

        // Ajouter les annotations Spring
        ClassName serviceAnnotation = ClassName.get("org.springframework.stereotype", "Service");
        classBuilder.addAnnotation(serviceAnnotation);

        // Ajouter l'injection du Repository
        FieldSpec repositoryField = FieldSpec.builder(repositoryTypeName, "repository", Modifier.PRIVATE, Modifier.FINAL)
                .build();
        classBuilder.addField(repositoryField);

        // Ajouter un constructeur pour l'injection de dépendances
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(repositoryTypeName, "repository")
                .addStatement("this.$N = $N", "repository", "repository")
                .build();
        classBuilder.addMethod(constructor);

        // Ajouter les méthodes CRUD
        addFindAllMethod(classBuilder, entityClassName);
        addFindByIdMethod(classBuilder, entityClassName, idType);
        addSaveMethod(classBuilder, entityClassName);
        addDeleteMethod(classBuilder, entityClassName, idType);

        // Créer le fichier Java
        JavaFile javaFile = JavaFile.builder(getGeneratedPackageName(entityModel, config), classBuilder.build())
                .indent("    ")
                .build();

        return javaFile.toString();
    }

    @Override
    public String getGeneratedClassName(EntityModel entityModel, ApiGeneratorConfig config) {
        return entityModel.getClassName() + config.getServiceSuffix();
    }

    @Override
    public String getGeneratedPackageName(EntityModel entityModel, ApiGeneratorConfig config) {
        String basePackage = config.getBasePackage();
        if (basePackage == null || basePackage.isEmpty()) {
            basePackage = entityModel.getPackageName();
        }

        if (basePackage.endsWith(".")) {
            return basePackage + config.getServicePackage();
        } else {
            return basePackage + "." + config.getServicePackage();
        }
    }

    /**
     * Obtient le nom du package pour le Repository.
     */
    private String getRepositoryPackageName(EntityModel entityModel, ApiGeneratorConfig config) {
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

    /**
     * Convertit un nom de type Java en TypeName JavaPoet.
     */
    private TypeName determineTypeName(String typeName) {
        switch (typeName) {
            case "boolean": return TypeName.BOOLEAN;
            case "byte": return TypeName.BYTE;
            case "short": return TypeName.SHORT;
            case "int": return TypeName.INT;
            case "long": return TypeName.LONG;
            case "char": return TypeName.CHAR;
            case "float": return TypeName.FLOAT;
            case "double": return TypeName.DOUBLE;
            case "Boolean": return TypeName.BOOLEAN.box();
            case "Byte": return TypeName.BYTE.box();
            case "Short": return TypeName.SHORT.box();
            case "Integer": return TypeName.INT.box();
            case "Long": return TypeName.LONG.box();
            case "Character": return TypeName.CHAR.box();
            case "Float": return TypeName.FLOAT.box();
            case "Double": return TypeName.DOUBLE.box();
            case "String": return ClassName.get("java.lang", "String");
            case "BigDecimal": return ClassName.get("java.math", "BigDecimal");
            case "BigInteger": return ClassName.get("java.math", "BigInteger");
            case "LocalDate": return ClassName.get("java.time", "LocalDate");
            case "LocalTime": return ClassName.get("java.time", "LocalTime");
            case "LocalDateTime": return ClassName.get("java.time", "LocalDateTime");
            case "ZonedDateTime": return ClassName.get("java.time", "ZonedDateTime");
            case "Instant": return ClassName.get("java.time", "Instant");
            default:
                // Pour les autres types, supposer que c'est une classe Java
                // Si le type contient un point, c'est probablement un nom de classe complet
                if (typeName.contains(".")) {
                    String packageName = typeName.substring(0, typeName.lastIndexOf("."));
                    String className = typeName.substring(typeName.lastIndexOf(".") + 1);
                    return ClassName.get(packageName, className);
                } else {
                    return ClassName.get("java.lang", typeName);
                }
        }
    }

    /**
     * Ajoute la méthode pour récupérer toutes les entités.
     */
    private void addFindAllMethod(TypeSpec.Builder classBuilder, TypeName entityType) {
        TypeName returnType = ParameterizedTypeName.get(
                ClassName.get(List.class), entityType);

        MethodSpec findAll = MethodSpec.methodBuilder("findAll")
                .addModifiers(Modifier.PUBLIC)
                .returns(returnType)
                .addStatement("return repository.findAll()")
                .build();

        classBuilder.addMethod(findAll);
    }

    /**
     * Ajoute la méthode pour trouver une entité par son ID.
     */
    private void addFindByIdMethod(TypeSpec.Builder classBuilder, TypeName entityType, TypeName idType) {
        TypeName returnType = ParameterizedTypeName.get(
                ClassName.get(Optional.class), entityType);

        MethodSpec findById = MethodSpec.methodBuilder("findById")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(idType, "id")
                .returns(returnType)
                .addStatement("return repository.findById(id)")
                .build();

        classBuilder.addMethod(findById);
    }

    /**
     * Ajoute la méthode pour sauvegarder une entité.
     */
    private void addSaveMethod(TypeSpec.Builder classBuilder, TypeName entityType) {
        MethodSpec save = MethodSpec.methodBuilder("save")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(entityType, "entity")
                .returns(entityType)
                .addStatement("return repository.save(entity)")
                .build();

        classBuilder.addMethod(save);
    }

    /**
     * Ajoute la méthode pour supprimer une entité par son ID.
     */
    private void addDeleteMethod(TypeSpec.Builder classBuilder, TypeName entityType, TypeName idType) {
        MethodSpec delete = MethodSpec.methodBuilder("deleteById")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(idType, "id")
                .returns(TypeName.VOID)
                .addStatement("repository.deleteById(id)")
                .build();

        classBuilder.addMethod(delete);
    }
}
