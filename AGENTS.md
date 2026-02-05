# Project: Fairphone Moments

This document tells AI agents how to work productively and safely on this Android project.

## Project Overview

This is the Fairphone Moments, a custom home screen experience for Fairphone devices. 
It's designed to provide a simple, mindful, and elegant user interface.

**Project quick facts:**

*   **Package name:** `com.fairphone.moments.launcher`
*   **UI:** Jetpack Compose only (no XML views)
*   **Architecture:** MVVM
*   **DI:** Koin
*   **Persistence:** DataStore with Protocol Buffers

## How to make changes safely

*   **UI and navigation:**
    *   Add new screens as Compose functions under `app/src/main/java/com/fairphone/spring/launcher/ui/screen/...`.
    *   For new activities, declare them in `AndroidManifest.xml`.
    *   Use unidirectional data flow (UDF).
*   **ViewModels and DI:**
    *   Create ViewModels and register them in the appropriate `di` module.
    *   Inject dependencies via Koin.
*   **Data and storage:**
    *   Use DataStore with Protocol Buffers for storing data. The proto file is located at `app/src/main/proto/launcher_profile.proto`.
    *   For any data related change, make sure to update the proto files in `app/src/main/proto` and make them backwards compatible.

## Build, run, and test commands

*   **Assemble/Install debug build:**
    *   `./gradlew assembleDebug`
    *   `./gradlew installDebug`
*   **Lint and static checks:**
    *   `./gradlew lintDebug`
*   **Unit tests:**
    *   `./gradlew testDebugUnitTest`
*   **Release builds:**
    *   `./gradlew assembleRelease`

## Do and Don’t for agents

#### Do:

*   Respect Compose-only UI policy.
*   Add new dependencies via version catalogs and `app/build.gradle.kts` consistently.
*   Update Koin modules when introducing new services or repositories.
*   Add or update tests where they already exist; prefer small, fast unit tests for logic.

#### Don’t:

*   Don’t introduce XML layouts or legacy view system code.
*   Don’t commit keys or keystore files. Never hardcode secrets.
*   Don’t break DI graphs; always register new ViewModels/services in the proper Koin module.

## Verification requirements

*   Run `./gradlew check` after code changes
*   Run `./gradlew lint` and `reuse lint` before marking complete
*   For UI changes, verify in emulator (if possible) before committing
