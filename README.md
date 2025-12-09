[![Dependency review][dep-badge]][dep-link] [![CodeQL][codeql-badge]][codeql-link] [![Dependabot Updates][dependabot-badge]][dependabot-link]

[dep-badge]: https://github.com/TerryEbdon/Trk21/actions/workflows/dependency-review.yml/badge.svg
[dep-link]: https://github.com/TerryEbdon/Trk21/actions/workflows/dependency-review.yml
[codeql-badge]: https://github.com/TerryEbdon/Trk21/actions/workflows/github-code-scanning/codeql/badge.svg
[codeql-link]: https://github.com/TerryEbdon/Trk21/actions/workflows/github-code-scanning/codeql
[dependabot-badge]: https://github.com/TerryEbdon/Trk21/actions/workflows/dependabot/dependabot-updates/badge.svg
[dependabot-link]: https://github.com/TerryEbdon/Trk21/actions/workflows/dependabot/dependabot-updates

# Trk21

A Groovy version of the 1973 BASIC-PLUS program TREK.BAS, originally written on
a PDP-11 running RSTS/E, with user interaction via a Teletype.
```
Command: C
  Course (1-8.99999): 1
  Warp Factor (0-12): 1
  Repair systems are working on damage to device.S.R..SENSORS, state improved to -1

Command: S
  ---------------
  . . . . . . . .
  . . . . . . . .
  B . . . . . . .
  . . . * * . . *
  . . . * . . E .
  . . . . . . . .
  . . . . . * . .
  . . * . . K . .
  ---------------
  STARDATE:  2104        CONDITION: RED
  QUADRANT: 7 - 3           SECTOR: 7 - 5
    ENERGY:  2960  PHOTON TORPEDOS: 10
  KLINGONS:    15

Command:
```
## Running the game

Currently the simplest way to run the game is via Gradle:
```
gradlew --console=plain run
```

Note the use of `--console=plain`. This is required to prevent Gradle from
overwriting the game's output. Without this option the game would be unplayable.

## App Status

This is a development release, the game is now fully implemented.

| Command                 | Status                                           |
| :---------------------- | :----------------------------------------------- |
| **C**ourse              | <font color='green'>Fully implemented</font>     |
| **L**ong range scan     | <font color='green'>Fully implemented</font>     |
| **S**hort range scan    | <font color='green'>Fully implemented</font>     |
| **D**amage Report       | <font color='green'>Fully implemented</font>     |
| **T**orpedo firing      | <font color='green'>Fully implemented</font>     |
| **P**haser firing       | <font color='green'>Fully implemented</font>     |
| **Q**uit                | <font color='green'>Fully implemented</font>     |


```
Command: P
  Number of units to fire: 100
  Phasers locked in on target. Energy available 2960
2019-02-18 17:40:13.544 ERROR PhaserControl ** Code incomplete **
  Hit from Klingon at sector [8, 6] == 6 - 8

Command:
```
## Background
My original intention was to document the TREK.BAS code. At some point I started
writing it in Groovy and ended up with the code you see today.

This is **not** a direct port of the original game. My intention was to
replicate the game play, not the spaghetti code flow. While documenting the
BASIC-PLUS source I uncovered several bugs. These bugs are *not* present in
Trk21, thus it's not possible to exactly reproduce a TREK game in Trk21. If you
plug the same random number source into both apps the game will still will
rapidly differ, due to a bug in the TREK.BAS ship maneuvering logic.

 -- Terry Ebdon, 18-JAN-2019.
