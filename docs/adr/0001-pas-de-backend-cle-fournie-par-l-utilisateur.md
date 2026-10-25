# Pas de backend : clé API fournie par l'utilisateur, données sur l'appareil

L'app est publiée sur le Play Store mais n'a aucun serveur : chaque utilisateur colle sa propre clé API (OpenRouter par défaut), chiffrée sur l'appareil, et les conversations, souvenirs et journal ne quittent jamais le téléphone, sauf les messages envoyés au fournisseur choisi. Nous l'avons choisi pour éviter les coûts d'inférence, la gestion de comptes et la responsabilité de stocker des données personnelles, au prix d'un onboarding plus exigeant (obtenir une clé) et de l'absence de synchronisation entre appareils.

## Consequences

- Pas d'analytics ni de crash reporting distant : le débogage chez les testeurs passe par l'export manuel du journal.
- Ajouter un backend plus tard (abonnement, sync) remettrait en cause le formulaire « Sécurité des données » du Play Store.
