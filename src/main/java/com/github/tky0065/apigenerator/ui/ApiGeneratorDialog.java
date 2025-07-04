package com.github.tky0065.apigenerator.ui;

import com.github.tky0065.apigenerator.config.ApiGeneratorConfig;
import com.github.tky0065.apigenerator.model.EntityModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ApiGeneratorDialog extends DialogWrapper {
    private final Project project;
    private final ApiGeneratorConfig config;
    private final EntityModel entityModel;
    private CodePreviewComponent previewComponent;

    // Champs pour les packages
    private JBTextField basePackageField;
    private JBTextField dtoPackageField;
    private JBTextField mapperPackageField;
    private JBTextField repositoryPackageField;
    private JBTextField servicePackageField;
    private JBTextField controllerPackageField;

    // Suffixes des classes
    private JBTextField dtoSuffixField;
    private JBTextField mapperSuffixField;
    private JBTextField repositorySuffixField;
    private JBTextField serviceSuffixField;
    private JBTextField controllerSuffixField;

    // Checkboxes pour les options
    private JBCheckBox generateDtoCheckBox;
    private JBCheckBox generateMapperCheckBox;
    private JBCheckBox generateRepositoryCheckBox;
    private JBCheckBox generateServiceCheckBox;
    private JBCheckBox generateControllerCheckBox;
    private JBCheckBox useLombokCheckBox;

    // Panneau pour la prévisualisation du code
    private JPanel previewPanel;

    public ApiGeneratorDialog(Project project, ApiGeneratorConfig config, EntityModel entityModel) {
        super(project);
        this.project = project;
        this.config = config;
        this.entityModel = entityModel;

        setTitle("Configuration de la génération d'API");
        setOKButtonText("Générer");
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Partie gauche : formulaire de configuration
        JPanel configPanel = createConfigPanel();

        // Partie droite : prévisualisation du code
        previewPanel = new JPanel(new BorderLayout());
        previewComponent = new CodePreviewComponent(project, entityModel, config);
        previewPanel.add(previewComponent.getComponent(), BorderLayout.CENTER);

        // Ajouter un diviseur entre les deux parties
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, configPanel, previewPanel);
        splitPane.setResizeWeight(0.4);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        mainPanel.setPreferredSize(new Dimension(1200, 700));
        return mainPanel;
    }

    /**
     * Crée le panneau de configuration avec Swing.
     */
    private JPanel createConfigPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        List<JComponent> fields = new ArrayList<>();

        // Section: Couches à générer
        JPanel layersPanel = new JPanel(new GridLayout(0, 1));
        layersPanel.setBorder(BorderFactory.createTitledBorder("Couches à générer"));
        generateDtoCheckBox = new JBCheckBox("DTO", config.isGenerateDto());
        generateMapperCheckBox = new JBCheckBox("Mapper", config.isGenerateMapper());
        generateRepositoryCheckBox = new JBCheckBox("Repository", config.isGenerateRepository());
        generateServiceCheckBox = new JBCheckBox("Service", config.isGenerateService());
        generateControllerCheckBox = new JBCheckBox("Controller", config.isGenerateController());
        layersPanel.add(generateDtoCheckBox);
        layersPanel.add(generateMapperCheckBox);
        layersPanel.add(generateRepositoryCheckBox);
        layersPanel.add(generateServiceCheckBox);
        layersPanel.add(generateControllerCheckBox);
        panel.add(layersPanel);

        // Section: Options générales
        JPanel optionsPanel = new JPanel(new GridLayout(0, 2));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options générales"));
        optionsPanel.add(new JBLabel("Package de base:"));
        basePackageField = new JBTextField(config.getBasePackage(), 30);
        optionsPanel.add(basePackageField);
        useLombokCheckBox = new JBCheckBox("Utiliser Lombok pour les DTOs", config.isUseLombok());
        optionsPanel.add(useLombokCheckBox);
        optionsPanel.add(new JLabel()); // empty cell
        panel.add(optionsPanel);

        // Section: Noms des packages
        JPanel packagesPanel = new JPanel(new GridLayout(0, 2));
        packagesPanel.setBorder(BorderFactory.createTitledBorder("Noms des packages (relatifs au package de base)"));
        packagesPanel.add(new JBLabel("Package DTOs:"));
        dtoPackageField = new JBTextField(config.getDtoPackage(), 20);
        packagesPanel.add(dtoPackageField);
        packagesPanel.add(new JBLabel("Package Mappers:"));
        mapperPackageField = new JBTextField(config.getMapperPackage(), 20);
        packagesPanel.add(mapperPackageField);
        packagesPanel.add(new JBLabel("Package Repositories:"));
        repositoryPackageField = new JBTextField(config.getRepositoryPackage(), 20);
        packagesPanel.add(repositoryPackageField);
        packagesPanel.add(new JBLabel("Package Services:"));
        servicePackageField = new JBTextField(config.getServicePackage(), 20);
        packagesPanel.add(servicePackageField);
        packagesPanel.add(new JBLabel("Package Controllers:"));
        controllerPackageField = new JBTextField(config.getControllerPackage(), 20);
        packagesPanel.add(controllerPackageField);
        panel.add(packagesPanel);

        // Section: Suffixes des classes
        JPanel suffixesPanel = new JPanel(new GridLayout(0, 2));
        suffixesPanel.setBorder(BorderFactory.createTitledBorder("Suffixes des classes"));
        suffixesPanel.add(new JBLabel("Suffixe DTO:"));
        dtoSuffixField = new JBTextField(config.getDtoSuffix(), 10);
        suffixesPanel.add(dtoSuffixField);
        suffixesPanel.add(new JBLabel("Suffixe Mapper:"));
        mapperSuffixField = new JBTextField(config.getMapperSuffix(), 10);
        suffixesPanel.add(mapperSuffixField);
        suffixesPanel.add(new JBLabel("Suffixe Repository:"));
        repositorySuffixField = new JBTextField(config.getRepositorySuffix(), 10);
        suffixesPanel.add(repositorySuffixField);
        suffixesPanel.add(new JBLabel("Suffixe Service:"));
        serviceSuffixField = new JBTextField(config.getServiceSuffix(), 10);
        suffixesPanel.add(serviceSuffixField);
        suffixesPanel.add(new JBLabel("Suffixe Controller:"));
        controllerSuffixField = new JBTextField(config.getControllerSuffix(), 10);
        suffixesPanel.add(controllerSuffixField);
        panel.add(suffixesPanel);

        // Bouton de prévisualisation
        JButton refreshButton = new JButton("Rafraîchir la prévisualisation");
        refreshButton.addActionListener(e -> updatePreview());
        panel.add(refreshButton);

        // Listeners pour la logique d'activation/désactivation
        setupListeners();

        return panel;
    }

    private void updatePreview() {
        ApiGeneratorConfig tempConfig = new ApiGeneratorConfig();
        updateConfigFromUI(tempConfig);
        if (previewComponent != null) {
            previewComponent.updatePreviews(tempConfig);
        }
    }

    private void setupListeners() {
        generateDtoCheckBox.addActionListener(e -> {
            boolean dtosEnabled = generateDtoCheckBox.isSelected();
            generateMapperCheckBox.setEnabled(dtosEnabled);
            if (!dtosEnabled) {
                generateMapperCheckBox.setSelected(false);
            }
            updatePreview();
        });
        generateMapperCheckBox.addActionListener(e -> updatePreview());
        generateRepositoryCheckBox.addActionListener(e -> updatePreview());
        generateServiceCheckBox.addActionListener(e -> updatePreview());
        generateControllerCheckBox.addActionListener(e -> updatePreview());
        useLombokCheckBox.addActionListener(e -> updatePreview());
        generateMapperCheckBox.setEnabled(generateDtoCheckBox.isSelected());
    }

    private void updateConfigFromUI(ApiGeneratorConfig targetConfig) {
        targetConfig.setGenerateDto(generateDtoCheckBox.isSelected());
        targetConfig.setGenerateMapper(generateMapperCheckBox.isSelected());
        targetConfig.setGenerateRepository(generateRepositoryCheckBox.isSelected());
        targetConfig.setGenerateService(generateServiceCheckBox.isSelected());
        targetConfig.setGenerateController(generateControllerCheckBox.isSelected());

        targetConfig.setBasePackage(basePackageField.getText());
        targetConfig.setUseLombok(useLombokCheckBox.isSelected());

        targetConfig.setDtoPackage(dtoPackageField.getText());
        targetConfig.setMapperPackage(mapperPackageField.getText());
        targetConfig.setRepositoryPackage(repositoryPackageField.getText());
        targetConfig.setServicePackage(servicePackageField.getText());
        targetConfig.setControllerPackage(controllerPackageField.getText());

        targetConfig.setDtoSuffix(dtoSuffixField.getText());
        targetConfig.setMapperSuffix(mapperSuffixField.getText());
        targetConfig.setRepositorySuffix(repositorySuffixField.getText());
        targetConfig.setServiceSuffix(serviceSuffixField.getText());
        targetConfig.setControllerSuffix(controllerSuffixField.getText());
    }

    @Override
    protected void doOKAction() {
        updateConfigFromUI(config);
        if (previewComponent != null) {
            previewComponent.dispose();
        }
        super.doOKAction();
    }

    @Override
    public void doCancelAction() {
        if (previewComponent != null) {
            previewComponent.dispose();
        }
        super.doCancelAction();
    }
}
