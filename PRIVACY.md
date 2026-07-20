# Privacy

MSA Bee is an offline arcade game. The application does not include a backend, analytics SDK, advertising SDK, account system, tracking technology, crash-reporting service, or remote telemetry.

## Data processed by the app

The app stores only local data required to continue a session and remember user preferences:

- game status
- score and best score
- bee position and velocity
- pipe positions and scored flags
- deterministic random-generator state

The snapshot remains on the current device or browser profile:

- Android: private `SharedPreferences`
- iOS: app-scoped `NSUserDefaults`
- Desktop: user-scoped `java.util.prefs.Preferences`
- Browser: origin-scoped `localStorage`

No snapshot is transmitted to MSA, ALISCHILLER, or a third party.

## Permissions and sensors

The app does not request network, microphone, camera, location, contacts, notification, Bluetooth, storage, or advertising permissions. Audio files are bundled with the app; the microphone is never accessed.

## Deleting local data

Local game state can be removed by clearing the app data, uninstalling the application, deleting the desktop preference node, or clearing the website storage for the deployed origin.

## Platform declarations

The iOS target includes `PrivacyInfo.xcprivacy`, declares no tracking or collected data, and declares the app-only UserDefaults required-reason API. Android backup and cleartext traffic are disabled.

## Changes

If a future release introduces networking, analytics, accounts, cloud synchronization, or another data-processing feature, this document and the platform privacy declarations must be updated before release.


Reset progress removes only the saved flight and best score. Restoring default settings changes only preferences. Neither action contacts a server.
