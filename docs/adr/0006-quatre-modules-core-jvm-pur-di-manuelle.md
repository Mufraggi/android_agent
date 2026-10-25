# Quatre modules, `:core` en JVM pur, injection de dépendances manuelle

Le projet est découpé par adaptateur technique et non par feature : `:core` (le domaine, ses ports et leurs erreurs), `:llm` (le fournisseur compatible OpenAI), `:storage` (Room) et `:app` (UI, adaptateurs système Android, composition root). `:core` et `:llm` sont des modules Kotlin/JVM purs, si bien que le compilateur interdit toute dépendance Android au domaine et que leurs tests tournent sans émulateur ; `:llm` et `:storage` dépendent de `:core` sans se voir l'un l'autre, et seul `:app` les assemble. Les dépendances sont câblées à la main dans un `AppContainer` plutôt qu'avec Hilt, qui aurait imposé des annotations dans `:core` ou un module de binding par type pour un graphe d'une quinzaine d'objets. Nous avons écarté le découpage par feature (style « Now in Android ») parce qu'il disperserait le domaine que l'ADR 0004 veut d'un seul tenant.

## Consequences

- Un nouveau fournisseur natif ou le modèle sur l'appareil devient un nouveau module d'adaptateur (`:llm-local`, etc.), pas une modification de `:llm`.
- Le graphe de modules est la seule garde d'architecture en v0 : ne pas exposer `:llm` ou `:storage` via `api`, ni ajouter de dépendance Android à `:core`.
