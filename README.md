# Junior Hockey Franchise Mode

A fully-featured junior hockey franchise simulator built in Java with a JavaFX GUI. Manage your team through a complete season — sign free agents, make trades, scout prospects at the draft, and compete for a championship across a 14-team league.

![Java](https://img.shields.io/badge/Java-17%2B-orange?style=flat-square&logo=java)
![JavaFX](https://img.shields.io/badge/JavaFX-21-blue?style=flat-square)
![Status](https://img.shields.io/badge/Status-Active-brightgreen?style=flat-square)

---

## Features

- **Franchise Mode** — Pick a team and manage it across multiple seasons
- **Full 44-Game Regular Season** — Oct 1 → Jan 9, 3 games/week with real scheduling
- **Playoff Bracket** — Seeded postseason tournament with day-by-day simulation
- **Trade System** — Propose and evaluate trades with AI-driven team logic
- **Free Agency** — Sign players using a signing budget that resets each offseason
- **Annual Draft** — Scout prospects with hidden potential ratings before selecting
- **Player Development** — Players improve or regress between seasons based on age, potential, and work ethic
- **Injury System** — Players miss games with realistic injury durations
- **Live Standings** — Updated in real-time, sorted by PTS → W → GP
- **News Ticker** — In-game news feed for transactions, injuries, and results
- **Save/Load** — Full franchise state serialized to disk
- **14 Teams** — Each with unique logos and team-color themed UI accents

---

## Gameplay

| Phase | Dates | What You Can Do |
|---|---|---|
| Regular Season | Oct 1 – Jan 9 | Simulate games, make trades (before Dec 15 deadline), sign/release players |
| Playoffs | Jan 15+ | Day-by-day bracket simulation |
| Off-Season | After playoffs | Player development, free agency, entry draft |

**Key controls:**
- **Next Game** — Jump forward to your next scheduled game with a full box score
- **Adv. Day** — Simulate one calendar day at a time
- Left navigation panel — Access all screens (Roster, Standings, Trades, FA, Draft, History) at any time

---

## Tech Stack

- **Language:** Java 17+
- **UI Framework:** JavaFX 21
- **Persistence:** Java Object Serialization (`SaveManager`)
- **Architecture:** Layered package structure — `core`, `domain`, `simulation`, `ui`

### Project Structure
```
src/com/juniorhockeysim/
├── Main.java                  # Entry point
├── core/
│   ├── FranchiseMode.java     # Central game state & season logic
│   ├── DraftManager.java      # Draft prospect generation & picks
│   ├── FreeAgentManager.java  # FA pool & signing logic
│   ├── TradeManager.java      # Trade proposal & AI evaluation
│   ├── SaveManager.java       # Save/load serialization
│   └── GameDate.java          # Custom calendar system
├── domain/
│   ├── Player.java            # Player attributes, ratings, stats, contracts
│   ├── Team.java              # Roster management & team stats
│   ├── League.java            # League-wide data
│   ├── Position.java          # Player position enum
│   └── NameGenerator.java     # Procedural player name generation
├── simulation/
│   ├── GameSimulator.java     # Per-game simulation engine
│   ├── SeasonSimulator.java   # Full season orchestration
│   ├── ScheduleGenerator.java # Round-robin schedule builder
│   ├── PlayoffBracket.java    # Bracket seeding & structure
│   ├── PlayoffSimulator.java  # Playoff series simulation
│   ├── PlayoffSeries.java     # Best-of series state
│   ├── ScheduledGame.java     # Individual game data
│   └── GameResult.java        # Post-game result model
└── ui/
    ├── HockeyApp.java         # JavaFX application & screen controller
    ├── TeamColors.java        # Per-team color theming
    └── TeamLogos.java         # Logo asset loader
```

---

## Getting Started

### Prerequisites
- Java 17 or higher — [Download JDK](https://adoptium.net/)
- JavaFX 21 SDK — [Download from Gluon](https://gluonhq.com/products/javafx/)
- IntelliJ IDEA (recommended) — [Download](https://www.jetbrains.com/idea/)

### Setup in IntelliJ IDEA

1. **Clone the repo**
   ```bash
   git clone https://github.com/YOURUSERNAME/junior-hockey-sim.git
   cd junior-hockey-sim
   ```

2. **Open the project**
   `File → Open → select the juniorhockeysim folder → OK`

3. **Mark Sources Root**
   Right-click `src` → `Mark Directory as → Sources Root`

4. **Set Java SDK**
   `File → Project Structure → Project SDK → Java 17+`

5. **Add JavaFX as a Library**
   `File → Project Structure → Libraries → + → Java`
   Navigate to your JavaFX SDK `lib` folder and select all `.jar` files → Apply

6. **Add VM Options to Run Config**
   `Run → Edit Configurations → Modify options → Add VM Options`
   ```
   --module-path /path/to/javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml
   ```
   > **Windows example:** `--module-path C:\javafx-sdk-21\lib --add-modules javafx.controls,javafx.fxml`
   > **Mac/Linux example:** `--module-path ~/javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml`

7. **Run**
   Right-click `Main.java → Run 'Main.main()'`

---

## Player Rating System

Each player has six core attributes rated 1–100:

| Attribute | Affects |
|---|---|
| Shooting | Goal scoring probability |
| Passing | Assist generation |
| Skating | Overall offensive contribution |
| Defense | Goals-against reduction |
| Physical | Board battles, defensive zone play |
| Save Rating | Goalie-specific save percentage |

Players also have hidden **Potential** (60–99) and **Work Ethic** (50–100) ratings that determine development trajectory. Rare **"gem"** prospects represent generational talents with much higher potential.

---

## Save System

Franchises are saved to disk via Java serialization. Multiple save slots are supported. Save files persist full league state including standings, player stats, contract years, and season history.

---

## Planned Features

- [ ] Player morale events and chemistry
- [ ] Staff hiring (coaches, scouts)
- [ ] Multi-season statistical leaderboards
- [ ] Export season stats to CSV

---

## Author

**Maddox Flood** — [@mflo77](https://github.com/yourhandle)

Built as a personal project during first year of university.

---
🎮 [Play it on itch.io](https://mflo9.itch.io/crimson-dynasty)
---

## License

This project is open source under the [MIT License](LICENSE).
