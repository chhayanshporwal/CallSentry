<div align="center">
  <h1>🛡️ SafeGuard (Call Sentry)</h1>
  <p><strong>Intelligent Privacy & Zero-Trust Communication Control System for Android</strong></p>
</div>

> **⚠️ Notice for Users & Testers:** Phone number registration and login functionality are currently unavailable and will be added in upcoming updates! Stay tuned!

## 📖 Overview
In an era of rampant unsolicited marketing calls ("robocalls"), spam SMS, and potential harassment, managing personal communication channels has become increasingly difficult. **SafeGuard** is a native Android application designed to return control to the user.

Unlike traditional call blockers that rely on crowd-sourced blacklists (which are often outdated or easily bypassed by spoofed numbers), SafeGuard operates on a strict **Zero-Trust Whitelist Model**. This means communication is only permitted from known, trusted contacts, while everything else is automatically blocked silently.

## 🚀 Why Use SafeGuard?

### The Problem
- **Notification Fatigue:** Users are bombarded with spam calls and texts, disrupting focus and causing anxiety.
- **Ineffective Blacklists:** Blocking after the fact does not prevent initial harassment. Spam callers frequently change numbers.
- **Safety Concerns:** Traditional "Do Not Disturb" modes filter out critical emergency calls from unknown numbers (e.g., hospitals, or family using a different phone).

### The Solution
SafeGuard proposes a shift from reactive blocking to proactive protection. By combining strict privacy controls with a safety net for genuine emergencies, it effectively addresses communication spam without compromising user safety.

## 🎯 Key Features
- ✅ **Strict Whitelist Enforcement:** By default, all incoming calls and SMS are blocked. Only explicitly whitelisted contacts (imported from your phonebook or added manually) are allowed through.
- 🚨 **Emergency Breakthrough Algorithm:** We recognize that emergencies happen. If a non-whitelisted number attempts to call 3 times within 5 minutes, it is classified as a potential emergency. The 4th attempt is allowed to ring through, ensuring critical connections are made.
- 🔕 **Silent Activity Logging:** Blocked calls and messages are silently logged in a local database for review at your convenience, without intrusive notifications at the time of the event.
- 🔒 **Privacy-First (On-Device):** All processing happens locally on your device. No contact lists or call logs are uploaded to external servers.
- 📊 **Interactive Dashboard:** Real-time statistics showing calls and SMS blocked today, with quick toggles to manage blocking features.
- 🔐 **PIN Protection:** Secure the app settings so only authorized users can modify the whitelist.

## 👥 Use Cases (Who is this for?)
1. **Elderly Protection ("Protecting Grandma"):** Keep vulnerable users safe from scam calls claiming to be from banks or authorities. Setup the whitelist once and protect them from predators.
2. **Child Safety ("Parental Control"):** Restrict a child's phone so they can only receive communications from approved family and friends.
3. **Professional Focus Mode:** Block out all distractions and unapproved contacts during critical work or deep-focus sessions.

## 🛠️ How It Was Made (Technical Implementation)
SafeGuard was built using modern Android development practices, ensuring high performance, scalability, and maintainability.

### Tech Stack
- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose + Material Design 3 for a declarative, reactive, and beautiful interface.
- **Architecture:** MVVM (Model-View-ViewModel) guided by Clean Architecture principles (Presentation, Domain, and Data layers).
- **Asynchronous Operations:** Kotlin Coroutines & Flow.
- **Dependency Injection:** Hilt / Dagger.
- **Local Database:** Room (for structured data like logs and contacts) & DataStore (for preferences).

### Core System Integration
- **Call Interception:** Utilizes Android's `CallScreeningService` API to intercept calls before they ring the device. It checks the incoming number against the local Room database in under 50ms to decide whether to reject or allow the call.
- **SMS Interception:** Employs a high-priority `BroadcastReceiver` listening to `Telephony.Sms.Intents.SMS_RECEIVED_ACTION`. If the sender is not whitelisted, the broadcast is aborted, hiding the SMS from the default messaging app and storing it safely in the local Blocked Log.

## 💻 Building & Installation

### Requirements
- Android Studio (Latest Version)
- Minimum SDK: Android 8.0 (API 26)
- Target SDK: Android 14 (API 34)

### Steps
1. Clone this repository.
2. Open the project in Android Studio.
3. Build the project using Gradle:
   ```bash
   ./gradlew assembleDebug
   ```
4. Install the generated APK on your device.
5. **Note on Permissions:** On first launch, the app will request necessary permissions (Call Log, Phone State, Contacts, SMS, and Notification). Please grant these for the app to function properly. Depending on your device manufacturer (Xiaomi, Samsung, OnePlus), you may need to disable battery optimization for SafeGuard to allow it to run reliably in the background.

## 📄 License
This project is licensed under the MIT License.
