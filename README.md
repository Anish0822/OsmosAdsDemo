# OsmosAdsDemo
Demo Android application integrating Osmos SDK for display ad fetching, rendering, impression tracking, and click tracking.

The application demonstrates:
- SDK initialization
- AU-based display ad fetching
- Manual banner rendering
- Impression tracking
- Click tracking
- Error handling and fallback UI

---

## Tech Stack
- Kotlin
- Android SDK
- View Binding
- Kotlin Coroutines
- Glide
- Osmos Android SDK

---

## Project Structure

```
app/
├── src/main/java/com/example/osmosadsdemo
│   ├── ui/
│   │   └── MainActivity.kt
│   └── utils/
│       └── App.kt
├── src/main/res/layout/
│   └── activity_main.xml
```

---

## SDK Configuration

SDK is initialized in the Application class using:

- clientId = 10088010
- productAdsHost = demo.o-s.io
- displayAdsHost = demo-ba.o-s.io

The application uses the global SDK instance:

```kotlin
OsmosSDK.clientId("10088010")
    .productAdsHost("demo.o-s.io")
    .displayAdsHost("demo-ba.o-s.io")
    .buildGlobalInstance()
```

---

## How Ad Fetching Works

When the user taps the **Load Ad** button:

1. Loading state is shown.
2. Display ads are fetched using:

- cliUbid = "Any"
- pageType = "demo_page"
- adUnit = "banner_ads"

The application parses:

```
ads.banner_ads[0]
```

and extracts:

- elements.value (banner image URL)
- click_tracking_url
- uclid
- width
- height

The banner image is then rendered manually using an ImageView.

---

## Banner Rendering

Banner ads are rendered manually using Glide.

The aspect ratio is maintained using the width and height returned by the API response:

```
ratio = bannerHeight / bannerWidth
```

This ensures the banner displays correctly on different screen sizes.

---

## Impression Tracking (50% Visibility)

Impression tracking is implemented according to the assignment requirement.

Steps:

1. Banner becomes visible on screen.
2. Visible area and total area of the ImageView are calculated.
3. Impression is fired only when at least 50% of the banner is visible.
4. Impression is sent only once per ad using an `impressionSent` flag.

SDK method used:

```kotlin
registerAdImpressionEvent()
```

---

## Click Tracking

When the user taps the banner:

1. `registerAdClickEvent()` is called.
2. The click tracking URL is opened using an `ACTION_VIEW` intent.

Click events are logged in Logcat.

---

## Event Logging

The application logs:

- Ad Loaded
- Ad Failed
- Impression Fired
- Click Fired

Logs are available in Logcat using the tag:

```
OSMOS
```

---

## Error Handling

The application handles:

- Null SDK response
- Invalid response structure
- Empty ad list
- Missing image URL
- Network failures
- Unexpected exceptions

Fallback message:

```
Ad not available
```

The application avoids crashes and gracefully handles failures.

---

## Screen Rotation Handling

Banner information is preserved during configuration changes.

The following values are saved and restored:

- imageUrl
- clickUrl
- uclid
- bannerWidth
- bannerHeight
- impressionSent

This prevents unnecessary re-fetching and duplicate impression events.

---

## Architecture

The application follows a simple modular structure:

```
UI Layer (MainActivity)
        ↓
Osmos SDK
        ↓
Ad Response Parsing
        ↓
Banner Rendering
        ↓
Impression & Click Tracking
```

---

## Assumptions Made

- The demo API response returns one banner ad and therefore only the first item (`banner_ads[0]`) is rendered.
- The provided response did not include `elements.destination_url`. Therefore, `click_tracking_url` is used for navigation in the demo.
- Only one banner is displayed at a time.

---

## Challenges Faced

- Understanding the nested SDK response structure.
- Maintaining banner aspect ratio dynamically.
- Ensuring impressions are fired only once.
- Handling timing issues where the banner dimensions were not immediately available after image loading.

---

## How to Run the Demo

1. Clone the repository.
2. Open the project in Android Studio.
3. Sync Gradle dependencies.
4. Run the application on an emulator or physical device.
5. Tap **Load Ad**.
6. Observe:
   - Banner rendering
   - Impression event logging
   - Click event logging
   - Error handling behavior

---

## Demo

A short demo video showing:
- Ad loading
- Banner rendering
- Impression firing
- Click handling
- Error scenarios

