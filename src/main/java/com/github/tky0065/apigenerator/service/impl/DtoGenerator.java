package com.github.tky0065.apigenerator.service.impl;

import com.github.tky0065.apigenerator.config.ApiGeneratorConfig;
import com.github.tky0065.apigenerator.model.EntityModel;
import com.github.tky0065.apigenerator.service.CodeGenerator;
import com.intellij.openapi.project.Project;
import com.squareup.javapoet.*;

import javax.lang.model.element.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Générateur de DTO (Data Transfer Object) avec support Lombok.
 */
public class DtoGenerator implements CodeGenerator {

    @Override
    public String generateCode(Project project, EntityModel entityModel, ApiGeneratorConfig config) {
        // Créer la classe DTO
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(getGeneratedClassName(entityModel, config))
                .addModifiers(Modifier.PUBLIC);

        // Ajouter les annotations Lombok si activé
        if (config.isUseLombok()) {
            ClassName dataAnnotation = ClassName.get("lombok", "Data");
            ClassName noArgsConstructorAnnotation = ClassName.get("lombok", "NoArgsConstructor");
            ClassName allArgsConstructorAnnotation = ClassName.get("lombok", "AllArgsConstructor");
            ClassName builderAnnotation = ClassName.get("lombok", "Builder");

            classBuilder.addAnnotation(dataAnnotation);
            classBuilder.addAnnotation(noArgsConstructorAnnotation);
            classBuilder.addAnnotation(allArgsConstructorAnnotation);
            classBuilder.addAnnotation(builderAnnotation);
        }

        // Ajouter les champs
        for (EntityModel.EntityField field : entityModel.getFields()) {
            // Ignorer les champs transient et les relations OneToMany/ManyToMany pour éviter les références circulaires
            if (field.isTransient() || "OneToMany".equals(field.getRelationshipType()) || "ManyToMany".equals(field.getRelationshipType())) {
                continue;
            }

            // Pour les relations ManyToOne et OneToOne, utiliser un ID au lieu de l'entité complète
            if ("ManyToOne".equals(field.getRelationshipType()) || "OneToOne".equals(field.getRelationshipType())) {
                // Utiliser Long comme type pour les références d'ID
                addField(classBuilder, field.getName() + "Id", TypeName.LONG.box(), config.isUseLombok());
                continue;
            }

            // Gérer les collections
            if (field.isCollection() && field.getGenericType() != null) {
                // Pour les collections, créer un type générique (List<String>, Set<Integer>, etc.)
                ClassName collectionType = ClassName.get("java.util", field.getCollectionType());
                TypeName genericType = determineTypeName(field.getGenericType());
                TypeName parameterizedTypeName = ParameterizedTypeName.get(collectionType, genericType);

                addField(classBuilder, field.getName(), parameterizedTypeName, config.isUseLombok());
            } else {
                // Pour les types simples
                TypeName typeName = determineTypeName(field.getType());
                addField(classBuilder, field.getName(), typeName, config.isUseLombok());
            }
        }

        // Générer les getters et setters si Lombok n'est pas utilisé
        if (!config.isUseLombok()) {
            generateGettersAndSetters(classBuilder, entityModel);
        }

        // Créer le fichier Java
        JavaFile javaFile = JavaFile.builder(getGeneratedPackageName(entityModel, config), classBuilder.build())
                .indent("    ")
                .build();

        return javaFile.toString();
    }

    @Override
    public String getGeneratedClassName(EntityModel entityModel, ApiGeneratorConfig config) {
        return entityModel.getClassName() + config.getDtoSuffix();
    }

    @Override
    public String getGeneratedPackageName(EntityModel entityModel, ApiGeneratorConfig config) {
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

    private void addField(TypeSpec.Builder classBuilder, String name, TypeName typeName, boolean useLombok) {
        // Vérifier si un champ avec ce nom existe déjà pour éviter les doublons
        for (FieldSpec field : classBuilder.fieldSpecs) {
            if (field.name.equals(name)) {
                return; // Ne pas ajouter de doublons
            }
        }

        FieldSpec.Builder fieldBuilder = FieldSpec.builder(typeName, name, Modifier.PRIVATE);
        classBuilder.addField(fieldBuilder.build());
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

    private void generateGettersAndSetters(TypeSpec.Builder classBuilder, EntityModel entityModel) {
        // Si Lombok n'est pas utilisé, générer les getters et setters manuellement
        for (EntityModel.EntityField field : entityModel.getFields()) {
            if (field.isTransient() || "OneToMany".equals(field.getRelationshipType()) || "ManyToMany".equals(field.getRelationshipType())) {
                continue;
            }

            String fieldName = field.getName();
            String capitalizedName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            TypeName typeName;

            // Ajuster le nom et le type pour les relations
            if ("ManyToOne".equals(field.getRelationshipType()) || "OneToOne".equals(field.getRelationshipType())) {
                fieldName = fieldName + "Id";
                capitalizedName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
                typeName = TypeName.LONG.box();
            } else if (field.isCollection() && field.getGenericType() != null) {
                ClassName collectionType = ClassName.get("java.util", field.getCollectionType());
                TypeName genericType = determineTypeName(field.getGenericType());
                typeName = ParameterizedTypeName.get(collectionType, genericType);
            } else {
                typeName = determineTypeName(field.getType());
            }

            // Getter
            MethodSpec getter = MethodSpec.methodBuilder("get" + capitalizedName)
                    .addModifiers(Modifier.PUBLIC)
                    .returns(typeName)
                    .addStatement("return this.$N", fieldName)
                    .build();

            // Setter
            MethodSpec setter = MethodSpec.methodBuilder("set" + capitalizedName)
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(typeName, fieldName)
                    .addStatement("this.$N = $N", fieldName, fieldName)
                    .build();

            classBuilder.addMethod(getter);
            classBuilder.addMethod(setter);
        }
    }
}
