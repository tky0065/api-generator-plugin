# Configuration des secrets pour la publication automatique

Pour permettre la publication automatique de votre plugin vers le JetBrains Marketplace via GitHub Actions, vous devez configurer un token d'authentification comme secret GitHub.

## Obtention du token JetBrains Marketplace

1. Connectez-vous à votre compte sur le [JetBrains Marketplace](https://plugins.jetbrains.com/)
2. Accédez à votre profil (icône en haut à droite) > **Developer Tools**
3. Dans la section **Permanent Tokens**, cliquez sur **New Token**
4. Donnez un nom à votre token (par exemple, "GitHub Actions Deploy")
5. Copiez le token généré (il ne sera affiché qu'une seule fois)

## Configuration du secret dans GitHub

1. Accédez à votre dépôt GitHub
2. Cliquez sur **Settings** > **Secrets and variables** > **Actions**
3. Cliquez sur **New repository secret**
4. Entrez le nom du secret: `JETBRAINS_MARKETPLACE_TOKEN`
5. Collez le token copié précédemment dans le champ "Value"
6. Cliquez sur **Add secret**

## Vérification

Le workflow de publication (`release.yml`) est déjà configuré pour utiliser ce secret via:

```yaml
env:
  PUBLISH_TOKEN: ${{ secrets.JETBRAINS_MARKETPLACE_TOKEN }}
```

Une fois ces étapes accomplies, le workflow GitHub Actions pourra s'authentifier auprès du JetBrains Marketplace et publier automatiquement votre plugin lors d'une nouvelle release.

## Sécurité

- Ne partagez jamais ce token directement dans votre code
- Le token a des permissions élevées, ne le compromettez pas
- Si vous pensez que votre token a été compromis, régénérez-en un nouveau immédiatement dans le JetBrains Marketplace
