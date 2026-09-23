<div align="center">

# 🚑 SOSJibon

**A bilingual (Bengali/English) medical emergency first-aid platform for Android**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com)
[![Gemini API](https://img.shields.io/badge/AI-Gemini%20%2B%20TFLite-8E75B2?logo=googlegemini&logoColor=white)](https://ai.google.dev)
[![Platform](https://img.shields.io/badge/Platform-Android%2015%2B-3DDC84?logo=android&logoColor=white)](https://developer.android.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

</div>

---

## 📖 Overview

SOSJibon combines live SOS broadcasting, an admin oversight system, an on-device + cloud AI assistant, and a bilingual first-aid knowledge base into a single emergency response platform. It targets real-world emergency response with content covering 26 medical conditions in both Bengali and English.

**Repository:** [github.com/rehnuma14/SOSJibon](https://github.com/rehnuma14/SOSJibon.git)

## 📑 Table of Contents

- [Architecture](#-architecture)
- [Core Modules](#-core-modules)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [Team](#-team)
- [Roadmap](#-roadmap)
- [License](#-license)

---

## 🏗 Architecture

```
                    ┌────────────────────────────────────────┐
                    │          SOSJibon Android App          │
                    └───────────────────┬────────────────────┘
                                        │
      ┌──────────────────┬──────────────┼──────────────┬──────────────────┐
      ▼                  ▼              ▼              ▼                  ▼
┌───────────┐    ┌──────────────┐ ┌───────────┐ ┌──────────────┐ ┌────────────────┐
│ Emergency │    │  Admin Panel │ │  AI Engine│ │ Health Vault │ │  E-Library &   │
│ SOS & GPS │    │  & Audit     │ │ (TFLite + │ │  & Donations │ │  Community     │
│ Broadcast │    │  Directory   │ │  Gemini)  │ │  & Contacts  │ │  Support Hub   │
└───────────┘    └──────────────┘ └───────────┘ └──────────────┘ └────────────────┘
```

---

## 🧩 Core Modules

### 🚨 Emergency SOS & Live GPS Broadcast Engine
`EmergencySosScreen.kt` · `EmergencyLocationHelper.kt`
- Safety permit gate: choose **Activate Live SOS** or **Explore Mode**
- Explore Mode — browse emergency numbers, first-aid steps, and CPR protocols without broadcasting location
- App-wide active SOS banner (`AppTopActiveSosBanner`) with a **Mark Safe** action
- Two-way real-time resolution — alerts sync across `/active_sos_alerts` and `/sos_history_records` and clear from the live map instantly

### 👑 Admin Control Panel & Moderator System
`ui/admin`
- Master admin authentication
- Live GPS emergency map, system statistics, and pending community stories (`AdminDashboardScreen`)
- User directory with verification badges, search, and one-click verification/role controls (`AdminPanelScreen`)
- Real-time admin permit requests from `DeveloperScreen`
- SOS history audit with status filters, one-tap call/map actions, and log deletion (`AdminSosHistoryScreen`)

### 🤖 AI Engine — TFLite + Gemini
`ai/` · `ui/ai/`
- On-device TensorFlow Lite intent classifier (`sos_jibon_intent_classifier_final.tflite`) for automatic in-app navigation
- Floating draggable AI chatbot (`FloatingAiBubble`, `AiSheet`) for 24/7 assistance
- Gemini-powered Pro Tip Center (`ProTipScreen`) with voice input (STT) and medical PDF analysis (`GeminiFileManager`)

### 🩺 Health Vault, Blood Donation & Emergency Contacts
- Blood donation tracker with gender-specific eligibility rules (Male: 90 days, Female: 120 days) and full donation history log
- Encrypted local Room database with two-way Firestore sync for vitals, documents, and medications (`VaultScreen`, `VaultViewModel`)
- Emergency contacts stored locally and in Firestore, with one-tap dialing (`EmergencyContactsScreen`)

### 📚 First Aid E-Library, Triage & Community Hub
- Bilingual first-aid guides — CPR, choking, bleeding, burns, seizures, animal bites
- Interactive symptom triage wizard (`AssessmentScreen`)
- Community healthcare stories with admin approval workflow (`CommunityStoriesScreen`)

### 🔒 Security & Infrastructure
- Persistent login sessions across app restarts
- Google Sign-In via Play Services Auth (`GoogleAuthProvider`)
- Android 15+ (16 KB page size) compliant

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language / UI | Kotlin, Jetpack Compose |
| AI | TensorFlow Lite (on-device), Gemini API (cloud) |
| Backend / Data | Firebase Firestore, Room (local DB), SharedPreferences |
| Auth | Google Sign-In (Play Services) |
| Location | FusedLocationProviderClient, Geocoder |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio (latest stable)
- JDK 17+
- A Firebase project with `google-services.json`
- A Gemini API key

### Installation

```bash
git clone https://github.com/rehnuma14/SOSJibon.git
```

1. Open the project in **Android Studio** and let Gradle sync.
2. Add your `google-services.json` to the `app/` module.
3. Add your Gemini API key to `local.properties`:
   ```
   GEMINI_API_KEY=your_key_here
   ```
4. Run on a device or emulator with Google Play Services installed.

---

## 👥 Team

<table>
  <tr>
    <td align="center" width="25%">
      <img src="assets/team/rehnuma.jpg" width="110" height="110" style="border-radius:50%; object-fit:cover;" alt="Rehnuma Ahmed"/><br/>
      <b>Rehnuma Ahmed</b><br/>
      <sub>Team Leader</sub><br/>
      <a href="https://www.linkedin.com/in/reh-nu-ma"><img src="https://img.shields.io/badge/LinkedIn-0A66C2?logo=linkedin&logoColor=white" alt="LinkedIn"/></a>
    </td>
    <td align="center" width="25%">
      <img src="assets/team/jobaida.jpg" width="110" height="110" style="border-radius:50%; object-fit:cover;" alt="Jobaida Khanam"/><br/>
      <b>Jobaida Khanam</b><br/>
      <sub>Team Member</sub><br/>
      <a href="https://www.linkedin.com/in/jobaida-khanam-juli-447057415"><img src="https://img.shields.io/badge/LinkedIn-0A66C2?logo=linkedin&logoColor=white" alt="LinkedIn"/></a>
    </td>
  </tr>
  <tr>
    <td align="center" width="25%">
      <img src="assets/team/aditya.jpg" width="110" height="110" style="border-radius:50%; object-fit:cover;" alt="Aditya Babu Tanmoy"/><br/>
      <b>Aditya Babu Tanmoy</b><br/>
      <sub>Team Member</sub><br/>
      <a href="https://www.linkedin.com/in/aditya-babu-tanmoy-2025abt"><img src="https://img.shields.io/badge/LinkedIn-0A66C2?logo=linkedin&logoColor=white" alt="LinkedIn"/></a>
    </td>
    <td align="center" width="25%">
      <img src="assets/team/arman.jpg" width="110" height="110" style="border-radius:50%; object-fit:cover;" alt="Kazi Arman Samir"/><br/>
      <b>Kazi Arman Samir</b><br/>
      <sub>Team Member</sub><br/>
      <a href="https://www.linkedin.com/in/arman-samir-22a2213b6"><img src="https://img.shields.io/badge/LinkedIn-0A66C2?logo=linkedin&logoColor=white" alt="LinkedIn"/></a>
    </td>
  </tr>
</table>

---

## 🗺 Roadmap

- [ ] Expert review of Bengali translations and medical content
- [ ] Expand condition coverage beyond the current 26 conditions
- [ ] Multi-admin role auditing

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">
<sub>Built with care for real-world emergency response.</sub>
</div>
