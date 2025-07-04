package com.github.tky0065.apigenerator.service;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;

import java.util.Map;

/**
 * Service pour la détection et la gestion des fichiers existants.
 * Permet d'éviter l'écrasement accidentel de fichiers modifiés manuellement.
 */
public interface ExistingFileService {

    /**
     * Options de gestion des fichiers existants.
     */
    enum FileAction {
        REPLACE,    // Remplacer le fichier existant
        SKIP,       // Ignorer (ne pas générer ce fichier)
        RENAME,     // Renommer le nouveau fichier (ajouter un suffixe)
        ASK         // Demander à l'utilisateur quoi faire
    }

    /**
     * Vérifie si un fichier existe déjà et détermine l'action à prendre.
     *
     * @param project Le projet
     * @param packageName Le nom du package
     * @param className Le nom de la classe
     * @param defaultAction L'action par défaut si aucune règle ne correspond
     * @return L'action à effectuer
     */
    FileAction checkFileExists(Project project, String packageName, String className, FileAction defaultAction);

    /**
     * Enregistre un choix utilisateur pour les futures générations.
     *
     * @param packageName Le nom du package
     * @param className Le nom de la classe
     * @param action L'action choisie par l'utilisateur
     * @param applyToAll Si ce choix doit s'appliquer à tous les fichiers de cette génération
     */
    void saveUserChoice(String packageName, String className, FileAction action, boolean applyToAll);

    /**
     * Génère un nom alternatif pour un fichier en conflit.
     *
     * @param originalName Le nom original de la classe
     * @return Un nom alternatif (avec suffixe)
     */
    String generateAlternativeName(String originalName);

    /**
     * Vérifie si un fichier a été modifié manuellement après sa génération.
     *
     * @param project Le projet
     * @param file Le fichier à vérifier
     * @return true si le fichier a probablement été modifié manuellement
     */
    boolean isFileManuallyModified(Project project, PsiFile file);

    /**
     * Réinitialise tous les choix utilisateur enregistrés.
     */
    void resetUserChoices();

    /**
     * Obtient la liste des choix utilisateur actuellement enregistrés.
     *
     * @return Une map associant les identifiants de fichier aux actions choisies
     */
    Map<String, FileAction> getUserChoices();
}
