package com.github.tky0065.apigenerator.ui;

import com.github.tky0065.apigenerator.config.ApiGeneratorConfig;
import com.github.tky0065.apigenerator.model.EntityModel;
import com.github.tky0065.apigenerator.service.CodeGenerator;
import com.github.tky0065.apigenerator.service.impl.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.EditorSettings;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTabbedPane;

import javax.swing.*;
import java.awt.*;

/**
 * Composant permettant de prévisualiser le code généré dans l'interface.
 */
public class CodePreviewComponent {
    private JPanel mainPanel;
    private JBTabbedPane tabbedPane;
    private final Project project;
    private final EntityModel entityModel;
    private final ApiGeneratorConfig config;

    // Les éditeurs pour chaque type de fichier
    private Editor dtoEditor;
    private Editor mapperEditor;
    private Editor repositoryEditor;
    private Editor serviceEditor;
    private Editor controllerEditor;

    public CodePreviewComponent(Project project, EntityModel entityModel, ApiGeneratorConfig config) {
        this.project = project;
        this.entityModel = entityModel;
        this.config = config;

        initComponent();
    }

    private void initComponent() {
        mainPanel = new JPanel(new BorderLayout());
        tabbedPane = new JBTabbedPane();
        mainPanel.add(tabbedPane, BorderLayout.CENTER);

        // Créer les onglets pour chaque type de fichier
        createDtoPreview();
        createMapperPreview();
        createRepositoryPreview();
        createServicePreview();
        createControllerPreview();

        // Dimensionner le composant
        mainPanel.setPreferredSize(new Dimension(800, 400));
    }

    /**
     * Met à jour le contenu des prévisualisations avec la configuration actuelle.
     */
    public void updatePreviews(ApiGeneratorConfig updatedConfig) {
        // Mettre à jour la prévisualisation du DTO
        if (updatedConfig.isGenerateDto()) {
            updateDtoPreview(updatedConfig);
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("DTO"), true);
        } else {
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("DTO"), false);
        }

        // Mettre à jour la prévisualisation du Mapper
        if (updatedConfig.isGenerateMapper() && updatedConfig.isGenerateDto()) {
            updateMapperPreview(updatedConfig);
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Mapper"), true);
        } else {
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Mapper"), false);
        }

        // Mettre à jour la prévisualisation du Repository
        if (updatedConfig.isGenerateRepository()) {
            updateRepositoryPreview(updatedConfig);
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Repository"), true);
        } else {
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Repository"), false);
        }

        // Mettre à jour la prévisualisation du Service
        if (updatedConfig.isGenerateService()) {
            updateServicePreview(updatedConfig);
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Service"), true);
        } else {
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Service"), false);
        }

        // Mettre à jour la prévisualisation du Controller
        if (updatedConfig.isGenerateController()) {
            updateControllerPreview(updatedConfig);
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Controller"), true);
        } else {
            tabbedPane.setEnabledAt(tabbedPane.indexOfTab("Controller"), false);
        }
    }

    /**
     * Crée l'onglet pour la prévisualisation du DTO.
     */
    private void createDtoPreview() {
        dtoEditor = createJavaEditor();
        tabbedPane.addTab("DTO", createEditorPanel(dtoEditor));
        updateDtoPreview(config);
    }

    /**
     * Crée l'onglet pour la prévisualisation du Mapper.
     */
    private void createMapperPreview() {
        mapperEditor = createJavaEditor();
        tabbedPane.addTab("Mapper", createEditorPanel(mapperEditor));
        updateMapperPreview(config);
    }

    /**
     * Crée l'onglet pour la prévisualisation du Repository.
     */
    private void createRepositoryPreview() {
        repositoryEditor = createJavaEditor();
        tabbedPane.addTab("Repository", createEditorPanel(repositoryEditor));
        updateRepositoryPreview(config);
    }

    /**
     * Crée l'onglet pour la prévisualisation du Service.
     */
    private void createServicePreview() {
        serviceEditor = createJavaEditor();
        tabbedPane.addTab("Service", createEditorPanel(serviceEditor));
        updateServicePreview(config);
    }

    /**
     * Crée l'onglet pour la prévisualisation du Controller.
     */
    private void createControllerPreview() {
        controllerEditor = createJavaEditor();
        tabbedPane.addTab("Controller", createEditorPanel(controllerEditor));
        updateControllerPreview(config);
    }

    /**
     * Met à jour la prévisualisation du DTO.
     */
    private void updateDtoPreview(ApiGeneratorConfig updatedConfig) {
        CodeGenerator dtoGenerator = new DtoGenerator();
        String code = dtoGenerator.generateCode(project, entityModel, updatedConfig);
        EditorFactory.getInstance().releaseEditor(dtoEditor);
        dtoEditor = updateEditor(dtoEditor, code);
        tabbedPane.setComponentAt(tabbedPane.indexOfTab("DTO"), createEditorPanel(dtoEditor));
    }

    /**
     * Met à jour la prévisualisation du Mapper.
     */
    private void updateMapperPreview(ApiGeneratorConfig updatedConfig) {
        CodeGenerator mapperGenerator = new MapperGenerator();
        String code = mapperGenerator.generateCode(project, entityModel, updatedConfig);
        EditorFactory.getInstance().releaseEditor(mapperEditor);
        mapperEditor = updateEditor(mapperEditor, code);
        tabbedPane.setComponentAt(tabbedPane.indexOfTab("Mapper"), createEditorPanel(mapperEditor));
    }

    /**
     * Met à jour la prévisualisation du Repository.
     */
    private void updateRepositoryPreview(ApiGeneratorConfig updatedConfig) {
        CodeGenerator repositoryGenerator = new RepositoryGenerator();
        String code = repositoryGenerator.generateCode(project, entityModel, updatedConfig);
        EditorFactory.getInstance().releaseEditor(repositoryEditor);
        repositoryEditor = updateEditor(repositoryEditor, code);
        tabbedPane.setComponentAt(tabbedPane.indexOfTab("Repository"), createEditorPanel(repositoryEditor));
    }

    /**
     * Met à jour la prévisualisation du Service.
     */
    private void updateServicePreview(ApiGeneratorConfig updatedConfig) {
        CodeGenerator serviceGenerator = new ServiceGenerator();
        String code = serviceGenerator.generateCode(project, entityModel, updatedConfig);
        EditorFactory.getInstance().releaseEditor(serviceEditor);
        serviceEditor = updateEditor(serviceEditor, code);
        tabbedPane.setComponentAt(tabbedPane.indexOfTab("Service"), createEditorPanel(serviceEditor));
    }

    /**
     * Met à jour la prévisualisation du Controller.
     */
    private void updateControllerPreview(ApiGeneratorConfig updatedConfig) {
        CodeGenerator controllerGenerator = new ControllerGenerator();
        String code = controllerGenerator.generateCode(project, entityModel, updatedConfig);
        EditorFactory.getInstance().releaseEditor(controllerEditor);
        controllerEditor = updateEditor(controllerEditor, code);
        tabbedPane.setComponentAt(tabbedPane.indexOfTab("Controller"), createEditorPanel(controllerEditor));
    }

    /**
     * Crée un éditeur Java configuré pour l'affichage de code.
     */
    private Editor createJavaEditor() {
        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor editor = editorFactory.createEditor(editorFactory.createDocument(""),
                project,
                FileTypeManager.getInstance().getFileTypeByExtension("java"),
                true);

        // Configuration de l'éditeur pour la prévisualisation
        EditorSettings settings = editor.getSettings();
        settings.setLineNumbersShown(true);
        settings.setFoldingOutlineShown(true);
        settings.setLineMarkerAreaShown(true);
        settings.setIndentGuidesShown(true);
        settings.setGutterIconsShown(true);
        settings.setAdditionalLinesCount(0);
        settings.setAdditionalColumnsCount(0);
        settings.setRightMarginShown(false);

        // Configurer la coloration syntaxique
        if (editor instanceof EditorEx) {
            EditorEx editorEx = (EditorEx) editor;
            FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension("java");
            editorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(
                    project, fileType));
            editorEx.setBackgroundColor(EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground());
        }

        return editor;
    }

    /**
     * Met à jour un éditeur avec un nouveau contenu.
     */
    private Editor updateEditor(Editor oldEditor, String code) {
        if (oldEditor != null) {
            EditorFactory.getInstance().releaseEditor(oldEditor);
        }

        EditorFactory editorFactory = EditorFactory.getInstance();
        Editor newEditor = editorFactory.createEditor(editorFactory.createDocument(code),
                project,
                FileTypeManager.getInstance().getFileTypeByExtension("java"),
                true);

        // Configuration de l'éditeur pour la prévisualisation
        EditorSettings settings = newEditor.getSettings();
        settings.setLineNumbersShown(true);
        settings.setFoldingOutlineShown(true);
        settings.setLineMarkerAreaShown(true);
        settings.setIndentGuidesShown(true);
        settings.setGutterIconsShown(true);
        settings.setAdditionalLinesCount(0);
        settings.setAdditionalColumnsCount(0);
        settings.setRightMarginShown(false);

        // Configurer la coloration syntaxique
        if (newEditor instanceof EditorEx) {
            EditorEx editorEx = (EditorEx) newEditor;
            FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension("java");
            editorEx.setHighlighter(EditorHighlighterFactory.getInstance().createEditorHighlighter(
                    project, fileType));
            editorEx.setBackgroundColor(EditorColorsManager.getInstance().getGlobalScheme().getDefaultBackground());
        }

        return newEditor;
    }

    /**
     * Crée un panneau contenant l'éditeur.
     */
    private JPanel createEditorPanel(Editor editor) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(editor.getComponent(), BorderLayout.CENTER);
        return panel;
    }

    /**
     * Renvoie le composant principal.
     */
    public JComponent getComponent() {
        return mainPanel;
    }

    /**
     * Libère les ressources des éditeurs à la fermeture du dialogue.
     */
    public void dispose() {
        if (dtoEditor != null) {
            EditorFactory.getInstance().releaseEditor(dtoEditor);
        }
        if (mapperEditor != null) {
            EditorFactory.getInstance().releaseEditor(mapperEditor);
        }
        if (repositoryEditor != null) {
            EditorFactory.getInstance().releaseEditor(repositoryEditor);
        }
        if (serviceEditor != null) {
            EditorFactory.getInstance().releaseEditor(serviceEditor);
        }
        if (controllerEditor != null) {
            EditorFactory.getInstance().releaseEditor(controllerEditor);
        }
    }
}
