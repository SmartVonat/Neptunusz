# Implement Light/Dark Mode Compatibility

This plan outlines the steps to make the Neptunusz app fully compatible with both light and dark modes, including automatic switching, web content darkening, and adaptive app icons.

## User Action Required

You will need to place your icon files into the project after I create the necessary directories. I will provide the exact paths and filenames below.

> [!IMPORTANT]
> For the app icons to switch automatically, we will use the `drawable` and `drawable-night` folders. Ensure your icons are either XML vectors or PNG/WebP files.

## Proposed Changes

### Theme & Styling

#### [MODIFY] [Theme.kt](file:///C:/Users/szerg/AndroidStudioProjects/Neptunusz/app/src/main/java/com/example/neptunusz/ui/theme/Theme.kt)
- Refine the `LightColorScheme` and `DarkColorScheme` to ensure they provide a consistent look.

#### [MODIFY] [themes.xml](file:///C:/Users/szerg/AndroidStudioProjects/Neptunusz/app/src/main/res/values/themes.xml)
- Change the parent theme to `Theme.Material3.DayNight.NoActionBar` to allow the system (splash screen, etc.) to respect dark mode settings.

#### [NEW] [colors.xml (night)](file:///C:/Users/szerg/AndroidStudioProjects/Neptunusz/app/src/main/res/values-night/colors.xml)
- Define dark-mode specific colors, specifically the app icon background.

### WebView Enhancement

#### [MODIFY] [WebViewScreen.kt](file:///C:/Users/szerg/AndroidStudioProjects/Neptunusz/app/src/main/java/com/example/neptunusz/ui/WebViewScreen.kt)
- Update `configureWebView` to enable algorithmic darkening for web content when the system is in dark mode. This ensures the Neptun website itself appears dark.

### App Icons Instructions

I will set up the structure so you only need to drop the files:

1.  **Light Mode Icon**: Place in `app/src/main/res/drawable/ic_launcher_foreground.xml` (or .png).
2.  **Dark Mode Icon**: Place in `app/src/main/res/drawable-night/ic_launcher_foreground.xml` (or .png).
3.  **Icon Backgrounds**:
    - Light: Edit `app/src/main/res/values/ic_launcher_background.xml`.
    - Dark: I will create `app/src/main/res/values-night/ic_launcher_background.xml`.

## Verification Plan

### Manual Verification
- Deploy the app to an emulator or device.
- Toggle the System Dark Mode setting and verify:
    - The Compose UI (TopBar, Settings) changes colors.
    - The WebView content darkens automatically.
    - The app icon on the home screen matches the system theme (requires a launcher that supports themed icons or adaptive icons).
