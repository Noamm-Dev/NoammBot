# NoammBot

A Discord bot built with Kotlin and JDA (Java Discord API), featuring AI-driven conversations via the Groq API, custom event management, and dynamic command registration.

## ️ Tech Stack

- **Language**: [Kotlin](https://kotlinlang.org)
- **Library**: [JDA (Java Discord API)](https://github.com/discord-jda/JDA)
- **AI Backend**: [Groq API](https://groq.com)
- **JSON Parsing**: [Gson](https://github.com/google/gson)
- **Dependency Injection/Reflection**: [Reflections Library](https://github.com/ronmamo/reflections)
- **Build Tool**: [Gradle](https://gradle.org) Kotlin DSL with Shadow Jar plugin "fat" jar

## Setup & Installation

### Requirements

- **JDK 17** or higher.

- A **Discord Bot Token** from the [Discord Developer Portal](https://discord.com/developers/applications).
- A **Groq API Key** from [Groq Console](https://console.groq.com/).

### Environment Variables

The bot requires the following environment variables to run:

| Variable | Description |
| :--- | :--- |
| `DISCORD_BOT_TOKEN` | Your Discord bot's secret token. |
| `GROQ_API_KEY` | Your Groq API key for AI features. |

### Building

To build the bot and generate a fat JAR (Shadow JAR):

```bash
./gradlew build
```

The output JAR will be located in `build/libs/NoammBot.jar`.

### Running

```bash
# On Linux/macOS
export DISCORD_BOT_TOKEN="your_token"
export GROQ_API_KEY="your_key"
java -jar build/libs/NoammBot.jar

# On Windows (PowerShell)
$env:DISCORD_BOT_TOKEN="your_token"
$env:GROQ_API_KEY="your_key"
java -jar build/libs/NoammBot.jar
```

## Project Structure

- `src/main/kotlin/`:
    - `NoammBot.kt`: Main entry point and initialization.
    - `commands/`: Contains slash command implementations (e.g., `CalcPingCommand`).
    - `features/`: Modular features and event listeners (e.g., `ChatBot`).
    - `managers/`: Core services (AI, Command Management, Event Bus).
    - `interfaces/`: API models and command interfaces.
    - `annotations/`: Custom annotations for automated registration.
- `src/main/resources/`:
    - `faq.json`: Configuration for the FAQ classifier.
    - `logback.xml`: Logging configuration.

## Contributing

1.  Fork the repository.
2.  Create your feature branch (`git checkout -b feature/amazing-feature`).
3.  Commit your changes (`git commit -m 'Add some amazing feature'`).
4.  Push to the branch (`git push origin feature/amazing-feature`).
5.  Open a Pull Request.

## License

This project is licensed under the [MIT License](LICENSE)