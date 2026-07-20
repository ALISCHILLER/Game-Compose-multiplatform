# Game Settings

MSA Bee stores settings locally on the current device. No setting is uploaded or shared.

## Audio

- **Game sound** is the master mute control.
- **Background music** controls the looping flight music.
- **Sound effects** controls jump, score, and game-over cues.
- Music and effects have independent volume sliders from 0 to 100 percent.
- Volume changes are applied immediately and persisted before the UI returns.

## Comfort

- **Reduce motion** disables decorative parallax, wing cycling, trails, score pulses, and rising feedback. Essential game physics still move normally.
- A system Reduced Motion preference always takes priority over the in-app switch.
- **Gameplay hints** controls the short input reminder shown at the beginning of a flight.

## Local data

- **Restore default settings** resets preferences without deleting the best score.
- **Reset progress** requires confirmation and deletes the saved flight and best score on the current device.
- Settings and progress use separate storage keys, so resetting either one does not accidentally remove the other.

## Platform storage

| Target | Storage |
|---|---|
| Android | SharedPreferences |
| iOS | NSUserDefaults |
| Desktop | java.util.prefs |
| JS / Wasm | localStorage |

Settings use a bounded, versioned, checksum-protected codec. Invalid data is replaced with safe defaults.

## Verification

```bash
./scripts/run_common_pure_tests.sh
python scripts/verify_project.py
python scripts/verify_ui.py
```

Full UI interaction and platform audio verification still require the applicable Gradle and device/browser toolchains.

## Author

Developed and maintained by  
[ALISCHILLER](https://github.com/ALISCHILLER) — **MSA**

Email: [solimaniali90@gmail.com](mailto:solimaniali90@gmail.com)
