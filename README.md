This is a Kotlin Multiplatform project targeting Android, iOS, and Desktop (JVM).

- [/composeApp](./composeApp/src) contains the shared Compose Multiplatform code.
  - [commonMain](./composeApp/src/commonMain/kotlin) is shared across all targets.
  - Platform folders (for example [iosMain](./composeApp/src/iosMain/kotlin) and [jvmMain](./composeApp/src/jvmMain/kotlin)) contain target-specific code.

- [/iosApp](./iosApp/iosApp) contains the iOS entry point project used by Xcode.

### Build and Run Android Application

Use your IDE run configuration or run from terminal:
- macOS/Linux
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```
- Windows
  ```shell
  .\gradlew.bat :composeApp:assembleDebug
  ```

### Build and Run Desktop (JVM) Application

Desktop is opt-in. Enable it with a Gradle property:
- macOS/Linux
  ```shell
  ./gradlew :composeApp:run -Pllego.enableDesktop=true
  ```
- Windows
  ```shell
  .\gradlew.bat :composeApp:run -Pllego.enableDesktop=true
  ```

To enable Compose Hot Reload in desktop development, also add:
- `-Pllego.desktopDev=true`

### Build and Run iOS Application

Use your IDE run configuration or open [/iosApp](./iosApp) in Xcode and run from there.

---

Learn more about Kotlin Multiplatform:
https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html
