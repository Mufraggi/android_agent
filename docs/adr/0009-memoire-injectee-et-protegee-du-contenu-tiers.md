# Mémoire : tout injecter, et confirmer `remember` après un contenu tiers

En v0, la mémoire est injectée en entier dans le prompt système à chaque appel au modèle, sans recherche par pertinence : 100 souvenirs de 200 caractères au plus, au-delà desquels `remember` renvoie une erreur typée. Une recherche aurait exigé des embeddings, donc un second fournisseur ou un modèle local. Comme un souvenir est relu par tous les runs futurs, un souvenir contaminé par une injection de prompt agirait de façon permanente : chaque outil déclare donc si son résultat est un contenu tiers (`fetch_url`, `list_events`, car n'importe qui peut envoyer une invitation de calendrier), et dès qu'un run en a reçu un, il est exposé et `remember` y devient une écriture sensible. Les runs des tâches planifiées, qui tournent sans l'utilisateur et lisent souvent le web, lisent la mémoire sans pouvoir y écrire. Nous avons préféré cette règle à une confirmation systématique de `remember`, plus sûre mais pénible dans la conversation principale.

## Consequences

- Tout nouvel outil doit déclarer si son résultat est un contenu tiers ; en cas de doute, il en est un.
- Passer à une mémoire plus grande que le plafond demandera une recherche par pertinence, et donc un nouvel ADR.
