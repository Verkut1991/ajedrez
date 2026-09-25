# Ajedrez

A quiet local chess game for Android. No accounts, no lobbies, no browser tab — just open the app, sit with the board, and play a few minutes against a light AI on your phone.

You are White. Tap a piece and the board lights the squares it can reach (if you leave that helper on). Move, wait a short beat for Black to answer, and keep going. The feel is meant to stay close to a physical board: clear squares, readable pieces, and a pace that does not rush you.

## What you get

The rules live entirely on the device. Movement validation, check and checkmate, castling, and en passant are handled in the local game logic — nothing is sent anywhere to decide a legal move.

The opponent is a simple on-device AI. It scores candidate moves (captures, checks, center control, getting out of danger) and picks among the best ones. It is not a deep engine and it is not meant to be; it is enough to push back without turning the phone into a tournament machine.

During a game you get a countdown clock for your side (5, 10, or 30 minutes, chosen in options), a turn indicator, live capture counts by piece type, and a resign button when you are done. When the game ends — mate, time, or resign — a stats screen shows the result message, how long the game lasted, and how many pieces each side took.

## Options

From the home screen you can open **Opciones** and tweak the session:

- Player name
- Master volume and optional looping background music
- Highlight legal moves on or off
- Vibration on piece moves
- Dark mode
- Match length: 5 / 10 / 30 minutes

Preferences are stored locally with SharedPreferences and apply the next time you play.

## Stack

| Piece | Detail |
| --- | --- |
| Language | Java 11 |
| UI | AndroidX AppCompat, Material Components, ConstraintLayout |
| Architecture extras | Activity / Fragment, Lifecycle ViewModel |
| Build | Gradle + Android Gradle Plugin 8.13 |
| SDK | `minSdk` 26 · `targetSdk` / compile 36 |

Board drawing is a custom `View` (`TableroAjedrez`); game rules and the AI sit in plain Java classes next to it.

## Open in Android Studio

1. Clone this repo and open the project root in Android Studio.
2. Let Gradle sync (JDK 11+).
3. Run the `app` module on an emulator or a phone (API 26+).

That is it — local demo, install, play.
