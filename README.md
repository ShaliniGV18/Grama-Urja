# Grama-Urja (Smart Village Power Monitor)

Grama-Urja is an Android mobile application designed to provide smart power monitoring and management solutions for villages. It offers real-time insights into power usage, customizable pump timers, and AI-driven power predictions, all wrapped in a modern Jetpack Compose UI with dual-language support (English and Kannada).

## Features

* **Real-time Power Monitoring:** Track power usage and status using Firebase Realtime Database.
* **Pump Timer Functionality:** Customizable timers to manage agricultural or water pumps effectively.
* **AI Power Predictions:** Integrated Gemini AI feature to predict power patterns and provide intelligent insights.
* **Bilingual Support:** Dynamic language switcher between English and Kannada for better accessibility.
* **Interactive Charts:** Visualize power consumption data using MPAndroidChart.
* **User Authentication:** Secure user access powered by Firebase Auth.

## Tech Stack

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose
* **Architecture:** MVVM (Model-View-ViewModel)
* **Dependency Injection:** Dagger Hilt
* **Local Database:** Room Database
* **Preferences:** Preferences DataStore
* **Cloud & Backend:** Firebase (Realtime Database, Authentication, FCM, Analytics)
* **AI Integration:** Google Generative AI (Gemini) SDK
* **Mapping:** Google Maps Compose & Play Services Location
* **Image Loading:** Coil
* **Background Tasks:** WorkManager

## Prerequisites

To build and run this project, you will need:
* Android Studio (Ladybug or newer recommended)
* JDK 17
* A Firebase project configured with Authentication, Realtime Database, FCM, and Analytics.
* A `google-services.json` file placed in the `app/` directory.

## Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   ```

2. **Configure API Keys:**
   Create a `local.properties` file in the root directory if it doesn't exist, and add your API keys:
   ```properties
   GEMINI_API_KEY=your_gemini_api_key_here
   MAPS_API_KEY=your_google_maps_api_key_here
   ```

3. **Firebase Setup:**
   Ensure your `google-services.json` file is correctly placed in the `app/` folder.

4. **Build and Run:**
   Open the project in Android Studio, sync the Gradle files, and run the app on an emulator or physical device.

## Architecture & Libraries

This app follows modern Android development best practices. It utilizes Hilt for robust dependency injection and Room for local data caching. The UI is built entirely with Jetpack Compose, ensuring a reactive and declarative user interface. Kotlin Coroutines and Flow are used extensively for asynchronous programming and managing data streams.
