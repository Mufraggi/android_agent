# Erreurs typées avec Arrow (`Either` / `Raise`)

Les erreurs métier ne sont jamais des exceptions : elles sont typées dans les signatures via `arrow-core` (`Either<E, A>` et le DSL `Raise<E>`), avec une `sealed interface` d'erreur par frontière (parsing d'un type de domaine, chaque port) et une issue dédiée par cas d'usage. Nous avons préféré Arrow à un type résultat maison et à `kotlin.Result` (qui ne type pas l'erreur) parce qu'il apporte le style direct (`either { … .bind() }`), l'accumulation d'erreurs pour le parsing (`zipOrAccumulate`) et un modèle familier venant d'Effect-TS ; en contrepartie, tout `:core` dépend d'Arrow et le retirer plus tard serait coûteux.

## Consequences

- Les exceptions sont réservées aux bugs : les adaptateurs traduisent les exceptions techniques en erreurs de port, `CancellationException` est toujours relancée, et une seule frontière (le Run) attrape l'imprévu.
