# Changelog

Toutes les modifications notables apportées à ce projet seront documentées dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/),
et ce projet adhère au [Versionnement Sémantique](https://semver.org/lang/fr/).

## [Non publié]
### Ajouté
- Système de prévisualisation du code généré dans l'interface
- Validation des dépendances requises dans le projet utilisateur
- Détection des fichiers existants pour éviter l'écrasement accidentel

### Modifié
- Amélioration des messages d'erreur avec suggestions de résolution
- Intégration d'un système de journalisation pour faciliter le débogage

## [1.0.0] - 2025-07-04
### Ajouté
- Génération de classes DTO avec support Lombok optionnel
- Génération de Repository JPA avec méthodes de recherche basiques
- Génération de classes Service avec opérations CRUD complètes
- Génération de Controllers REST exposant toutes les opérations CRUD
- Génération de Mappers avec MapStruct pour la conversion DTO-Entité
- Interface utilisateur configurable avec JetBrains UI DSL
- Support des entités JPA avec relations (OneToMany, ManyToOne, etc.)
- Analyse PSI pour extraire les informations des classes @Entity
- Options de configuration pour les packages et suffixes de classes
- Documentation complète avec guide d'utilisation
