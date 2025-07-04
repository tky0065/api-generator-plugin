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
 * Générateur de Controller REST avec endpoints CRUD.
 */
public class ControllerGenerator implements CodeGenerator {

    @Override
    public String generateCode(Project project, EntityModel entityModel, ApiGeneratorConfig config) {
        // Déterminer le type de la clé primaire
        TypeName idType = getIdTypeName(entityModel);

        // Obtenir le nom complet de la classe d'entité
        ClassName entityClassName = ClassName.get(entityModel.getPackageName(), entityModel.getClassName());

        // Obtenir le nom complet de la classe DTO si utilisée
        TypeName dtoTypeName = null;
        if (config.isGenerateDto()) {
            String dtoPackageName = getDtoPackageName(entityModel, config);
            String dtoClassName = entityModel.getClassName() + config.getDtoSuffix();
            dtoTypeName = ClassName.get(dtoPackageName, dtoClassName);
        } else {
            dtoTypeName = entityClassName;
        }

        // Obtenir le nom complet de la classe Service
        String servicePackageName = getServicePackageName(entityModel, config);
        String serviceClassName = entityModel.getClassName() + config.getServiceSuffix();
        ClassName serviceTypeName = ClassName.get(servicePackageName, serviceClassName);

        // Créer la classe Controller
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(getGeneratedClassName(entityModel, config))
                .addModifiers(Modifier.PUBLIC);

        // Ajouter les annotations Spring
        ClassName restControllerAnnotation = ClassName.get("org.springframework.web.bind.annotation", "RestController");
        classBuilder.addAnnotation(restControllerAnnotation);

        String basePath = "/" + entityModel.getClassName().toLowerCase() + "s";
        AnnotationSpec.Builder requestMappingAnnotation = AnnotationSpec.builder(
                ClassName.get("org.springframework.web.bind.annotation", "RequestMapping"))
                .addMember("value", "$S", basePath);
        classBuilder.addAnnotation(requestMappingAnnotation.build());

        // Ajouter l'injection du Service
        FieldSpec serviceField = FieldSpec.builder(serviceTypeName, "service", Modifier.PRIVATE, Modifier.FINAL)
                .build();
        classBuilder.addField(serviceField);

        // Ajouter un constructeur pour l'injection de dépendances
        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(serviceTypeName, "service")
                .addStatement("this.$N = $N", "service", "service")
                .build();
        classBuilder.addMethod(constructor);

        // Ajouter les méthodes REST
        addGetAllMethod(classBuilder, entityClassName, dtoTypeName);
        addGetByIdMethod(classBuilder, entityClassName, dtoTypeName, idType);
        addCreateMethod(classBuilder, entityClassName, dtoTypeName);
        addUpdateMethod(classBuilder, entityClassName, dtoTypeName, idType);
        addDeleteMethod(classBuilder, idType);

        // Créer le fichier Java
        JavaFile javaFile = JavaFile.builder(getGeneratedPackageName(entityModel, config), classBuilder.build())
                .indent("    ")
                .build();

        return javaFile.toString();
    }

    @Override
    public String getGeneratedClassName(EntityModel entityModel, ApiGeneratorConfig config) {
        return entityModel.getClassName() + config.getControllerSuffix();
    }

    @Override
    public String getGeneratedPackageName(EntityModel entityModel, ApiGeneratorConfig config) {
        String basePackage = config.getBasePackage();
        if (basePackage == null || basePackage.isEmpty()) {
            basePackage = entityModel.getPackageName();
        }

        if (basePackage.endsWith(".")) {
            return basePackage + config.getControllerPackage();
        } else {
            return basePackage + "." + config.getControllerPackage();
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
     * Obtient le nom du package pour le Service.
     */
    private String getServicePackageName(EntityModel entityModel, ApiGeneratorConfig config) {
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
     * Ajoute la méthode GET pour récupérer toutes les entités.
     */
    private void addGetAllMethod(TypeSpec.Builder classBuilder, TypeName entityType, TypeName dtoType) {
        TypeName returnType = ParameterizedTypeName.get(
                ClassName.get("org.springframework.http", "ResponseEntity"),
                ParameterizedTypeName.get(ClassName.get(List.class), dtoType));

        ClassName getMappingAnnotation = ClassName.get("org.springframework.web.bind.annotation", "GetMapping");

        MethodSpec getAllMethod = MethodSpec.methodBuilder("getAll")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(getMappingAnnotation)
                .returns(returnType)
                .addStatement("List<$T> entities = service.findAll()", entityType)
                .addStatement("// Ici, vous devez convertir les entités en DTOs si nécessaire")
                .addStatement("return $T.ok(entities)", ClassName.get("org.springframework.http", "ResponseEntity"))
                .build();

        classBuilder.addMethod(getAllMethod);
    }

    /**
     * Ajoute la méthode GET pour récupérer une entité par son ID.
     */
    private void addGetByIdMethod(TypeSpec.Builder classBuilder, TypeName entityType, TypeName dtoType, TypeName idType) {
        TypeName returnType = ParameterizedTypeName.get(
                ClassName.get("org.springframework.http", "ResponseEntity"),
                dtoType);

        ClassName getMapping = ClassName.get("org.springframework.web.bind.annotation", "GetMapping");
        AnnotationSpec getMappingAnnotation = AnnotationSpec.builder(getMapping)
                .addMember("value", "$S", "/{id}")
                .build();

        ClassName pathVariable = ClassName.get("org.springframework.web.bind.annotation", "PathVariable");

        MethodSpec getByIdMethod = MethodSpec.methodBuilder("getById")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(getMappingAnnotation)
                .addParameter(ParameterSpec.builder(idType, "id")
                        .addAnnotation(pathVariable)
                        .build())
                .returns(returnType)
                .addStatement("return service.findById(id)")
                .addStatement("    .map(entity -> $T.ok(entity))", ClassName.get("org.springframework.http", "ResponseEntity"))
                .addStatement("    .orElseGet(() -> $T.notFound().build())", ClassName.get("org.springframework.http", "ResponseEntity"))
                .build();

        classBuilder.addMethod(getByIdMethod);
    }

    /**
     * Ajoute la méthode POST pour créer une nouvelle entité.
     */
    private void addCreateMethod(TypeSpec.Builder classBuilder, TypeName entityType, TypeName dtoType) {
        TypeName returnType = ParameterizedTypeName.get(
                ClassName.get("org.springframework.http", "ResponseEntity"),
                dtoType);

        ClassName postMapping = ClassName.get("org.springframework.web.bind.annotation", "PostMapping");
        ClassName requestBody = ClassName.get("org.springframework.web.bind.annotation", "RequestBody");

        MethodSpec createMethod = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(postMapping)
                .addParameter(ParameterSpec.builder(dtoType, "dto")
                        .addAnnotation(requestBody)
                        .build())
                .returns(returnType)
                .addStatement("// Ici, vous devez convertir le DTO en entité si nécessaire")
                .addStatement("$T savedEntity = service.save(($T) dto)", entityType, entityType)
                .addStatement("// Puis reconvertir en DTO pour la réponse")
                .addStatement("return $T.created(null).body(($T) savedEntity)",
                        ClassName.get("org.springframework.http", "ResponseEntity"), dtoType)
                .build();

        classBuilder.addMethod(createMethod);
    }

    /**
     * Ajoute la méthode PUT pour mettre à jour une entité existante.
     */
    private void addUpdateMethod(TypeSpec.Builder classBuilder, TypeName entityType, TypeName dtoType, TypeName idType) {
        TypeName returnType = ParameterizedTypeName.get(
                ClassName.get("org.springframework.http", "ResponseEntity"),
                dtoType);

        ClassName putMapping = ClassName.get("org.springframework.web.bind.annotation", "PutMapping");
        AnnotationSpec putMappingAnnotation = AnnotationSpec.builder(putMapping)
                .addMember("value", "$S", "/{id}")
                .build();

        ClassName pathVariable = ClassName.get("org.springframework.web.bind.annotation", "PathVariable");
        ClassName requestBody = ClassName.get("org.springframework.web.bind.annotation", "RequestBody");

        MethodSpec updateMethod = MethodSpec.methodBuilder("update")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(putMappingAnnotation)
                .addParameter(ParameterSpec.builder(idType, "id")
                        .addAnnotation(pathVariable)
                        .build())
                .addParameter(ParameterSpec.builder(dtoType, "dto")
                        .addAnnotation(requestBody)
                        .build())
                .returns(returnType)
                .addStatement("return service.findById(id)")
                .addStatement("    .map(existingEntity -> {")
                .addStatement("        // Ici, mettre à jour l'entité existante avec les valeurs du DTO")
                .addStatement("        $T updatedEntity = service.save(existingEntity)", entityType)
                .addStatement("        return $T.ok(($T) updatedEntity)",
                        ClassName.get("org.springframework.http", "ResponseEntity"), dtoType)
                .addStatement("    })")
                .addStatement("    .orElseGet(() -> $T.notFound().build())",
                        ClassName.get("org.springframework.http", "ResponseEntity"))
                .build();

        classBuilder.addMethod(updateMethod);
    }

    /**
     * Ajoute la méthode DELETE pour supprimer une entité.
     */
    private void addDeleteMethod(TypeSpec.Builder classBuilder, TypeName idType) {
        TypeName returnType = ParameterizedTypeName.get(
                ClassName.get("org.springframework.http", "ResponseEntity"),
                ClassName.get("java.lang", "Void"));

        ClassName deleteMapping = ClassName.get("org.springframework.web.bind.annotation", "DeleteMapping");
        AnnotationSpec deleteMappingAnnotation = AnnotationSpec.builder(deleteMapping)
                .addMember("value", "$S", "/{id}")
                .build();

        ClassName pathVariable = ClassName.get("org.springframework.web.bind.annotation", "PathVariable");

        MethodSpec deleteMethod = MethodSpec.methodBuilder("delete")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(deleteMappingAnnotation)
                .addParameter(ParameterSpec.builder(idType, "id")
                        .addAnnotation(pathVariable)
                        .build())
                .returns(returnType)
                .addStatement("service.deleteById(id)")
                .addStatement("return $T.noContent().build()",
                        ClassName.get("org.springframework.http", "ResponseEntity"))
                .build();

        classBuilder.addMethod(deleteMethod);
    }
}
