package com.github.tky0065.apigenerator.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Représente une entité JPA analysée avec ses propriétés.
 * Cette classe sert de modèle pour la génération de code.
 */
@Data
@Builder
public class EntityModel {
    private String className;
    private String packageName;
    private String qualifiedName;
    private String tableName;
    private List<EntityField> fields;
    private EntityField idField;

    /**
     * Représente un champ d'une entité JPA.
     */
    @Data
    @Builder
    public static class EntityField {
        private String name;
        private String type;
        private String qualifiedType;
        private String columnName;
        private boolean isPrimitive;
        private boolean isCollection;
        private boolean isEnum;
        private boolean isId;
        private boolean isVersion;
        private boolean isTransient;
        private String relationshipType;  // OneToMany, ManyToOne, etc.
        private String collectionType;    // Si c'est une collection, le type de la collection (List, Set, etc.)
        private String genericType;       // Si c'est une collection, le type générique
    }
}
