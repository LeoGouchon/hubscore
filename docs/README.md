# Documentation Hubscore

Source de vérité du projet : comment l'API fonctionne, les règles à suivre et les étapes pour livrer une feature.
Elle s'adresse autant aux développeurs qu'aux agents IA (Claude Code charge ce fichier via `CLAUDE.md`).

**Lecture progressive** : chaque dossier a un `README.md` qui sert de sommaire. Ouvre uniquement le dossier utile,
puis uniquement les fichiers dont la ligne « À lire si » correspond à ta tâche.

## Sommaire

- [getting-started.md](getting-started.md) : installation, configuration (`config/application.yml`, `.env`),
  commandes, Docker, données de dev, identity server.
  _À lire si_ : tu installes le projet, ou une commande, une variable ou le démarrage ne fonctionne pas.
- [architecture/](architecture/README.md) : fonctionnement actuel du code (domaines et couches, sécurité, données,
  ELO et statistiques, endpoints).
  _À lire si_ : tu dois comprendre ou modifier un mécanisme transverse avant de coder.
- [conventions/](conventions/README.md) : règles à suivre pour tout nouveau code (style Java, structure, API,
  persistance, tests, Git).
  _À lire si_ : tu écris ou relis du code. **Toujours** pour une nouvelle feature.
- [process/](process/README.md) : recettes pas-à-pas (nouvelle feature, endpoint, migration, PR, release).
  _À lire si_ : tu démarres une tâche et veux la checklist complète.
- [decisions/](decisions/README.md) : décisions d'architecture (ADR), c'est-à-dire _pourquoi_ le projet est construit
  ainsi.
  _À lire si_ : tu remets en cause un choix structurant ou tu en prends un nouveau.
- [glossary.md](glossary.md) : vocabulaire métier (joueur, match, ELO, saison…), terme français et nom dans le code.
  _À lire si_ : tu cadres une feature, tu nommes un concept métier ou un terme te semble ambigu.
- [tech-debt.md](tech-debt.md) : écarts connus entre le code actuel et les conventions, bugs repérés, code mort.
  _À lire si_ : le code existant contredit une convention, ou tu cherches quoi nettoyer.

## Contribuer à la doc

1. **Où écrire** :
    - `architecture/` décrit ce qui **est** ;
    - `conventions/` fixe ce qui **doit être** ;
    - `process/` dit **comment faire** ;
    - `decisions/` explique **pourquoi**.
2. **Gabarit d'un fichier** :
    - un titre `#` ;
    - une phrase de résumé ;
    - une section « Fichiers clés » (chemins `src/...` qui font foi) ;
    - le contenu, illustré par des extraits courts du vrai code.
3. **200 lignes visées par fichier.** Au-delà, le hook du plugin `gouchon` (voir
   [l'ADR 0005](decisions/0005-workflow-feature.md)) signale le dépassement et l'agent **propose** un découpage. C'est
   l'auteur qui décide. Si on accepte, le fichier `x.md` devient un dossier `x/` qui suit le même système :
    - un `README.md` : visée, quand y ajouter un fichier, sommaire ;
    - des sous-fichiers.

    Le sommaire parent pointe ensuite vers `x/README.md`. Un fichier qu'on choisit de garder long est ajouté à
    `.claude/doc-length-ignore.txt` : le hook ne le signale plus, et l'exception reste visible en relecture.

4. **Sommaire à jour** : tout nouveau fichier ou dossier est ajouté au `README.md` de son dossier, au format
   `- [fichier.md](fichier.md) : description. *À lire si* : situation.`
5. **Un niveau à la fois** : ce sommaire racine ne pointe que vers les `README.md` des dossiers, jamais directement
   vers leurs sous-fichiers.
6. **Dans la même PR** : un changement de convention, de process, d'architecture ou d'endpoint met à jour la doc
   concernée. Une décision structurante donne lieu à un ADR.
7. **Langue** : la doc est en français ; les identifiants, commandes et chemins restent tels qu'ils sont dans le code.
