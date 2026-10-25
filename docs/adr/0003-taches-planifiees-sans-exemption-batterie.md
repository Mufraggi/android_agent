# Tâches planifiées sans exemption batterie ni alarme exacte

Les tâches planifiées s'exécutent via le planificateur différé d'Android, sans demander l'exemption d'optimisation batterie ni la permission d'alarme exacte : Google Play restreint ces deux permissions et une app d'assistant risque le rejet. En contrepartie, une tâche prévue « à 8 h » peut s'exécuter avec quelques minutes de retard ; c'est voulu, ne pas « corriger » en ajoutant ces permissions sans revoir la politique Play.
