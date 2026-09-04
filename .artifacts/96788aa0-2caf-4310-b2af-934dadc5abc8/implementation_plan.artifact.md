# Implementation Plan - Neptunusz Automated Login Client

Build an automated login client for the Neptun university system with secure storage, TOTP generation, and WebView injection.

## User Review Required

> [!IMPORTANT]
> The JavaScript injection relies on HTML element IDs (e.g., `INPUT_USER_ID`, `INPUT_PASS_ID`). These are currently placeholders and MUST be updated with the actual Neptun DOM IDs for the injection to work.

## Proposed Changes

### [Component] Core Logic & Storage

#### [NEW] [SecureStorageManager.kt](file:///C:/Users/szerg/AndroidStudioProjects/Neptunusz/app/src/main/java/com/example/neptunusz/SecureStorageManager.kt)
Implement secure credential storage using `EncryptedSharedPreferences`.
- Methods: `saveCredentials(user, pass, totpSecret)`, `getCredentials()`, `hasCredentials()`, `clearCredentials()`.

#### [NEW] [TotpGenerator.kt](file:///C:/Users/szerg/AndroidStudioProjects/Neptunusz/app/src/main/java/com/example/neptunusz/TotpGenerator.kt)
Implement RFC 6238 TOTP algorithm and a pure Kotlin Base32 decoder.
- `generateCode(secretBase32: String): String`

### [Component] UI & ViewModels

#### [MODIFY] [MainActivity.kt](file:///C:/Users/szerg/AndroidStudioProjects/Neptunusz/app/src/main/java/com/example/neptunusz/MainActivity.kt)
Set up the main entry point and navigation between Settings and WebView screens.

#### [NEW] [MainViewModel.kt](file:///C:/Users/szerg/AndroidStudioProjects/Neptunusz/app/src/main/java/com/example/neptunusz/MainViewModel.kt)
Manage UI state, credential validation, and interactions with `SecureStorageManager`.

#### [NEW] [SettingsScreen.kt](file:///C:/Users/szerg/AndroidStudioProjects/Neptunusz/app/src/main/java/com/example/neptunusz/ui/SettingsScreen.kt)
Compose UI for entering and saving credentials.

#### [NEW] [WebViewScreen.kt](file:///C:/Users/szerg/AndroidStudioProjects/Neptunusz/app/src/main/java/com/example/neptunusz/ui/WebViewScreen.kt)
Compose UI containing the `WebView` with JavaScript injection logic.

## Verification Plan

### Automated Tests
- Unit tests for `TotpGenerator` with known test vectors.
- Unit tests for `SecureStorageManager` (using `Robolectric` if possible, otherwise manual verification).

### Manual Verification
1. Launch app -> Verify it starts on Settings screen (if no credentials).
2. Enter credentials -> Save -> Verify it navigates to WebView.
3. Observe WebView -> Check logcat for JavaScript injection attempts.
4. Use TopAppBar buttons -> Refresh, Back, Settings (to edit).
