# API Generator - Plugin IntelliJ IDEA

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Java](https://img.shields.io/badge/Java-11%2B-orange.svg)
![IntelliJ](https://img.shields.io/badge/IntelliJ%20IDEA-2023.1%2B-purple.svg)

Un plugin IntelliJ IDEA qui accélère le développement backend en générant automatiquement une API CRUD complète à partir d'une entité JPA.

## 🚀 Fonctionnalités

- **Génération complète d'API** : Créez rapidement une API REST fonctionnelle à partir de vos entités JPA
- **Flexibilité** : Choisissez les composants à générer (DTO, Repository, Service, Controller, Mapper)
- **Prévisualisation** : Visualisez le code avant de l'ajouter à votre projet
- **Hautement configurable** : Personnalisez les packages et les noms des classes générées
- **Support Lombok** : Option pour générer des DTOs avec Lombok
- **Sécurité** : Protection contre l'écrasement accidentel des fichiers existants

## 📋 Prérequis

- IntelliJ IDEA (Community ou Ultimate) version 2023.1 ou ultérieure
- Java 11 ou supérieur
- Un projet avec Spring Boot et JPA

## 🔧 Installation

### À partir du JetBrains Marketplace

1. Ouvrez IntelliJ IDEA
2. Allez dans `File > Settings > Plugins > Marketplace`
3. Recherchez "API Generator"
4. Cliquez sur "Install"
5. Redémarrez IntelliJ IDEA quand vous y êtes invité

### Installation manuelle

1. Téléchargez le fichier `.zip` du plugin depuis la [page des releases](https://github.com/tky0065/api-generator/releases)
2. Dans IntelliJ IDEA, allez dans `File > Settings > Plugins`
3. Cliquez sur l'icône d'engrenage ⚙️ et sélectionnez "Install Plugin from Disk..."
4. Sélectionnez le fichier `.zip` téléchargé
5. Redémarrez IntelliJ IDEA

## 📝 Guide d'utilisation

### Étape 1 : Créez une entité JPA

```java
@Entity
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String description;
    private BigDecimal price;
    
    // getters, setters, constructeurs...
}
```

### Étape 2 : Générez l'API

1. Ouvrez votre classe d'entité JPA
2. Faites un clic droit sur le nom de la classe ou dans l'éditeur
3. Sélectionnez `Generate > Generate API`
4. Dans la boîte de dialogue, configurez les options selon vos besoins :
   - Sélectionnez les composants à générer (DTO, Repository, Service, etc.)
   - Configurez les packages cibles
   - Personnalisez les suffixes des classes
   - Activez ou désactivez Lombok pour les DTOs

5. Prévisualisez le code à générer dans le panneau de droite
6. Cliquez sur `Générer` pour ajouter les fichiers à votre projet

### Étape 3 : Résolution des dépendances

Si votre projet ne possède pas toutes les dépendances nécessaires, le plugin vous proposera d'ajouter les dépendances manquantes. Vous pourrez :

- Copier les dépendances Maven ou Gradle dans le presse-papier
- Les ajouter à votre `pom.xml` ou `build.gradle`
- Recharger votre projet

## 🔍 Composants générés

### DTO (Data Transfer Object)

```java
// Generated by API Generator
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
}
```

### Mapper (avec MapStruct)

```java
// Generated by API Generator
@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductDTO toDto(Product entity);
    Product toEntity(ProductDTO dto);
    List<ProductDTO> toDtoList(List<Product> entities);
    List<Product> toEntityList(List<ProductDTO> dtos);
}
```

### Repository

```java
// Generated by API Generator
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String name);
}
```

### Service

```java
// Generated by API Generator
@Service
public class ProductService {
    private final ProductRepository repository;
    
    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }
    
    public List<Product> findAll() {
        return repository.findAll();
    }
    
    public Optional<Product> findById(Long id) {
        return repository.findById(id);
    }
    
    public Product save(Product entity) {
        return repository.save(entity);
    }
    
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
```

### Controller REST

```java
// Generated by API Generator
@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;
    private final ProductMapper mapper;
    
    public ProductController(ProductService service, ProductMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }
    
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAll() {
        List<Product> products = service.findAll();
        return ResponseEntity.ok(mapper.toDtoList(products));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(product -> ResponseEntity.ok(mapper.toDto(product)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<ProductDTO> create(@RequestBody ProductDTO dto) {
        Product saved = service.save(mapper.toEntity(dto));
        return ResponseEntity.created(null).body(mapper.toDto(saved));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> update(@PathVariable Long id, @RequestBody ProductDTO dto) {
        return service.findById(id)
                .map(existing -> {
                    Product updated = mapper.toEntity(dto);
                    updated.setId(id);
                    return ResponseEntity.ok(mapper.toDto(service.save(updated)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

## 📚 FAQ

**Q : Le plugin est-il compatible avec les versions antérieures d'IntelliJ IDEA ?**  
R : Le plugin est conçu pour IntelliJ IDEA 2023.1 et versions ultérieures. Il pourrait fonctionner avec des versions antérieures, mais cela n'est pas officiellement supporté.

**Q : Puis-je générer une API pour plusieurs entités à la fois ?**  
R : Actuellement, le plugin traite une entité à la fois. Le support pour la génération en lot pourrait être ajouté dans une version future.

**Q : Est-il possible de personnaliser les templates de génération ?**  
R : La personnalisation des templates est prévue pour une version future.

**Q : Le plugin fonctionne-t-il avec d'autres frameworks de persistance que JPA ?**  
R : Pour l'instant, seul JPA est pris en charge. Le support pour d'autres technologies comme MongoDB est prévu.

**Q : Que se passe-t-il si j'ai déjà des fichiers générés et que je régénère l'API ?**  
R : Le plugin détectera les fichiers existants et vous demandera si vous souhaitez les remplacer, les ignorer ou générer de nouveaux fichiers avec des noms différents.

## 🐛 Signalement de problèmes

Si vous rencontrez des problèmes ou avez des suggestions, veuillez créer une issue sur notre [page GitHub](https://github.com/tky0065/api-generator-plugin/issues).

## 🔧 Contribuer

Les contributions sont les bienvenues ! Consultez notre [guide de contribution](CONTRIBUTING.md) pour plus de détails.

## 📄 Licence

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.
