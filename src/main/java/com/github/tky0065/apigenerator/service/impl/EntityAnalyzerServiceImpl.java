package com.github.tky0065.apigenerator.service.impl;

import com.github.tky0065.apigenerator.model.EntityModel;
import com.github.tky0065.apigenerator.service.EntityAnalyzerService;
import com.github.tky0065.apigenerator.util.PsiUtils;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation du service d'analyse d'entités JPA.
 */
public class EntityAnalyzerServiceImpl implements EntityAnalyzerService {

    private static final String JPA_ENTITY_ANNOTATION = "javax.persistence.Entity";
    private static final String JAKARTA_ENTITY_ANNOTATION = "jakarta.persistence.Entity";
    private static final String JPA_ID_ANNOTATION = "javax.persistence.Id";
    private static final String JAKARTA_ID_ANNOTATION = "jakarta.persistence.Id";
    private static final String JPA_TRANSIENT_ANNOTATION = "javax.persistence.Transient";
    private static final String JAKARTA_TRANSIENT_ANNOTATION = "jakarta.persistence.Transient";
    private static final String JPA_VERSION_ANNOTATION = "javax.persistence.Version";
    private static final String JAKARTA_VERSION_ANNOTATION = "jakarta.persistence.Version";
    private static final String JPA_COLUMN_ANNOTATION = "javax.persistence.Column";
    private static final String JAKARTA_COLUMN_ANNOTATION = "jakarta.persistence.Column";
    private static final String JPA_MANY_TO_ONE_ANNOTATION = "javax.persistence.ManyToOne";
    private static final String JAKARTA_MANY_TO_ONE_ANNOTATION = "jakarta.persistence.ManyToOne";
    private static final String JPA_ONE_TO_MANY_ANNOTATION = "javax.persistence.OneToMany";
    private static final String JAKARTA_ONE_TO_MANY_ANNOTATION = "jakarta.persistence.OneToMany";
    private static final String JPA_MANY_TO_MANY_ANNOTATION = "javax.persistence.ManyToMany";
    private static final String JAKARTA_MANY_TO_MANY_ANNOTATION = "jakarta.persistence.ManyToMany";
    private static final String JPA_ONE_TO_ONE_ANNOTATION = "javax.persistence.OneToOne";
    private static final String JAKARTA_ONE_TO_ONE_ANNOTATION = "jakarta.persistence.OneToOne";

    @Override
    public boolean isJpaEntity(PsiClass psiClass) {
        if (psiClass == null || psiClass.isInterface() || psiClass.isEnum() || psiClass.isAnnotationType()) {
            return false;
        }

        return PsiUtils.hasAnnotation(psiClass, JPA_ENTITY_ANNOTATION) ||
               PsiUtils.hasAnnotation(psiClass, JAKARTA_ENTITY_ANNOTATION);
    }

    @Override
    public EntityModel analyzeEntity(PsiClass psiClass) {
        if (!isJpaEntity(psiClass)) {
            throw new IllegalArgumentException("La classe n'est pas une entité JPA valide: " + psiClass.getQualifiedName());
        }

        List<EntityModel.EntityField> fields = new ArrayList<>();
        EntityModel.EntityField idField = null;

        // Collecte tous les champs, y compris ceux hérités
        List<PsiField> allFields = getAllFields(psiClass);

        for (PsiField field : allFields) {
            if (field.hasModifierProperty(PsiModifier.STATIC) || field.hasModifierProperty(PsiModifier.FINAL)) {
                continue;  // Ignorer les champs statiques et finals
            }

            EntityModel.EntityField entityField = analyzeField(field);
            fields.add(entityField);

            if (entityField.isId()) {
                idField = entityField;
            }
        }

        String tableName = extractTableName(psiClass);

        return EntityModel.builder()
                .className(psiClass.getName())
                .packageName(((PsiJavaFile) psiClass.getContainingFile()).getPackageName())
                .qualifiedName(psiClass.getQualifiedName())
                .tableName(tableName)
                .fields(fields)
                .idField(idField)
                .build();
    }

    private String extractTableName(PsiClass psiClass) {
        // Essayer d'extraire le nom de table de l'annotation @Table
        PsiAnnotation tableAnnotation = psiClass.getAnnotation("javax.persistence.Table");
        if (tableAnnotation == null) {
            tableAnnotation = psiClass.getAnnotation("jakarta.persistence.Table");
        }

        if (tableAnnotation != null) {
            String name = PsiUtils.getAnnotationAttributeValue(tableAnnotation, "name");
            if (name != null && !name.isEmpty()) {
                // Supprimer les guillemets si présents
                return name.replaceAll("\"", "");
            }
        }

        // Si pas d'annotation @Table ou pas d'attribut name, utiliser le nom de la classe
        return psiClass.getName().toLowerCase();
    }

    private EntityModel.EntityField analyzeField(PsiField field) {
        PsiType type = field.getType();
        String typeName = type.getPresentableText();
        String qualifiedTypeName = type.getCanonicalText();
        boolean isCollection = PsiUtils.isCollectionType(type);
        boolean isPrimitive = type instanceof PsiPrimitiveType;
        boolean isEnum = isEnumType(type);

        boolean isId = PsiUtils.hasAnnotation(field, JPA_ID_ANNOTATION) ||
                       PsiUtils.hasAnnotation(field, JAKARTA_ID_ANNOTATION);

        boolean isTransient = PsiUtils.hasAnnotation(field, JPA_TRANSIENT_ANNOTATION) ||
                             PsiUtils.hasAnnotation(field, JAKARTA_TRANSIENT_ANNOTATION) ||
                             field.hasModifierProperty(PsiModifier.TRANSIENT);

        boolean isVersion = PsiUtils.hasAnnotation(field, JPA_VERSION_ANNOTATION) ||
                           PsiUtils.hasAnnotation(field, JAKARTA_VERSION_ANNOTATION);

        // Déterminer le type de relation JPA
        String relationshipType = determineRelationshipType(field);

        // Extraire le nom de colonne de l'annotation @Column
        String columnName = extractColumnName(field);

        String collectionType = null;
        String genericType = null;

        if (isCollection) {
            collectionType = extractCollectionType(type);
            genericType = extractGenericType(type);
        }

        return EntityModel.EntityField.builder()
                .name(field.getName())
                .type(typeName)
                .qualifiedType(qualifiedTypeName)
                .columnName(columnName)
                .isPrimitive(isPrimitive)
                .isCollection(isCollection)
                .isEnum(isEnum)
                .isId(isId)
                .isTransient(isTransient)
                .isVersion(isVersion)
                .relationshipType(relationshipType)
                .collectionType(collectionType)
                .genericType(genericType)
                .build();
    }

    private String extractColumnName(PsiField field) {
        // Essayer d'extraire le nom de colonne de l'annotation @Column
        PsiAnnotation columnAnnotation = field.getAnnotation(JPA_COLUMN_ANNOTATION);
        if (columnAnnotation == null) {
            columnAnnotation = field.getAnnotation(JAKARTA_COLUMN_ANNOTATION);
        }

        if (columnAnnotation != null) {
            String name = PsiUtils.getAnnotationAttributeValue(columnAnnotation, "name");
            if (name != null && !name.isEmpty()) {
                // Supprimer les guillemets si présents
                return name.replaceAll("\"", "");
            }
        }

        // Si pas d'annotation @Column ou pas d'attribut name, utiliser le nom du champ
        return field.getName().toLowerCase();
    }

    private String determineRelationshipType(PsiField field) {
        if (PsiUtils.hasAnnotation(field, JPA_MANY_TO_ONE_ANNOTATION) ||
            PsiUtils.hasAnnotation(field, JAKARTA_MANY_TO_ONE_ANNOTATION)) {
            return "ManyToOne";
        }

        if (PsiUtils.hasAnnotation(field, JPA_ONE_TO_MANY_ANNOTATION) ||
            PsiUtils.hasAnnotation(field, JAKARTA_ONE_TO_MANY_ANNOTATION)) {
            return "OneToMany";
        }

        if (PsiUtils.hasAnnotation(field, JPA_MANY_TO_MANY_ANNOTATION) ||
            PsiUtils.hasAnnotation(field, JAKARTA_MANY_TO_MANY_ANNOTATION)) {
            return "ManyToMany";
        }

        if (PsiUtils.hasAnnotation(field, JPA_ONE_TO_ONE_ANNOTATION) ||
            PsiUtils.hasAnnotation(field, JAKARTA_ONE_TO_ONE_ANNOTATION)) {
            return "OneToOne";
        }

        return null;
    }

    private List<PsiField> getAllFields(PsiClass psiClass) {
        List<PsiField> result = new ArrayList<>();
        collectFields(psiClass, result);
        return result;
    }

    private void collectFields(PsiClass psiClass, List<PsiField> fields) {
        if (psiClass == null) {
            return;
        }

        fields.addAll(Arrays.asList(psiClass.getFields()));

        // Récupère également les champs privés
        fields.addAll(Arrays.stream(psiClass.getAllFields())
                .filter(f -> f.hasModifierProperty(PsiModifier.PRIVATE))
                .collect(Collectors.toList()));

        // Parcourir la hiérarchie des classes
        PsiClass superClass = psiClass.getSuperClass();
        if (superClass != null && !"java.lang.Object".equals(superClass.getQualifiedName())) {
            collectFields(superClass, fields);
        }
    }

    private boolean isEnumType(PsiType type) {
        if (!(type instanceof PsiClassType)) {
            return false;
        }

        PsiClass psiClass = ((PsiClassType) type).resolve();
        return psiClass != null && psiClass.isEnum();
    }

    private String extractCollectionType(PsiType type) {
        if (!(type instanceof PsiClassType)) {
            return null;
        }

        PsiClass psiClass = ((PsiClassType) type).resolve();
        if (psiClass == null) {
            return null;
        }

        return psiClass.getName();
    }

    private String extractGenericType(PsiType type) {
        if (!(type instanceof PsiClassType)) {
            return null;
        }

        PsiClassType classType = (PsiClassType) type;
        PsiType[] parameters = classType.getParameters();

        if (parameters.length > 0) {
            return parameters[0].getPresentableText();
        }

        return null;
    }
}
