# Un seul protocole de fournisseur : l'API compatible OpenAI

En v0, l'agent ne parle qu'un protocole, le format Chat Completions compatible OpenAI, avec une URL de base configurable. OpenRouter, Ollama, LM Studio, Groq, Mistral et d'autres le parlent déjà, ce qui donne le multi-fournisseur pour le coût d'une seule intégration. Les API natives (Anthropic, Gemini) et le modèle sur l'appareil viendront plus tard derrière la même interface Fournisseur, au prix de fonctionnalités propres à chaque API (cache de prompt, etc.) inaccessibles en attendant.
