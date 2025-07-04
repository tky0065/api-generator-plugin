package com.github.tky0065.apigenerator.service;

import com.intellij.openapi.project.Project;

/**
 * Service de journalisation des actions et erreurs du plugin.
 * Permet de garder une trace des opérations effectuées pour faciliter le débogage.
 */
public interface LoggingService {

    /**
     * Niveaux de journalisation.
     */
    enum LogLevel {
        DEBUG,
        INFO,
        WARN,
        ERROR
    }

    /**
     * Enregistre un message au niveau INFO.
     *
     * @param project Le projet concerné (peut être null)
     * @param message Le message à journaliser
     */
    void info(Project project, String message);

    /**
     * Enregistre un message au niveau DEBUG.
     *
     * @param project Le projet concerné (peut être null)
     * @param message Le message à journaliser
     */
    void debug(Project project, String message);

    /**
     * Enregistre un message au niveau WARN.
     *
     * @param project Le projet concerné (peut être null)
     * @param message Le message à journaliser
     */
    void warn(Project project, String message);

    /**
     * Enregistre un message au niveau ERROR avec une exception associée.
     *
     * @param project Le projet concerné (peut être null)
     * @param message Le message à journaliser
     * @param throwable L'exception associée
     */
    void error(Project project, String message, Throwable throwable);

    /**
     * Enregistre un message au niveau ERROR.
     *
     * @param project Le projet concerné (peut être null)
     * @param message Le message à journaliser
     */
    void error(Project project, String message);

    /**
     * Journalise une action utilisateur.
     *
     * @param project Le projet concerné (peut être null)
     * @param actionName Le nom de l'action
     * @param details Les détails de l'action
     */
    void logAction(Project project, String actionName, String details);

    /**
     * Journalise la génération de code.
     *
     * @param project Le projet concerné
     * @param entityName Le nom de l'entité
     * @param generatedTypes Les types de fichiers générés
     * @param count Le nombre de fichiers générés
     */
    void logGeneration(Project project, String entityName, String[] generatedTypes, int count);

    /**
     * Journalise une validation d'entité.
     *
     * @param project Le projet concerné
     * @param entityName Le nom de l'entité
     * @param isValid Si l'entité est valide
     * @param details Les détails de la validation
     */
    void logEntityValidation(Project project, String entityName, boolean isValid, String details);
}
