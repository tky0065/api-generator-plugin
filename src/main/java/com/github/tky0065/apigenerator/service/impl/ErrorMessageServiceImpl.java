package com.github.tky0065.apigenerator.service.impl;

import com.github.tky0065.apigenerator.service.ErrorMessageService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Implémentation du service de gestion des messages d'erreur et d'information.
 * Fournit des messages clairs et des suggestions pour résoudre les problèmes.
 */
public class ErrorMessageServiceImpl implements ErrorMessageService {

    @Override
    public Message getInvalidEntityMessage(List<String> errors) {
        String title = "Entité JPA invalide";
        String code = "ENTITY_INVALID";

        StringBuilder description = new StringBuilder("L'entité sélectionnée contient des erreurs qui empêchent la génération du code. ");
        description.append("Veuillez corriger les problèmes suivants :\n\n");

        for (String error : errors) {
            description.append("• ").append(error).append("\n");
        }

        List<String> suggestions = Arrays.asList(
                "Assurez-vous que la classe est annotée avec @Entity de JPA",
                "Vérifiez que la classe a un champ annoté avec @Id",
                "Si la classe est abstraite, utilisez plutôt une classe concrète",
                "Ajoutez un constructeur sans argument si vous n'en avez pas",
                "Pour les relations OneToMany ou ManyToMany, utilisez des collections (List, Set, etc.)"
        );

        return new Message(MessageType.ERROR, code, title, description.toString(), suggestions);
    }

    @Override
    public Message getEntityWarningMessage(List<String> warnings) {
        String title = "Avertissements sur l'entité JPA";
        String code = "ENTITY_WARNINGS";

        StringBuilder description = new StringBuilder("L'entité sélectionnée présente des avertissements qui n'empêchent pas la génération mais pourraient causer des problèmes :\n\n");

        for (String warning : warnings) {
            description.append("• ").append(warning).append("\n");
        }

        List<String> suggestions = Arrays.asList(
                "Envisagez d'implémenter l'interface Serializable pour une meilleure compatibilité",
                "Spécifiez des types génériques explicites pour toutes les collections",
                "Évitez les références circulaires dans vos relations",
                "Utilisez FetchType.LAZY pour les collections volumineuses"
        );

        return new Message(MessageType.WARNING, code, title, description.toString(), suggestions);
    }

    @Override
    public Message getMissingDependenciesMessage(List<String> missingDependencies, String mavenSnippet, String gradleSnippet) {
        String title = "Dépendances manquantes dans le projet";
        String code = "MISSING_DEPENDENCIES";

        StringBuilder description = new StringBuilder("Le code généré nécessite des bibliothèques qui ne sont pas présentes dans votre projet. ");
        description.append("Les dépendances suivantes sont manquantes :\n\n");

        for (String dependency : missingDependencies) {
            description.append("• ").append(dependency).append("\n");
        }

        description.append("\nLe code généré ne compilera pas sans ces dépendances.");

        List<String> suggestions = Arrays.asList(
                "Pour Maven, ajoutez ces dépendances à votre pom.xml :\n\n" + mavenSnippet,
                "Pour Gradle, ajoutez ces dépendances à votre build.gradle :\n\n" + gradleSnippet,
                "Après avoir ajouté les dépendances, rechargez votre projet"
        );

        return new Message(MessageType.WARNING, code, title, description.toString(), suggestions);
    }

    @Override
    public Message getFileWriteErrorMessage(String errorDetails, String fileName) {
        String title = "Erreur lors de l'écriture du fichier";
        String code = "FILE_WRITE_ERROR";

        String description = "Une erreur est survenue lors de l'écriture du fichier " + fileName + ".\n\n" +
                "Détails de l'erreur : " + errorDetails;

        List<String> suggestions = Arrays.asList(
                "Vérifiez que vous avez les permissions d'écriture dans le répertoire cible",
                "Fermez le fichier s'il est ouvert dans un autre programme",
                "Si le problème persiste, essayez de redémarrer l'IDE"
        );

        return new Message(MessageType.ERROR, code, title, description, suggestions);
    }

    @Override
    public Message getGenerationSuccessMessage(int generatedCount) {
        String title = "Génération réussie";
        String code = "GENERATION_SUCCESS";

        String description = generatedCount + " fichier(s) ont été générés avec succès.";

        List<String> suggestions = Collections.singletonList(
                "N'oubliez pas de vérifier la présence de toutes les dépendances nécessaires dans votre projet"
        );

        return new Message(MessageType.INFO, code, title, description, suggestions);
    }
}
