package com.github.tky0065.apigenerator.service.impl;

import com.github.tky0065.apigenerator.service.LoggingService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.util.ExceptionUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Implémentation du service de journalisation.
 * Utilise le système de logs d'IntelliJ IDEA et garde une trace des actions importantes.
 */
public class LoggingServiceImpl implements LoggingService {

    private static final Logger LOG = Logger.getInstance(LoggingServiceImpl.class);
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public void info(Project project, String message) {
        LOG.info(formatMessage(project, message));
    }

    @Override
    public void debug(Project project, String message) {
        LOG.debug(formatMessage(project, message));
    }

    @Override
    public void warn(Project project, String message) {
        LOG.warn(formatMessage(project, message));
    }

    @Override
    public void error(Project project, String message, Throwable throwable) {
        LOG.error(formatMessage(project, message), throwable);
    }

    @Override
    public void error(Project project, String message) {
        LOG.error(formatMessage(project, message));
    }

    @Override
    public void logAction(Project project, String actionName, String details) {
        String message = String.format("Action: %s - %s", actionName, details);
        info(project, message);
    }

    @Override
    public void logGeneration(Project project, String entityName, String[] generatedTypes, int count) {
        String types = Arrays.stream(generatedTypes)
                .collect(Collectors.joining(", "));

        String message = String.format("Generated %d file(s) for entity '%s'. Types: [%s]",
                count, entityName, types);

        info(project, message);
    }

    @Override
    public void logEntityValidation(Project project, String entityName, boolean isValid, String details) {
        String status = isValid ? "valid" : "invalid";
        String message = String.format("Entity '%s' validation: %s - %s",
                entityName, status, details);

        if (isValid) {
            info(project, message);
        } else {
            warn(project, message);
        }
    }

    /**
     * Formate un message de log avec des informations contextuelles.
     */
    private String formatMessage(Project project, String message) {
        StringBuilder builder = new StringBuilder();

        // Ajouter l'horodatage
        builder.append("[").append(DATE_FORMAT.format(new Date())).append("] ");

        // Ajouter l'identifiant du projet si disponible
        if (project != null) {
            builder.append("[Project: ").append(project.getName()).append("] ");
        }

        // Ajouter le message
        builder.append(message);

        return builder.toString();
    }
}
