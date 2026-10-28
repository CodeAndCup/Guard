# Guard

Guard est un plugin de modération Minecraft pour Spigot/Paper, compatible de la version 1.8 à 1.21.5, développé en Java. Il fournit aux équipes de staff un panel d’outils modernes, simples d’utilisation et puissants pour assurer la sécurité et la sérénité de votre serveur.

## Fonctionnalités principales

- **Menus de sanction rapides** (GUI) : Sanctionnez les joueurs (warn, mute, ban, etc.) facilement via un menu intuitif. Possibilité de sélectionner la cible même si aucun joueur n’est explicitement désigné.
- **Automodération (AutoMod)** : Modère automatiquement les discussions (spam, langage, flood…) en infligeant avertissements et mutes quand les règles ne sont pas respectées.
- **Freeze & FreezeBan** : Gèle un joueur pour contrôle, et le bannit automatiquement s’il tente de se déconnecter pendant un freeze (fonction anti-cheat).
- **Outils de staff modernes** : Interfaces graphiques, logs des actions, commandes accessibles, menus adaptés pour que les modérateurs travaillent efficacement.
- **Gestion avancée des sanctions** : Historique, avertissements progressifs, gestion centralisée dans le menu SanctionSet.
- **Compatibilité & performances** : Fonctionne sur une large gamme de versions (1.8 à 1.21.5), inclut bStats (statistiques anonymes des usages) et un système d’auto-update.

## Installation

1. Téléchargez le `Guard.jar` depuis la page de release ou SpigotMC.
2. Placez-le dans le dossier `plugins` de votre serveur.
3. Redémarrez votre serveur pour générer les fichiers de configuration.

## Configuration

Une fois installé, le dossier `Guard` apparaît dans `plugins/`.
- Vous pouvez modifier les messages, régler les règles de l’automod, personnaliser les durées de sanctions, etc. via les fichiers YAML générés.

## Commandes clés

| Commande                  | Description                          | Permission associée      |
|---------------------------|--------------------------------------|--------------------------|
| `/guard gui`              | Ouvre le menu de modération staff    | guard.staff.gui          |
| `/guard freeze <joueur>`  | Gèle un joueur                       | guard.staff.freeze       |
| `/guard ban <joueur>`     | Bannit un joueur                     | guard.staff.ban          |
| `/guard mute <joueur>`    | Mute un joueur                       | guard.staff.mute         |
| `/guard warn <joueur>`    | Avertit un joueur                    | guard.staff.warn         |

## Permissions

Une permission spécifique pour chaque commande ou menu est disponible, permettant une gestion fine des accès.

## Contribution

1. Clonez le repo :  
   `git clone https://github.com/CodeAndCup/Guard.git`
2. Ouvrez le projet dans IntelliJ ou Eclipse.
3. Compilez avec Maven (`pom.xml` présent).

## Support

Pour toute question ou suggestion :
- Ouvrez une issue GitHub.
- Retrouvez l’équipe sur Discord (lien sur la page SpigotMC).

## Liens utiles

- [Page SpigotMC](https://www.spigotmc.org/resources/guard-moderation-plugin-1-8-to-1-21-5.116879/)
- [bStats Overview](https://bstats.org/plugin/bukkit/Guard)

---

Guard – La modération moderne, simple et efficace.
