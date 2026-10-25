# agentiqueMob

Une app Android qui héberge un agent personnel, dans l'esprit de Hermes, directement sur le téléphone de l'utilisateur. L'agent converse, appelle des outils et exécute des tâches en arrière-plan.

Le glossaire est en français ; tout le code est en anglais. Chaque terme indique son nom canonique dans le code (`_Code_`), à utiliser tel quel.

## Langage

### Agent et modèles

**Agent** :
L'assistant IA hébergé dans l'app, qui raisonne avec un modèle et agit via des outils.
_Code_ : `Agent`
_Éviter_ : bot, assistant, IA

**Fournisseur** :
Le service ou le runtime qui exécute un modèle (OpenRouter, un modèle local sur l'appareil, etc.).
_Code_ : `Provider`
_Éviter_ : backend, API

**Modèle** :
Un LLM précis proposé par un fournisseur, que l'utilisateur choisit pour son agent.
_Code_ : `Model`
_Éviter_ : LLM, IA, moteur

**Clé API** :
L'identifiant personnel que l'utilisateur fournit pour accéder à un fournisseur ; l'app n'en fournit aucune.
_Code_ : `ApiKey`
_Éviter_ : token, credential, secret

**Instructions personnelles** :
Un court texte libre que l'utilisateur ajoute au prompt système de l'agent (ton, préférences) ; il ne prime jamais sur les règles du prompt de base.
_Code_ : `PersonalInstructions`
_Éviter_ : prompt personnalisé, persona, custom instructions

**Budget quotidien** :
Le nombre maximal de tokens que les runs peuvent consommer dans une journée locale ; au-delà, aucun run ne démarre.
_Code_ : `DailyTokenBudget`
_Éviter_ : quota, limite, crédit

### Exécution

**Run** :
Une exécution complète de la boucle de l'agent, déclenchée par un message de l'utilisateur ou par une tâche planifiée, jusqu'à la réponse finale.
_Code_ : `Run`
_Éviter_ : exécution, tour, job, session

**Itération** :
Une étape d'un run : un appel au modèle suivi de l'exécution des outils qu'il a demandés ; un run en compte un nombre plafonné.
_Code_ : `RunIteration`
_Éviter_ : tour, étape, boucle

**Issue** :
La façon dont un run s'est terminé (achevé, plafond d'itérations atteint, échéance dépassée, budget épuisé, annulé, interrompu, échoué).
_Code_ : `RunOutcome`
_Éviter_ : résultat, statut, fin

**Journal** :
L'historique des runs : déclencheur, outils appelés, tokens consommés, issue.
_Code_ : `RunLog`
_Éviter_ : logs, historique, traces

**Outil** :
Une capacité que l'agent peut invoquer pour lire ou modifier le monde (calendrier, rappels, mémoire).
_Code_ : `Tool`
_Éviter_ : fonction, action, plugin

**Tâche planifiée** :
Une consigne donnée à l'agent pour un moment futur, unique ou récurrente (« tous les matins à 8 h, fais X »), qui déclenche un run même app fermée.
_Code_ : `ScheduledTask`
_Éviter_ : cron, job, routine, automatisation

**Rappel** :
Une notification à texte fixe programmée pour un moment donné ; contrairement à une tâche planifiée, elle ne déclenche aucun run.
_Code_ : `Reminder`
_Éviter_ : alarme, alerte, tâche

**Écriture sensible** :
Une action d'outil visible hors de l'app, qui crée ou supprime un déclenchement futur de runs, qui contacte un domaine non autorisé, ou qui retient un souvenir dans un run exposé (créer un événement de calendrier, créer ou annuler une tâche planifiée) ; créer un rappel n'en est pas une.
_Code_ : `SensitiveWrite`
_Éviter_ : action critique, écriture (sans qualificatif)

**Famille d'outils** :
Un ensemble d'outils que l'utilisateur active d'un bloc dans les réglages, avec la permission Android qu'il requiert ; une famille inactive est invisible pour l'agent.
_Code_ : `ToolGroup`
_Éviter_ : catégorie, module, pack

**Domaine autorisé** :
Un domaine web que l'utilisateur a écrit lui-même dans la conversation ou la consigne d'une tâche planifiée, et que l'agent peut donc contacter sans confirmation.
_Code_ : `AllowedDomain`
_Éviter_ : whitelist, domaine de confiance

**Contenu tiers** :
Un résultat d'outil dont le texte a été écrit par quelqu'un d'autre que l'utilisateur ou l'agent (page web, événement de calendrier).
_Code_ : `ThirdPartyContent`
_Éviter_ : contenu externe, données non fiables

**Run exposé** :
Un run qui a reçu un contenu tiers ; y retenir un souvenir devient une écriture sensible.
_Code_ : `Run.exposed`
_Éviter_ : run contaminé, run tainted

**Confirmation** :
L'accord explicite (Oui/Non) que l'utilisateur donne avant que l'agent n'exécute une écriture sensible ; sans réponse dans le délai imparti, l'écriture est abandonnée.
_Code_ : `Confirmation`
_Éviter_ : validation, approbation

### Conversations et mémoire

**Conversation** :
Un fil de messages entre l'utilisateur et l'agent ; chaque tâche planifiée possède la sienne, où s'accumulent ses résultats.
_Code_ : `Conversation`
_Éviter_ : chat, thread, session, discussion

**Conversation principale** :
L'unique conversation libre entre l'utilisateur et l'agent ; les runs des tâches planifiées n'y ont pas accès.
_Code_ : `MainConversation`
_Éviter_ : chat général, accueil

**Souvenir** :
Un fait court sur l'utilisateur que l'agent a décidé de retenir de lui-même, et que l'utilisateur peut consulter ou supprimer.
_Code_ : `Memory`
_Éviter_ : mémoire (pour un fait isolé), note, fact

**Mémoire** :
L'ensemble des souvenirs de l'agent.
_Code_ : `MemoryBank`
_Éviter_ : base de connaissances, contexte

**Compétence** :
Une procédure réutilisable que l'agent rédige lui-même à partir de son expérience (prévue après la v0).
_Code_ : `Skill`
_Éviter_ : recette, workflow
