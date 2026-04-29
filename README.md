# NoammBot

Discord bot with Kotlin + JDA. AI conversations via Groq API. Custom event management + dynamic command registration.

## ️ Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org)
- **Library**: [JDA](https://github.com/discord-jda/JDA)
- **AI**: [Groq API](https://groq.com)
- **JSON**: [Gson](https://github.com/google/gson)
- **DI/Reflection**: [Reflections](https://github.com/ronmamo/reflections)
- **Build**: [Gradle](https://gradle.org) Kotlin DSL + Shadow Jar.

## Setup

### Requirements

- **JDK 17+**.
- **Discord Bot Token** from [Developer Portal](https://discord.com/developers/applications).
- **Groq API Key** from [Groq Console](https://console.groq.com/).

### Env Vars

| Variable | Description |
| :--- | :--- |
| `DISCORD_BOT_TOKEN` | Discord bot secret token. |
| `GROQ_API_KEY` | Groq API key for AI. |

### Build

```bash
./gradlew build
```

JAR path: `build/libs/NoammBot.jar`.

### Run

```bash
# Linux/macOS
export DISCORD_BOT_TOKEN="your_token"
export GROQ_API_KEY="your_key"
java -jar build/libs/NoammBot.jar

# Windows (PowerShell)
$env:DISCORD_BOT_TOKEN="your_token"
$env:GROQ_API_KEY="your_key"
java -jar build/libs/NoammBot.jar
```

## Structure

- `src/main/kotlin/`:
    - `NoammBot.kt`: Entry point.
    - `commands/`: Slash commands.
    - `features/`: Modular features/listeners.
    - `managers/`: AI, Commands, Event Bus.
    - `interfaces/`: API models + interfaces.
    - `annotations/`: Auto-registration annotations.
- `src/main/resources/`:
    - `faq.json`: FAQ config.
    - `logback.xml`: Logging config.

## Contributing

1. Fork.
2. Feature branch (`git checkout -b feature/name`).
3. Commit (`git commit -m 'Add feature'`).
4. Push.
5. Pull Request.

## License

[MIT License](LICENSE)