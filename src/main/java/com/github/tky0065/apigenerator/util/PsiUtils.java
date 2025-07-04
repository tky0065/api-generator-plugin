package com.github.tky0065.apigenerator.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Classe utilitaire pour travailler avec l'API PSI (Program Structure Interface).
 */
public class PsiUtils {

    private PsiUtils() {
        // Classe utilitaire, ne doit pas être instanciée
    }

    /**
     * Trouve une classe par son nom qualifié complet.
     *
     * @param project le projet IntelliJ
     * @param qualifiedName le nom qualifié complet de la classe
     * @return la classe PSI ou null si elle n'est pas trouvée
     */
    @Nullable
    public static PsiClass findClass(@NotNull Project project, @NotNull String qualifiedName) {
        JavaPsiFacade javaPsiFacade = JavaPsiFacade.getInstance(project);
        return javaPsiFacade.findClass(qualifiedName, GlobalSearchScope.allScope(project));
    }

    /**
     * Vérifie si une classe possède une annotation spécifique.
     *
     * @param psiClass la classe à vérifier
     * @param annotationQualifiedName le nom qualifié complet de l'annotation
     * @return true si la classe possède l'annotation, false sinon
     */
    public static boolean hasAnnotation(@Nullable PsiClass psiClass, @NotNull String annotationQualifiedName) {
        if (psiClass == null) {
            return false;
        }

        for (PsiAnnotation annotation : psiClass.getAnnotations()) {
            String qualifiedName = annotation.getQualifiedName();
            if (annotationQualifiedName.equals(qualifiedName)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Vérifie si un élément (champ, méthode, etc.) possède une annotation spécifique.
     *
     * @param element l'élément à vérifier
     * @param annotationQualifiedName le nom qualifié complet de l'annotation
     * @return true si l'élément possède l'annotation, false sinon
     */
    public static boolean hasAnnotation(@Nullable PsiModifierListOwner element, @NotNull String annotationQualifiedName) {
        if (element == null || element.getModifierList() == null) {
            return false;
        }

        PsiAnnotation annotation = element.getModifierList().findAnnotation(annotationQualifiedName);
        return annotation != null;
    }

    /**
     * Obtient la valeur d'un attribut d'annotation sous forme de chaîne.
     *
     * @param annotation l'annotation
     * @param attributeName le nom de l'attribut
     * @return la valeur de l'attribut sous forme de chaîne, ou null si l'attribut n'existe pas
     */
    @Nullable
    public static String getAnnotationAttributeValue(@NotNull PsiAnnotation annotation, @NotNull String attributeName) {
        PsiAnnotationMemberValue value = annotation.findAttributeValue(attributeName);
        if (value instanceof PsiLiteralExpression) {
            Object literalValue = ((PsiLiteralExpression) value).getValue();
            return literalValue != null ? literalValue.toString() : null;
        }
        return value != null ? value.getText() : null;
    }

    /**
     * Crée un élément PsiElement à partir d'une chaîne de code.
     *
     * @param project le projet IntelliJ
     * @param text le code source
     * @param context le contexte pour la création de l'élément
     * @return l'élément PSI créé
     */
    @NotNull
    public static PsiElement createElementFromText(@NotNull Project project, @NotNull String text, @NotNull PsiElement context) {
        PsiElementFactory factory = JavaPsiFacade.getElementFactory(project);
        return factory.createStatementFromText(text, context);
    }

    /**
     * Vérifie si un type est un type de collection Java (List, Set, Collection, Map).
     *
     * @param type le type PSI à vérifier
     * @return true si le type est une collection, false sinon
     */
    public static boolean isCollectionType(PsiType type) {
        if (!(type instanceof PsiClassType)) {
            return false;
        }

        PsiClass psiClass = ((PsiClassType) type).resolve();
        if (psiClass == null) {
            return false;
        }

        String qualifiedName = psiClass.getQualifiedName();
        return qualifiedName != null && (
                qualifiedName.startsWith("java.util.List") ||
                qualifiedName.startsWith("java.util.Set") ||
                qualifiedName.startsWith("java.util.Collection") ||
                qualifiedName.startsWith("java.util.Map")
        );
    }
}
