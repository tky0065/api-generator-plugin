package com.github.tky0065.apigenerator.action;

import com.github.tky0065.apigenerator.config.ApiGeneratorConfig;
import com.github.tky0065.apigenerator.model.EntityModel;
import com.github.tky0065.apigenerator.service.CodeGenerator;
import com.github.tky0065.apigenerator.service.DependencyValidationService;
import com.github.tky0065.apigenerator.service.DependencyValidationService.DependencyValidationResult;
import com.github.tky0065.apigenerator.service.EntityAnalyzerService;
import com.github.tky0065.apigenerator.service.EntityValidationService;
import com.github.tky0065.apigenerator.service.EntityValidationService.ValidationResult;
import com.github.tky0065.apigenerator.service.ErrorMessageService;
import com.github.tky0065.apigenerator.service.ErrorMessageService.Message;
import com.github.tky0065.apigenerator.service.ExistingFileService;
import com.github.tky0065.apigenerator.service.ExistingFileService.FileAction;
import com.github.tky0065.apigenerator.service.LoggingService;
import com.github.tky0065.apigenerator.service.impl.*;
import com.github.tky0065.apigenerator.ui.ApiGeneratorDialog;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.openapi.vfs.VirtualFile;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Action permettant de générer du code API pour une classe JPA Entity.
 * Cette action est disponible via le menu contextuel (clic droit) sur une classe Java.
 */
public class GenerateApiAction extends AnAction {

    private final EntityAnalyzerService entityAnalyzerService = new EntityAnalyzerServiceImpl();
    private final EntityValidationService entityValidationService = new EntityValidationServiceImpl();
    private final DependencyValidationService dependencyValidationService = new DependencyValidationServiceImpl();
    private final ErrorMessageService errorMessageService = new ErrorMessageServiceImpl();
    private final LoggingService loggingService = new LoggingServiceImpl();
    private final ExistingFileService existingFileService;

    public GenerateApiAction() {
        // Initialiser les services
        this.existingFileService = new ExistingFileServiceImpl(loggingService);
    }


    @Override
    public void update(@NotNull AnActionEvent e) {
        // Active ou désactive l'action en fonction du contexte
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        // L'action est disponible uniquement si nous avons un projet, un éditeur et un fichier Java
        boolean enabled = project != null && editor != null && psiFile != null
                && isPossiblyEntity(psiFile, editor.getCaretModel().getOffset());

        e.getPresentation().setEnabledAndVisible(enabled);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        Editor editor = e.getData(CommonDataKeys.EDITOR);
        PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);

        if (project == null || editor == null || psiFile == null) {
            return;
        }

        // Récupérer la classe sous le curseur
        PsiClass psiClass = getPsiClassAtCursor(psiFile, editor.getCaretModel().getOffset());
        if (psiClass == null) {
            loggingService.warn(project, "No class found at cursor position");
            return;
        }

        String className = psiClass.getQualifiedName();
        loggingService.logAction(project, "GenerateApi", "Starting API generation for class " + className);

        // Valider l'entité avant de continuer
        ValidationResult validationResult = entityValidationService.validateEntity(psiClass);

        // Journaliser le résultat de la validation
        loggingService.logEntityValidation(project, className, validationResult.isValid(),
                validationResult.isValid() ?
                        "Entity is valid" :
                        "Validation errors: " + String.join(", ", validationResult.getErrors()));

        // S'il y a des erreurs, afficher un message amélioré et arrêter
        if (!validationResult.isValid()) {
            Message errorMessage = errorMessageService.getInvalidEntityMessage(validationResult.getErrors());
            Messages.showErrorDialog(project, errorMessage.toFormattedString(), errorMessage.getTitle());
            loggingService.error(project, "Entity validation failed: " + String.join(", ", validationResult.getErrors()));
            return;
        }

        // S'il y a des avertissements, les afficher mais permettre de continuer
        if (validationResult.hasWarnings()) {
            loggingService.warn(project, "Entity has warnings: " + String.join(", ", validationResult.getWarnings()));

            Message warningMessage = errorMessageService.getEntityWarningMessage(validationResult.getWarnings());
            if (Messages.showYesNoDialog(project, warningMessage.toFormattedString(), warningMessage.getTitle(),
                    "Continuer", "Annuler", Messages.getWarningIcon()) != Messages.YES) {
                // L'utilisateur a choisi de ne pas continuer
                loggingService.info(project, "User cancelled generation due to warnings");
                return;
            }
        }

        // Analyser l'entité
        EntityModel entityModel = entityAnalyzerService.analyzeEntity(psiClass);

        // Valider le modèle d'entité
        ValidationResult modelValidationResult = entityValidationService.validateEntityModel(entityModel);
        if (!modelValidationResult.isValid()) {
            loggingService.error(project, "Entity model validation failed: " +
                    String.join(", ", modelValidationResult.getErrors()));

            Message errorMessage = errorMessageService.getInvalidEntityMessage(modelValidationResult.getErrors());
            Messages.showErrorDialog(project, errorMessage.toFormattedString(), errorMessage.getTitle());
            return;
        }

        // Créer une configuration par défaut
        ApiGeneratorConfig config = new ApiGeneratorConfig();
        config.setBasePackage(psiClass.getQualifiedName().substring(0, psiClass.getQualifiedName().lastIndexOf(".")));

        // Ouvrir le dialogue de configuration
        ApiGeneratorDialog dialog = new ApiGeneratorDialog(project, config, entityModel);
        if (!dialog.showAndGet()) {
            // L'utilisateur a annulé
            loggingService.info(project, "User cancelled generation from configuration dialog");
            return;
        }

        // Journaliser les choix de configuration
        loggingService.info(project, "Configuration: generateDto=" + config.isGenerateDto() +
                ", generateMapper=" + config.isGenerateMapper() +
                ", generateRepository=" + config.isGenerateRepository() +
                ", generateService=" + config.isGenerateService() +
                ", generateController=" + config.isGenerateController());

        // Valider les dépendances requises en fonction de la configuration
        DependencyValidationResult dependencyResult = dependencyValidationService.validateDependencies(project, config);
        if (dependencyResult.hasMissingDependencies()) {
            loggingService.warn(project, "Missing dependencies: " +
                    String.join(", ", dependencyResult.getMissingDependencies()));

            if (!showDependencyWarningDialog(project, dependencyResult)) {
                // L'utilisateur a choisi de ne pas continuer
                loggingService.info(project, "User cancelled generation due to missing dependencies");
                return;
            }
        }

        // L'utilisateur a validé, générer le code selon la configuration
        generateCode(project, entityModel, config);
    }

    /**
     * Affiche un dialogue d'avertissement pour les dépendances manquantes.
     * Propose à l'utilisateur de copier les dépendances manquantes dans le presse-papier.
     *
     * @return true si l'utilisateur veut continuer malgré les dépendances manquantes
     */
    private boolean showDependencyWarningDialog(Project project, DependencyValidationResult dependencyResult) {
        // Préparer les snippets Maven et Gradle pour les dépendances manquantes
        String mavenSnippet = dependencyValidationService.generateMavenDependencySuggestions(
                dependencyResult.getMissingDependencies());
        String gradleSnippet = dependencyValidationService.generateGradleDependencySuggestions(
                dependencyResult.getMissingDependencies());

        // Créer un message amélioré pour les dépendances manquantes
        Message dependencyMessage = errorMessageService.getMissingDependenciesMessage(
                dependencyResult.getMissingDependencies(), mavenSnippet, gradleSnippet);

        // Afficher les options à l'utilisateur
        String[] options = {
                "Copier les dépendances Maven",
                "Copier les dépendances Gradle",
                "Continuer sans les dépendances",
                "Annuler"
        };

        int result = Messages.showDialog(
                project,
                dependencyMessage.getDescription(),
                dependencyMessage.getTitle(),
                options,
                0, // Default button index (Copier les dépendances Maven)
                Messages.getWarningIcon()
        );

        switch (result) {
            case 0: // Copier dépendances Maven
                copyToClipboard(mavenSnippet);
                Messages.showInfoMessage(project,
                        "Les dépendances Maven ont été copiées dans le presse-papier.\n"
                                + "Ajoutez-les à votre fichier pom.xml et rechargez votre projet.",
                        "Dépendances copiées");
                return true;
            case 1: // Copier dépendances Gradle
                copyToClipboard(gradleSnippet);
                Messages.showInfoMessage(project,
                        "Les dépendances Gradle ont été copiées dans le presse-papier.\n"
                                + "Ajoutez-les à votre fichier build.gradle et rechargez votre projet.",
                        "Dépendances copiées");
                return true;
            case 2: // Continuer sans les dépendances
                return true;
            default: // Annuler
                return false;
        }
    }

    /**
     * Copie un texte dans le presse-papier.
     */
    private void copyToClipboard(String text) {
        StringSelection selection = new StringSelection(text);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

    /**
     * Génère le code en fonction de la configuration de l'utilisateur.
     */
    private void generateCode(Project project, EntityModel entityModel, ApiGeneratorConfig config) {
        // Liste pour stocker les résultats de génération
        List<GeneratedFile> generatedFiles = new ArrayList<>();
        List<String> generatedTypes = new ArrayList<>();

        try {
            loggingService.info(project, "Starting code generation for entity " + entityModel.getClassName());

            // Générer les différentes couches selon la configuration
            if (config.isGenerateDto()) {
                loggingService.debug(project, "Generating DTO");
                CodeGenerator dtoGenerator = new DtoGenerator();
                String dtoCode = dtoGenerator.generateCode(project, entityModel, config);
                generatedFiles.add(new GeneratedFile(
                        dtoGenerator.getGeneratedPackageName(entityModel, config),
                        dtoGenerator.getGeneratedClassName(entityModel, config),
                        dtoCode
                ));
                generatedTypes.add("DTO");
            }

            if (config.isGenerateMapper() && config.isGenerateDto()) {
                loggingService.debug(project, "Generating Mapper");
                CodeGenerator mapperGenerator = new MapperGenerator();
                String mapperCode = mapperGenerator.generateCode(project, entityModel, config);
                generatedFiles.add(new GeneratedFile(
                        mapperGenerator.getGeneratedPackageName(entityModel, config),
                        mapperGenerator.getGeneratedClassName(entityModel, config),
                        mapperCode
                ));
                generatedTypes.add("Mapper");
            }

            if (config.isGenerateRepository()) {
                loggingService.debug(project, "Generating Repository");
                CodeGenerator repositoryGenerator = new RepositoryGenerator();
                String repositoryCode = repositoryGenerator.generateCode(project, entityModel, config);
                generatedFiles.add(new GeneratedFile(
                        repositoryGenerator.getGeneratedPackageName(entityModel, config),
                        repositoryGenerator.getGeneratedClassName(entityModel, config),
                        repositoryCode
                ));
                generatedTypes.add("Repository");
            }

            if (config.isGenerateService()) {
                loggingService.debug(project, "Generating Service");
                CodeGenerator serviceGenerator = new ServiceGenerator();
                String serviceCode = serviceGenerator.generateCode(project, entityModel, config);
                generatedFiles.add(new GeneratedFile(
                        serviceGenerator.getGeneratedPackageName(entityModel, config),
                        serviceGenerator.getGeneratedClassName(entityModel, config),
                        serviceCode
                ));
                generatedTypes.add("Service");
            }

            if (config.isGenerateController()) {
                loggingService.debug(project, "Generating Controller");
                CodeGenerator controllerGenerator = new ControllerGenerator();
                String controllerCode = controllerGenerator.generateCode(project, entityModel, config);
                generatedFiles.add(new GeneratedFile(
                        controllerGenerator.getGeneratedPackageName(entityModel, config),
                        controllerGenerator.getGeneratedClassName(entityModel, config),
                        controllerCode
                ));
                generatedTypes.add("Controller");
            }

            // Créer les fichiers dans le projet
            createFiles(project, generatedFiles);

            // Journaliser le résultat de la génération
            loggingService.logGeneration(project, entityModel.getClassName(),
                    generatedTypes.toArray(new String[0]), generatedFiles.size());

            // Afficher un message de succès amélioré avec le nombre de fichiers créés
            Message successMessage = errorMessageService.getGenerationSuccessMessage(generatedFiles.size());
            Messages.showInfoMessage(project, successMessage.toFormattedString(), successMessage.getTitle());

        } catch (Exception e) {
            // Journaliser l'erreur
            loggingService.error(project, "Error during code generation: " + e.getMessage(), e);

            // En cas d'erreur, afficher un message plus détaillé
            Message errorMessage = errorMessageService.getFileWriteErrorMessage(
                    e.getMessage(),
                    e.getMessage().contains(".java") ? e.getMessage().substring(e.getMessage().lastIndexOf('/') + 1) : "inconnu");
            Messages.showErrorDialog(project, errorMessage.toFormattedString(), errorMessage.getTitle());
        }
    }

    /**
     * Crée les fichiers Java générés dans le projet.
     */
    private void createFiles(Project project, List<GeneratedFile> files) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            for (GeneratedFile file : files) {
                try {
                    loggingService.debug(project, "Processing file " + file.className + ".java");

                    // Vérifier si le fichier existe déjà et déterminer l'action à prendre
                    FileAction action = existingFileService.checkFileExists(
                            project, file.packageName, file.className, FileAction.ASK);

                    // Si l'action est ASK, demander à l'utilisateur
                    if (action == FileAction.ASK) {
                        action = showFileExistsDialog(project, file.packageName, file.className);
                    }

                    // Agir selon la décision
                    switch (action) {
                        case SKIP:
                            loggingService.info(project, "Skipping file " + file.className + ".java (user choice)");
                            continue; // Passer au fichier suivant

                        case RENAME:
                            String newName = existingFileService.generateAlternativeName(file.className);
                            loggingService.info(project, "Renaming file to " + newName + ".java");
                            file = new GeneratedFile(file.packageName, newName, file.content);
                            break;

                        case REPLACE:
                            // Continuer avec le remplacement (comportement par défaut)
                            loggingService.info(project, "Replacing existing file " + file.className + ".java");
                            break;
                    }

                    // Ajouter une signature au contenu généré
                    String contentWithSignature = ExistingFileServiceImpl.addGeneratedSignature(file.content);

                    // Créer ou remplacer le fichier
                    PsiFileFactory psiFileFactory = PsiFileFactory.getInstance(project);
                    PsiJavaFile psiFile = (PsiJavaFile) psiFileFactory.createFileFromText(
                            file.className + ".java",
                            JavaFileType.INSTANCE,
                            contentWithSignature
                    );

                    // Trouver ou créer le répertoire du package
                    String packageName = file.packageName;
                    PsiDirectory directory = createPackageDirectories(project, packageName);

                    // Ajouter le fichier au répertoire
                    try {
                        // Vérifier si le fichier existe déjà
                        PsiFile existingFile = directory.findFile(file.className + ".java");
                        if (existingFile != null) {
                            // Remplacer le fichier existant
                            loggingService.info(project, "Deleting existing file before replacement: " + file.className + ".java");
                            existingFile.delete();
                        }

                        directory.add(psiFile);
                        loggingService.info(project, "File created successfully: " + file.className + ".java");
                    } catch (Exception e) {
                        loggingService.error(project, "Error creating file " + file.className + ".java: " + e.getMessage(), e);

                        // Reformater l'exception en un message plus clair
                        Message errorMessage = errorMessageService.getFileWriteErrorMessage(e.getMessage(), file.className + ".java");
                        throw new RuntimeException(errorMessage.getTitle() + ": " + e.getMessage());
                    }
                } catch (Exception e) {
                    loggingService.error(project, "Unexpected error: " + e.getMessage(), e);
                    throw e;
                }
            }

            // Réinitialiser les choix utilisateur après la génération
            existingFileService.resetUserChoices();
        });
    }

    /**
     * Affiche un dialogue demandant à l'utilisateur que faire avec un fichier existant.
     */
    private FileAction showFileExistsDialog(Project project, String packageName, String className) {
        String message = "Le fichier " + className + ".java existe déjà dans le package " + packageName + ".\n"
                + "Que souhaitez-vous faire ?";

        String[] options = {
            "Remplacer",
            "Ignorer",
            "Renommer le nouveau fichier",
            "Remplacer tous",
            "Ignorer tous",
            "Renommer tous"
        };

        int choice = Messages.showDialog(project, message, "Fichier existant",
                options, 0, Messages.getQuestionIcon());

        FileAction action;
        boolean applyToAll = false;

        switch (choice) {
            case 0: // Remplacer
                action = FileAction.REPLACE;
                break;
            case 1: // Ignorer
                action = FileAction.SKIP;
                break;
            case 2: // Renommer
                action = FileAction.RENAME;
                break;
            case 3: // Remplacer tous
                action = FileAction.REPLACE;
                applyToAll = true;
                break;
            case 4: // Ignorer tous
                action = FileAction.SKIP;
                applyToAll = true;
                break;
            case 5: // Renommer tous
                action = FileAction.RENAME;
                applyToAll = true;
                break;
            default:
                // Par défaut, ignorer ce fichier
                action = FileAction.SKIP;
                break;
        }

        // Enregistrer le choix de l'utilisateur pour les futures générations
        existingFileService.saveUserChoice(packageName, className, action, applyToAll);

        return action;
    }

    /**
     * Crée récursivement les répertoires nécessaires pour un package.
     */
    private PsiDirectory createPackageDirectories(Project project, String packageName) {
        // Utilisation de ProjectUtil.guessProjectDir au lieu de project.getBaseDir()
        VirtualFile projectDir = com.intellij.openapi.project.ProjectUtil.guessProjectDir(project);
        if (projectDir == null) {
            throw new IllegalStateException("Impossible de trouver le répertoire de base du projet");
        }

        PsiDirectory baseDir = PsiManager.getInstance(project).findDirectory(projectDir);
        if (baseDir == null) {
            throw new IllegalStateException("Impossible de trouver le répertoire de base du projet");
        }

        // Créer le répertoire "src/main/java" s'il n'existe pas
        baseDir = findOrCreateDirectory(baseDir, "src");
        baseDir = findOrCreateDirectory(baseDir, "main");
        baseDir = findOrCreateDirectory(baseDir, "java");

        // Créer les répertoires du package
        String[] packageParts = packageName.split("\\.");
        for (String part : packageParts) {
            baseDir = findOrCreateDirectory(baseDir, part);
        }

        return baseDir;
    }

    /**
     * Trouve ou crée un sous-répertoire.
     */
    private PsiDirectory findOrCreateDirectory(PsiDirectory parent, String name) {
        PsiDirectory directory = parent.findSubdirectory(name);
        if (directory == null) {
            directory = parent.createSubdirectory(name);
        }
        return directory;
    }

    /**
     * Vérifie si l'élément à la position donnée est potentiellement une entité JPA.
     */
    private boolean isPossiblyEntity(PsiFile psiFile, int offset) {
        // Récupérer la classe sous le curseur
        PsiClass psiClass = getPsiClassAtCursor(psiFile, offset);
        if (psiClass == null) {
            return false;
        }

        // Vérification rapide : le nom contient "Entity" ou possède une annotation
        return psiClass.getAnnotations().length > 0 || psiClass.getName().contains("Entity");
    }

    /**
     * Récupère la classe PsiClass à la position du curseur.
     */
    private PsiClass getPsiClassAtCursor(PsiFile psiFile, int offset) {
        PsiElement element = psiFile.findElementAt(offset);
        if (element == null) {
            return null;
        }

        return PsiTreeUtil.getParentOfType(element, PsiClass.class);
    }

    /**
     * Classe interne pour stocker les informations sur les fichiers générés.
     */
    private static class GeneratedFile {
        final String packageName;
        final String className;
        final String content;

        GeneratedFile(String packageName, String className, String content) {
            this.packageName = packageName;
            this.className = className;
            this.content = content;
        }
    }
}
