package com.github.tky0065.apigenerator.service;

import java.util.List;

/**
 * Service de gestion des messages d'erreur et d'information.
 * Centralise tous les messages pour garantir une expérience utilisateur cohérente.
 */
public interface ErrorMessageService {

    /**
     * Types de messages.
     */
    enum MessageType {
        ERROR,
        WARNING,
        INFO
    }

    /**
     * Modèle représentant un message d'erreur ou d'information.
     */
    class Message {
        private final MessageType type;
        private final String code;
        private final String title;
        private final String description;
        private final List<String> suggestions;

        public Message(MessageType type, String code, String title, String description, List<String> suggestions) {
            this.type = type;
            this.code = code;
            this.title = title;
            this.description = description;
            this.suggestions = suggestions;
        }

        public MessageType getType() {
            return type;
        }

        public String getCode() {
            return code;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getSuggestions() {
            return suggestions;
        }

        /**
         * Crée une représentation formatée du message.
         * @return Le message formaté
         */
        public String toFormattedString() {
            StringBuilder sb = new StringBuilder();

            // Ajouter le titre et le code
            sb.append(title);
            if (code != null && !code.isEmpty()) {
                sb.append(" [").append(code).append("]");
            }
            sb.append("\n\n");

            // Ajouter la description
            sb.append(description).append("\n");

            // Ajouter les suggestions
            if (suggestions != null && !suggestions.isEmpty()) {
                sb.append("\nSuggestions:\n");
                for (String suggestion : suggestions) {
                    sb.append("• ").append(suggestion).append("\n");
                }
            }

            return sb.toString();
        }
    }

    /**
     * Obtient un message d'erreur pour une entité invalide.
     *
     * @param errors Liste des problèmes détectés
     * @return Un message d'erreur formaté
     */
    Message getInvalidEntityMessage(List<String> errors);

    /**
     * Obtient un message d'avertissement pour une entité avec des problèmes non critiques.
     *
     * @param warnings Liste des avertissements
     * @return Un message d'avertissement formaté
     */
    Message getEntityWarningMessage(List<String> warnings);

    /**
     * Obtient un message d'erreur pour des dépendances manquantes.
     *
     * @param missingDependencies Liste des dépendances manquantes
     * @param mavenSnippet Extrait de code Maven pour ajouter les dépendances
     * @param gradleSnippet Extrait de code Gradle pour ajouter les dépendances
     * @return Un message d'erreur formaté
     */
    Message getMissingDependenciesMessage(List<String> missingDependencies,
                                         String mavenSnippet,
                                         String gradleSnippet);

    /**
     * Obtient un message d'erreur lors de l'écriture des fichiers générés.
     *
     * @param errorDetails Les détails de l'erreur
     * @param fileName Le nom du fichier concerné
     * @return Un message d'erreur formaté
     */
    Message getFileWriteErrorMessage(String errorDetails, String fileName);

    /**
     * Obtient un message de succès pour la génération des fichiers.
     *
     * @param generatedCount Nombre de fichiers générés
     * @return Un message d'information formaté
     */
    Message getGenerationSuccessMessage(int generatedCount);
}
