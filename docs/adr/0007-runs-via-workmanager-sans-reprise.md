# Tous les runs passent par WorkManager, avec une échéance et sans reprise

Chaque run s'exécute dans un worker WorkManager : un travail expedited quand l'utilisateur envoie un message, un travail différé pour une tâche planifiée (ADR 0003). Un seul chemin d'exécution survit au passage en arrière-plan, et le nom de travail unique par conversation (`ExistingWorkPolicy.KEEP`) garantit au plus un run actif par conversation. Comme le système arrête un worker après environ 10 minutes, un run a une échéance interne de 8 minutes (issue `TimedOut`) et une confirmation expire après 5 minutes au plus ; une tâche planifiée qui demande une confirmation pendant que l'utilisateur dort verra donc son écriture sensible abandonnée, ce qui est voulu en v0. Un run n'est jamais repris : chaque étape est persistée dès qu'elle est terminée, un processus tué laisse un run `Interrupted`, et le worker renvoie toujours `Result.success()`, car rejouer un run pourrait exécuter deux fois un outil.

## Consequences

- Ne pas renvoyer `Result.retry()` depuis le worker ni ajouter de reprise automatique sans revoir l'idempotence des outils.
- Garder une confirmation en attente au-delà du run (et relancer un run à la réponse) est l'évolution prévue si les expirations nocturnes gênent.
