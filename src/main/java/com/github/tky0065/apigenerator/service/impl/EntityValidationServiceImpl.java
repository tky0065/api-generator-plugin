package com.github.tky0065.apigenerator.service.impl;

import com.github.tky0065.apigenerator.model.EntityModel;
import com.github.tky0065.apigenerator.service.EntityValidationService;
import com.github.tky0065.apigenerator.util.PsiUtils;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Implémentation du service de validation des entités JPA.
 * Vérifie qu'une entité respecte les bonnes pratiques et peut être utilisée
 * pour générer une API CRUD complète.
 */
public class EntityValidationServiceImpl implements EntityValidationService {

    @Override
    public ValidationResult validateEntity(PsiClass psiClass) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (psiClass == null) {
            errors.add("La classe est nulle.");
            return new ValidationResult(false, errors, warnings);
        }

        // Vérifier que la classe est annotée avec @Entity
        boolean hasEntityAnnotation = PsiUtils.hasAnnotation(psiClass, "javax.persistence.Entity")
                || PsiUtils.hasAnnotation(psiClass, "jakarta.persistence.Entity");

        if (!hasEntityAnnotation) {
            errors.add("La classe n'est pas annotée avec @Entity.");
        }

        // Vérifier que la classe n'est pas abstraite
        if (psiClass.hasModifierProperty(PsiModifier.ABSTRACT)) {
            errors.add("Les classes abstraites ne peuvent pas être utilisées comme entités JPA.");
        }

        // Vérifier que la classe possède un constructeur sans argument
        boolean hasNoArgConstructor = false;
        for (PsiMethod constructor : psiClass.getConstructors()) {
            if (constructor.getParameterList().getParametersCount() == 0) {
                hasNoArgConstructor = true;
                break;
            }
        }

        if (!hasNoArgConstructor && psiClass.getConstructors().length > 0) {
            errors.add("L'entité doit avoir un constructeur sans argument.");
        }

        // Vérifier la présence d'un identifiant (champ avec @Id)
        boolean hasIdField = false;
        for (PsiField field : psiClass.getAllFields()) {
            if (PsiUtils.hasAnnotation(field, "javax.persistence.Id")
                    || PsiUtils.hasAnnotation(field, "jakarta.persistence.Id")) {
                hasIdField = true;
                break;
            }
        }

        if (!hasIdField) {
            errors.add("L'entité doit avoir un champ marqué avec l'annotation @Id.");
        }

        // Vérifications de bonnes pratiques (warnings)

        // Vérifier si l'entité implémente Serializable
        boolean isSerializable = false;
        for (PsiClassType interfaceType : psiClass.getImplementsListTypes()) {
            if ("java.io.Serializable".equals(interfaceType.getCanonicalText())) {
                isSerializable = true;
                break;
            }
        }

        if (!isSerializable) {
            warnings.add("Il est recommandé que les entités JPA implémentent java.io.Serializable.");
        }

        // Vérifier si les champs relationnels sont bien configurés
        for (PsiField field : psiClass.getAllFields()) {
            if (PsiUtils.hasAnnotation(field, "javax.persistence.OneToMany")
                    || PsiUtils.hasAnnotation(field, "jakarta.persistence.OneToMany")) {
                if (!PsiUtils.isCollectionType(field.getType())) {
                    errors.add("Le champ '" + field.getName() + "' est annoté avec @OneToMany mais n'est pas une collection.");
                }
            }

            if (PsiUtils.hasAnnotation(field, "javax.persistence.ManyToMany")
                    || PsiUtils.hasAnnotation(field, "jakarta.persistence.ManyToMany")) {
                if (!PsiUtils.isCollectionType(field.getType())) {
                    errors.add("Le champ '" + field.getName() + "' est annoté avec @ManyToMany mais n'est pas une collection.");
                }
            }
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }

    @Override
    public ValidationResult validateEntityModel(EntityModel entityModel) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (entityModel == null) {
            errors.add("Le modèle d'entité est nul.");
            return new ValidationResult(false, errors, warnings);
        }

        // Vérifier que le modèle a un identifiant
        if (entityModel.getIdField() == null) {
            errors.add("L'entité doit avoir un champ d'identifiant marqué avec @Id.");
        }

        // Vérifier que les champs de type collection sont correctement configurés
        for (EntityModel.EntityField field : entityModel.getFields()) {
            if (field.isCollection() && field.getGenericType() == null) {
                warnings.add("Le champ '" + field.getName() + "' est une collection sans type générique spécifié.");
            }

            // Vérifier les relations
            if ("OneToMany".equals(field.getRelationshipType()) || "ManyToMany".equals(field.getRelationshipType())) {
                if (!field.isCollection()) {
                    errors.add("Le champ '" + field.getName() + "' a une relation " + field.getRelationshipType() +
                            " mais n'est pas une collection.");
                }
            }
        }

        return new ValidationResult(errors.isEmpty(), errors, warnings);
    }
}
