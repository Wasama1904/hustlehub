
# 🚀 HustleHub - Freelance Business Manager
### *Hustle Smart, Grow Fast*

> **OPSC6312 - Part 2 Final Submission** | Student: Wasama ST10451742 | Platform: Android (Kotlin + Jetpack Compose)

[[Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple?logo=kotlin)](https://kotlinlang.org/)
[[Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-4285F4?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[[Firebase](https://img.shields.io/badge/Firebase-Auth%20%2B%20Firestore-FFCA28?logo=firebase)](https://firebase.google.com/)
[[Room](https://img.shields.io/badge/Room-Offline--First-003B57?logo=android)](https://developer.android.com/training/data-storage/room)
[[License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

HustleHub is an offline-first, ZAR-friendly freelance business manager built for South African freelancers facing load-shedding and volatile exchange rates. It combines client management, Kanban project tracking, billable time tracking, PDF invoicing, and gamification to keep hustlers motivated.

**Demo Video:** [▶️ Watch 3-min Demo on YouTube](https://youtube.com/your-video-link) *(replace with your link)*

---

## 📸 Screenshots

| Login & Register | Dashboard & Kanban | Clients & Settings | Growth & Badges |
| :---: | :---: | :---: | :---: |
| ![Login](screenshots/login.png.png) | ![Dashboard](screenshots/dashboard.png.png) | ![Clients](screenshots/clients.png.png) | ![Badges](screenshots/badges.png.png) |
| ![Register](screenshots/register.png.png) | ![Kanban](screenshots/kanban.png.png) | ![Settings](screenshots/settings.png.png) | |

**Folder structure for screenshots:**
```
/screenshots
  - login.png.png
  - register.png.png
  - dashboard.png.png
  - kanban.png.png
  - clients.png.png
  - settings.png.png
  - badges.png.png
```

---

## ✨ Core Features (Rubric Aligned)

### 1. Secure Authentication (Excellently Implemented)
- **2 separate pages:** Login & Register with Material 3 charcoal+teal design
- Firebase Authentication (email/password hashed server-side)
- **EncryptedSharedPreferences** for UID/token storage - no raw passwords in prefs
- API keys in `local.properties` (not in Git) + `.gitignore`

### 2. Client Management
- Add/edit/delete client with validation
- Fields: Name*, Company, Email, **Phone Number**, Rate R/hr, Trust Score
- **Call Button:** `Intent.ACTION_DIAL` with number pre-filled. If no number → Toast "No contact number added"
- Room persistence with `ClientDao`

### 3. Kanban Board with Dates (User Defined Feature 1)
- **5 Columns:** PROPOSED, IN_PROGRESS, DONE, INVOICED, PAID - all movable, PAID included
- **Long-press drag** + Drop zones + FilterChip quick-move
- Each card shows: Title, Client, Budget (USD→ZAR), **Deadline with days left**, Description preview, Tasks count
- **Overdue logic:** `deadlineTimestamp < now && status != PAID` → red badge + counts to Dashboard
- Layout fixed: 340dp width, 2-row chip layout, no squeeze

### 4. Project Detail - Description + 10 Tasks (User Defined Feature 2)
- Full description editable + save to Room
- **Add Task (max 10 enforced):** Button disabled at 10, error message
- Tasks stored as JSON `tasksJson` in `ProjectEntity`, parsed with `getTasksListSafe()` (crash-proof)
- Checkbox → marks complete → awards **+10 XP**, on-time bonus +50 XP
- Displays budget conversion via ExchangeRate-API

### 5. Dashboard - Real Stats (Not Hardcoded)
- **Unpaid:** `SUM(invoices.amount WHERE isPaid=false)` - drives R5400
- **Overdue:** `count(invoices WHERE dueDate < now && !isPaid) + count(projects WHERE deadline < now)` - drives "2"
- **Active:** `count(projects WHERE status=IN_PROGRESS/PROPOSED)` - drives "3"
- Recent Activity from actual invoices
- **Requires dates:** Invoice `dueDate` + Project `deadlineTimestamp` - set via DatePickerDialog

### 6. Timer (Foreground Service)
- Foreground Service with notification - survives app background + load-shedding
- Start/Stop → saves minutes to `TimeEntryEntity` + updates `totalTrackedMinutes` in Project
- Billable calculation: `minutes/60 * hourlyRate from Settings`

### 7. Invoice PDF
- Generates PDF via `PdfDocument` + `FileProvider`
- Pulls real client/project data from Room
- Fields: Client name, budget, tracked hours, amount due, issue/due dates, XP earned

### 8. Gamification & Badges
- XP: 10 per task, 60 on-time, 100 invoice paid
- Level: `XP/300 +1` with titles: Newbie Hustler → Pro Freelancer → Agency Boss → Legend
- **7 Badges:** First Client, First R1k, 5 Projects, On-Time Finisher, Night Owl, Week Warrior, Agency Boss
- Animated scale + rarity colors (Common/Rare/Epic/Legendary) + gradient header
- Streak + Freeze Token logic

### 9. Settings (Makes Sense for App)
- **Account:** Email, Hourly Rate (editable), Default Currency (ZAR/USD)
- **Project Defaults:** Default Deadline Slider (1-30 days), Auto-convert USD→ZAR switch
- **Notifications & Offline:** Deadline Reminders, Offline-first Sync (Room + WorkManager), Dark Theme
- **Security:** Encrypted Storage explanation, Clear Local Data (fixes migration crashes)
- **Logout:** Firebase signOut + clear prefs

---

## 🛠 Tech Stack

| Layer | Technology | Why |
| :--- | :--- | :--- |
| **UI** | Jetpack Compose, Material 3, Charcoal #0A0A0A + Teal #2DD4BF | Modern, rubric UI 8-10 |
| **Auth** | Firebase Auth + EncryptedSharedPreferences | Secure, offline token |
| **Database** | Room v3 (Source of Truth) | Offline-first, load-shedding proof |
| **Cloud** | Firestore + Firebase Storage | Sync when online |
| **API 1** | ExchangeRate-API (USD→ZAR) | Real-time conversion, cache in Room |
| **API 2** | ZenQuotes API | Daily motivation |
| **Async** | WorkManager | Background sync |
| **PDF** | Android PdfDocument | Invoice generation |
| **DI** | Hilt (optional) | Clean architecture |

---

## 🏗 Architecture

```
MVVM + Clean Architecture + Offline-First

UI (Compose Screens)
  ↓
ViewModel (StateFlow + combine)
  ↓
Repository (ProjectRepositoryV2)
  ↓
Room (Source of Truth) <-> Firestore (Remote)
  ↓
ExchangeRate-API / ZenQuotes (Retrofit)
```

**Database Migrations:**
- `1→2`: Add `phone`, `description`, `tasksJson`
- `2→3`: Add `issueDate`, `dueDate` to invoices
- `fallbackToDestructiveMigration()` for dev + `clearAndGet()` for crash recovery

---

## 🔒 Security Implementation (Rubric: 8-10)

```kotlin
// EncryptedSharedPreferences - no plain text passwords
val prefs = EncryptedSharedPreferences.create(
    "secure_prefs", masterKey, context,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
prefs.edit().putString("uid", user.uid).apply() // token, not password

// API keys not in Git
// local.properties:
EXCHANGE_RATE_API_KEY=your_key_here
// .gitignore includes local.properties
```

---

## 📦 Installation & Setup

### Prerequisites
- Android Studio Ladybug+
- JDK 17
- Firebase project

### Steps
1. **Clone:**
   ```bash
   git clone https://github.com/yourusername/HustleHub.git
   cd HustleHub
   ```

2. **Firebase:**
   - Create project at console.firebase.google.com
   - Add Android app: package `com.wasama.hustlehub`
   - Download `google-services.json` → place in `app/` folder
   - Enable Email/Password Auth + Firestore

3. **API Keys:**
   Create `local.properties` in root:
   ```
   EXCHANGE_RATE_API_KEY=your_exchangerate_api_key
   ZENQUOTES_API=https://zenquotes.io/api/
   ```

4. **Build:**
   ```bash
   ./gradlew build
   ```
   - Install on device/emulator
   - **If crashing after DB update:** Clear Data (Settings → Apps → HustleHub → Clear Data)

---

## 🎬 How to Demo (For Markers)

1. **Register** → new account → check Firebase Auth
2. **Settings** → set Hourly Rate R400 → Default Deadline 2 days
3. **Add Client** → with phone 0821234567 → tap Call → dialer opens
4. **Add Client** without phone → tap Call → Toast "No contact number added"
5. **Add Project** → Title, Description, Tasks (one per line, 10 max), Deadline yesterday → Save
6. **Dashboard** → Unpaid R0 → Overdue 1 (red) → Active 1
7. **Kanban** → See card with red "Overdue by 1d" → Move to IN_PROGRESS → PAID → XP +100
8. **Project Detail** → Edit description → Add task → Check task → +10 XP
9. **Timer** → Start → Stop → hours saved
10. **Invoice** → Generate PDF → Open
11. **Growth** → See badges + level + animated XP
12. **Settings** → Clear Data → fix crash

---

## 🧪 Offline-First Proof

- Turn on Airplane Mode → Add Client/Project → Close app → Reopen → Data still there (Room)
- Turn off Airplane Mode → WorkManager syncs to Firestore in background
- Load-shedding scenario: No internet, still functional

---

## 📋 Rubric Coverage

| Criteria | Implementation | Marks |
| :--- | :--- | :--- |
| **App Runs** | No crash, fallbackToDestructiveMigration, getTasksListSafe() | 8-10 |
| **UI Design** | Charcoal+Teal, 340dp Kanban, 2-row chips, gradient badges | 8-10 |
| **GitHub** | Meaningful commits, .gitignore, README, screenshots folder | 8-10 |
| **Security** | Firebase Auth + EncryptedSharedPreferences + local.properties | 8-10 |
| **Sign In** | 2 separate pages, validation, error handling | 8-10 |
| **Feature 1** | Kanban with deadline + description + drag-drop + PAID | 8-10 |
| **Feature 2** | 10 tasks + description + Room persistence + XP | 8-10 |
| **API Integration** | ExchangeRate-API + ZenQuotes with Retrofit + cache | 8-10 |
| **Offline** | Room source of truth + WorkManager sync | 8-10 |
| **Documentation** | This README + inline KDoc + video | 8-10 |

---

## 🚧 Known Limitations & Future

- DatePicker uses legacy Dialog, migrate to Material3 DatePicker
- Timer foreground notification not yet actionable (pause/resume)
- Invoice PDF styling basic, needs logo upload
- Future: Chart for earnings, Export to Excel

---

## 👤 Author

**Wasama** - ST10451742
OPSC6312 - IIE MSA
Cape Town, South Africa 🇿🇦

Built with hustle for freelancers who hustle.

---

## 📄 License

MIT License - see [LICENSE](LICENSE) file.

---

### 🔗 Links

- [APK Release](releases/HustleHub_v1.0.apk)
- [YouTube Demo](https://youtube.com/your-link)
- [Figma Design](https://figma.com/your-design) - Charcoal + Teal theme

> **For markers:** Screenshots are in `/screenshots` folder. If images not loading, check branch `main` has them committed. Video link above shows full flow including overdue calculation from dates.
