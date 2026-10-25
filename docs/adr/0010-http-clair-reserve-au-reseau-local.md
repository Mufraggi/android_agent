# HTTP en clair réservé au réseau local, vérifié dans le code

Les fournisseurs locaux (Ollama, LM Studio) s'exposent souvent en `http://192.168.x.x`, mais `network_security_config` ne sait pas autoriser le trafic en clair « vers les adresses privées seulement ». La configuration réseau autorise donc le clair globalement, et c'est le parsing de l'URL de base du fournisseur (ADR 0004) qui refuse `http` sauf vers `localhost`, une adresse IP privée ou un nom en `.local` ; `fetch_url` reste en `https` seul (ADR 0008). Nous avons préféré cette vérification dans le code à l'interdiction du clair, qui aurait exclu les modèles locaux, et à une liste d'hôtes fixée dans la configuration, impossible à connaître d'avance.

## Consequences

- La configuration réseau Android ne protège plus rien : tout nouveau client HTTP doit passer par une URL parsée qui applique cette règle.
