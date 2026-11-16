# BenSuet Stay
**VIDEO PRESENTATION**
**Link to Google drive** https://drive.google.com/file/d/1dhzIpdwjzfjv32CN37wXeFUEgEGsneGD/view?usp=sharing

**BenSuet Stay** is an Android app that connects South African students with safe, verified accommodation near their campus. It matches each student to residences that accept their funding (NSFAS or self-funded/bursary), in any of the country's 11 written official languages. It also keeps working when data runs out.

Built by **Mpilonhle Memela** ([Mansuet Dev](https://mansuetdev.tech)) as the Portfolio of Evidence for **OPSC6312** at Rosebank College (The IIE).

---

## Contents

1. [Purpose of the app](#1-purpose-of-the-app)
2. [Project Structure](#2-project-structure)
3. [Design considerations](#3-design-considerations)
4. [GitHub and GitHub Actions](#4-github-and-github-actions)
5. [Release notes](#5-release-notes)
6. [Getting started](#6-getting-started)
7. [PoE requirements map](#7-poe-requirements-map)
8. [Demo video](#8-demo-video)
9. [Use of AI tools](#9-use-of-ai-tools)

---

## 1. Purpose of the app

**The problem.** Every year thousands of students look for a place to stay near campus, usually through social media posts and word of mouth. Their needs differ:
- **NSFAS students** can only use NSFAS-accredited residences.
- **Self-funded and bursary students** compare prices and distance.
- **Everyone** is exposed to fake listings, unsafe buildings and landlords who never reply.

**The solution.** BenSuet Stay puts verified listings in one place:
- Listings are created by an administrator and assigned to vetted landlords.
- Each student sees what fits their funding type and campus.
- Students can book a viewing, apply for a room and follow every step until a decision.

| Role | What they do in the app |
| --- | --- |
| **Student** | Registers (email or Google). Browses residences filtered by funding and campus, with distance to campus. Books viewings and applies for rooms (NSFAS confirmation or self-funded registration). Tracks applications and landlord feedback, and gets notified of every decision. |
| **Landlord** | Account created by an admin. Sees only their own listings, and confirms or declines viewing requests with a reason. Reviews applications and approves or declines them; room availability updates automatically. |
| **Admin** | Creates and edits listings (rooms, prices, amenities, photos). Creates landlord accounts, sends password resets and removes accounts. Sees platform totals. |

---

## 2. Project Structure

```
app/src/main/java/com/mansuetdev/bensuetstay/
├── BenSuetStayApp.kt, BaseActivity.kt      # App start-up; edge-to-edge insets + role guard for every screen
├── Session.kt                              # Signed-in user + role, Navigator
├── *Repository.kt                          # Firestore access (Listing, Student, Landlord, Admin, Booking, Application)
├── MediaUploader.kt, ProfilePhoto.kt       # Cloud Storage uploads (profile pictures only)
├── ListingLogic.kt, Validators.kt          # Pure logic (filters, prices, SA ID/phone): unit tested
├── Welcome / Login / Register / ForgotPassword
├── Home / ListingDetail / BookViewing / ConfirmNsfas / SelfFundedRegistration
├── StudentBookings / StudentApplications / StudentProfile / EditStudentProfile
├── LandlordDashboard / LandlordBookings / LandlordApplications / ApplicationDetail / LandlordProfile
└── AdminDashboard / AdminListings / AddEditListing / AdminLandlords / AddLandlord
firestore.rules, storage.rules, firebase.json   # Security rules (repo root)
```

---

## 3. Design considerations

### 3.1 Architecture

The app is written in Kotlin. Firebase is the backend, so there is no custom server to host or pay for.

```mermaid
flowchart LR
  subgraph App["Android app (Kotlin)"]
    UI["Activities + ViewBinding<br/>Material 3 UI"]
    Logic["ListingLogic + Validators<br/>pure Kotlin, unit-tested"]
    Repos["Repositories<br/>Listing, Booking, Application,<br/>Student, Landlord, Admin"]
    Session["Session + Navigator<br/>who is signed in, which role"]
    Offline["SyncManager<br/>SQLite outbox"]
    Notif["NotificationCenter<br/>+ FCM service"]
  end
  UI --> Logic
  UI --> Repos
  UI --> Session
  UI --> Offline
  Offline --> Repos
  Repos --> FS[("Cloud Firestore<br/>Johannesburg region")]
  Session --> Auth["Firebase Authentication<br/>email + Google"]
  Notif --> FS
  FCM["Firebase Cloud Messaging"] --> Notif
  UI --> Geo["OpenStreetMap Nominatim<br/>REST API"]
```

- **Screens talk to repositories, never to Firebase directly.** Each repository owns one Firestore collection: its queries, its field names and how documents become Kotlin objects.
- **Business rules are pure Kotlin.** Filtering, price parsing, distance, SA ID and phone validation sit in `ListingLogic` and `Validators`, with no Android code, so plain JUnit tests cover them on every push.
- **Lifecycle-safe live data.** Lists use Firestore listeners tied to the screen's lifecycle and removed in `onStop`, so nothing leaks or updates a closed screen.
- **One shared screen base.** `BaseActivity` handles Android 15 edge-to-edge insets and sends users through the Welcome screen if Android restarted the app without their session.

### 3.2 Data model

| Collection | Document id | Written by | Read by |
| --- | --- | --- | --- |
| `students` | Auth uid | the student | that student, admins |
| `landlords` | Auth uid | admins (landlord: own photo and password flag) | that landlord, admins |
| `admins` | Auth uid | Firebase console | that admin |
| `accommodations` | auto | admins (assigned landlord: room spaces only) | everyone, including guests |
| `bookings` | auto | student creates, landlord responds | that student, that landlord, admins |
| `applications` | auto | student creates, landlord decides | that student, that landlord, admins |
| `notifications` | auto | whoever performed the action | the recipient only |

Records are linked by **ids** (`studentId`, `listingId`, `landlordId`), never by names. Renaming a student or a residence therefore can't break a booking.

### 3.3 Security and privacy

- **One sign-in system for all three roles.** Students, landlords and admins all use Firebase Authentication. Firebase stores passwords as salted scrypt hashes; the app and the database never see them. A user's role is decided by which collection holds their uid.
- **Rules enforce access, not just the app.** `firestore.rules` lets a student read only their own records, a landlord only their own listings' bookings and applications, and only admins change listings. Every query is scoped the same way, so a phone downloads only its user's data.
- **No double-booking.** Approving an application takes a space off the room inside a Firestore **transaction**. Each booking or application is written in the same **batch** as its notification, so both are saved or neither is.
- **POPIA.** The app collects the minimum data, shows an in-app privacy policy, and offers *Delete my account* (re-confirming identity first). SA ID numbers are checked for a valid birth date and Luhn checksum before saving.

### 3.4 Funding-aware matching

- A residence with NSFAS and self-funded rooms appears as **two cards**, each showing only its own rooms and prices.
- The homepage combines three independent filters: **funding type**, **near my campus** and **search**. Signed-in students start with their own funding type and campus, and can change these defaults in Settings.
- Rent is stored as a number, so "From R2 500/mo" really is the cheapest room.

### 3.5 Eleven languages

- Every on-screen word (457 strings) lives in `res/values-<code>/strings.xml`: `af`, `zu`, `xh`, `b+nso` (Sepedi has no two-letter code), `tn`, `st`, `ts`, `ss`, `ve`, `nr`, with English as the default.
- The switcher uses AppCompat's per-app language API, so the choice is remembered. On Android 13+ it also appears in the phone's own app settings.
- **Stored data stays language-neutral.** Room types, amenities and funding are saved in English and translated only on screen, so filters and approvals work the same in every language.

### 3.6 Offline-first actions

Students often run out of data, so booking a viewing and applying for a room work offline via a SQLite outbox and Android network callbacks. Listings already opened stay visible offline through Firestore's on-device cache.

### 3.7 Real-time notifications

- **Event alerts.** Each action writes a small `notifications` document. Signed-in users listen for their unread alerts and get a system notification instantly.
- **Push announcements.** Users are subscribed to FCM topics (`all`, `students`, `landlords`, `admins`) for broadcast messages.

### 3.8 REST API: distance to campus

The app calls the **OpenStreetMap Nominatim** geocoding API to turn listing and campus addresses into coordinates. Haversine formula provides straight-line distance.

### 3.10 Constraints and trade-offs

| Constraint | Decision |
| --- | --- |
| Firebase **Spark (free) plan**: no Cloud Storage | Listing photos are linked from the admin's device. Application documents are recorded by file name. |
| Spark plan: no Cloud Functions | Event alerts are delivered in real time while the app is running. |
| Nominatim is a shared free service | Results are cached and requests limited to one per second. |

### 3.11 Testing

- **Unit tests:** `ListingLogicTest` and `ValidatorsTest` cover filtering, campus matching, price parsing, and SA ID/phone validation.
- **Tests command:** `./gradlew testDebugUnitTest`

---
#MY PART 3 CHANGES

## Settings depth 
Every Settings row now does real work, and the new features are controlled from Settings.
- **Sign-in & security:** change password for students. Google accounts are told their password is managed by Google.
- **Notifications:** switches for real-time booking/application alerts and for push announcements (FCM topics), plus a shortcut to the phone's notification settings that shows whether they're allowed.
- **Home screen defaults:** start with "Near my campus" and/or your own funding type.
- **Offline & data:** see how many items are waiting to sync and send them now; clear cached locations.
- **Help:** real FAQ pages (student and landlord versions), Contact support (email), and About (with OpenStreetMap attribution).
- **Privacy:**
  - An in-app POPIA privacy policy.
  - **Delete my account**, after re-confirming the password or Google account. This deletes the profile and sign-in, and is also a Google Play requirement.
  - **Landlords/admins:** Help Center opens the landlord FAQ, and Personal Information has "Request a change".
- **Rules:** `firestore.rules` now lets students delete their own profile (republish it).
- 57 new strings, translated into all 10 other languages.

## SSO, REST API, offline sync, notifications
- **Google single sign-on.** Credential Manager + Firebase Auth. Existing users of any role land on their home screen; new Google users complete a short student profile (no password).
- **REST API (OpenStreetMap Nominatim).** Listings are geocoded when saved, and students see the distance to their campus. Results are cached and limited to 1 request per second.
- **Offline mode with sync.** A SQLite outbox holds viewing bookings and applications made offline. They sync automatically when the network returns, and duplicate applications are re-checked on the server. Lists show "Waiting to sync", the homepage notes when it's showing saved (cached) listings, and a notification confirms the sync.
- **Notifications:**
  - FCM push to the role topics (`all`, `students`, `landlords`, `admins`).
  - Real-time alerts written atomically with each booking or application action, shown in the recipient's own app language.
  - The Android 13+ notification permission is requested, and notifications have their own channel.
- **New Firestore rules** for `notifications`. 29 new strings, translated into all 10 other languages.

## Multi-language 
- **All 11 written SA languages:** every user-facing string (376) moved into `strings.xml`, with full translations for Afrikaans, isiZulu, isiXhosa, Sepedi, Setswana, Sesotho, Xitsonga, siSwati, Tshivenḓa and isiNdebele.
- **Language picker** on the Welcome screen and in Settings for students, landlords and admins, using AppCompat per-app locales (`locales_config.xml` and `autoStoreLocales`).
- **Stored data stays in English.** Room types, amenities and funding labels are translated only on screen, so Firestore data and approvals don't depend on the language.
- **Translatable prices.** Price labels moved out of `ListingLogic` (now numbers only), so they translate ("R2 500/mo" → "R2 500/ngenyanga").
- **Fake language switch removed.** It had no effect and has been replaced by the real one.

## Security & data overhaul 

> **Spark plan:** listing photos are still linked from the admin's phone, and application documents are recorded by name only, as before. Nothing in the app needs Cloud Storage except profile pictures.

### Security
- **All three roles now use Firebase Authentication.** Admin and landlord passwords used to be stored as plain text in Firestore and downloaded to every phone. Passwords now exist only in Firebase Auth.
- **Removed the `StudentRepository.login()` backdoor.** It logged a user in with only an email or phone when the password was wrong.
- **Removed hard-coded passwords** (`password123`, `Stay@2025`). Admins now send landlords a real reset email instead.
- **Added `firestore.rules`** (and `storage.rules` for when Storage is enabled). Each user can only read their own records. Landlords only see their own listings' bookings and applications. Only admins can change listings or accounts.
- **Scoped queries.** Phones no longer download every student, application (with SA ID numbers) and booking. Each screen queries only its user's data.
- **Admins create landlords without being logged out.** This uses a secondary FirebaseApp, because `createUserWithEmailAndPassword` normally switches the signed-in user.

### Bugs fixed
- **Self-funded listing cards couldn't be opened.** The card id `"<id>_SELF_OR_BURSARY"` was cut at the last `_`, which gave `"<id>_SELF_OR"`.
- **Self-funded students weren't auto-filtered.** The code compared `"SELF_OR_BURSARY"` with `"SELF_FUNDED"`.
- **"From R…" showed the wrong cheapest price.** Prices were compared as text, so "R10 000" sorted before "R2 500". Rent is now stored as a number (`priceAmount`).
- **Document uploads were simulated** with a timer and fixed filenames. Students now pick their actual files and the real file names are recorded. Files aren't uploaded, so the project can stay on the free Spark plan.
- **The first admin/landlord login attempt failed.** Lazily-started repositories were still empty when Log In was tapped.
- **My Bookings showed empty on first open, and landlord dashboard counts showed 0.** Both had the same lazy-loading problem.
- **Landlords saw every booking and application on the platform.**
- **Bookings and applications were linked by names.** Renaming a student or listing broke the links, and two students with the same name saw each other's bookings. Records now store `studentId`, `listingId` and `landlordId`.
- **Students couldn't see application results.** There is a new **My Applications** screen with status and landlord feedback.
- **Approving an application didn't reduce room availability.** It now does, inside a transaction so two approvals can't take the last bed.
- **Students could apply twice for the same residence.**
- **Logins didn't survive Android killing the app.** Sessions are now restored from Firebase Auth on launch.
- **Possible RecyclerView crash.** Adapters received the repositories' live lists. They now get copies and update through listeners.
- **Listener leak in Landlord Bookings.** It added a listener on every visit. All lists now use activity-scoped listeners that are removed automatically.
- **Saving a listing wrote to Firestore twice, with different field names** (`furnished` vs `isFurnished`).
- **Content drew under the status and gesture bars on Android 15+** (targetSdk 37 enforces edge-to-edge). The keyboard no longer covers form fields either.
- **The Maps button always opened the browser on Android 11+.** A `<queries>` entry was missing.
- **Viewings could be booked in the past.**
- **Validation dialogs closed on error,** losing the text the user had typed.
- **The forced password change accepted an empty password.**
- **Homepage search ignored the selected filter chip,** and "Near my campus" did the same as "All". The filters are now independent and combined.
- **Campus matching failed for comma-separated campus lists.**
- **Recycled list rows showed stale colours or rejection reasons.**
- **Dark mode was half-applied** (no night colours). The app is now light-only until a dark palette exists.
- **The language picker claimed to switch language without doing it.** It now says isiZulu is coming.
- **The Register and Login screens said "Email or Phone"** even though only email works.

### Validation added
- SA ID number check: 13 digits, real date of birth and Luhn check digit.
- SA phone numbers, email format, and minimum password length.
- Room rent must be a number. Duplicate room type + funding combinations are blocked.

### Housekeeping
- Removed dead code: `MainActivity`, `AccommodationDetail`, `RoomOption`, `StudentProfile` and the sample bookings.
- Removed the duplicate Material dependency (1.12.0 was declared alongside the 1.14.0 that was actually used).
- Fixed `app/gitignore`, which was missing its dot and was being ignored. Its rules are merged into the root `.gitignore`.
- The README moved to the repo root and was updated, with Firebase setup and upgrade steps.

## 4. GitHub and GitHub Actions

- **Repository:** [BENEDICT-COD/BenSuetStay](https://github.com/BENEDICT-COD/BenSuetStay)
- **CI/CD:** `android-ci.yml` runs tests and builds the debug APK on every push. `release.yml` builds a signed release APK when a version tag (e.g. `v2.0.0`) is pushed.

---

## 5. Release notes

The full list of updates since the prototype is in **[RELEASE_NOTES.md](RELEASE_NOTES.md)**. Technical history is in [CHANGELOG.md](CHANGELOG.md).

---

## 6. Getting started

1. Clone the repo and open it in Android Studio.
2. In **Firebase → Authentication**, enable **Email/Password** and **Google**.
3. Add your SHA-1 to Firebase and download the new `google-services.json`.
4. Publish `firestore.rules` to your project.
5. **Create the first admin:** Add a user in Auth, then create a document in `admins/<UID>` with `name` and `email`.

### Upgrading from an earlier build
- Delete old `landlords` and `admins` documents from Firestore.
- Recreate the admin and landlords through the app.
- Reassign existing listings to their new landlord IDs.

---

## 7. PoE requirements map

| Requirement | Where it's met |
| --- | --- |
| Register and log in, encrypted password | Firebase Authentication: `LoginActivity`, `RegisterActivity` |
| Single sign-on | Continue with Google: `GoogleSignIn.kt` |
| User settings | `StudentProfileActivity`, `LandlordProfileActivity` |
| REST API | OpenStreetMap Nominatim: `GeocodingApi.kt` |
| Offline mode with sync (SQLite) | `OfflineStore.kt`, `SyncManager.kt` |
| Real-time notifications | `NotificationCenter.kt`, `BenSuetMessagingService.kt` |
| At least two SA languages | All 11 SA languages: `res/values-*` |
| NoSQL store and read | Cloud Firestore: `*Repository.kt` |

---

## 8. Demo video
The video shows:
MY VIDEO LINK:  https://drive.google.com/file/d/1dhzIpdwjzfjv32CN37wXeFUEgEGsneGD/view?usp=sharing
1. Register and log in.
2. Google sign-in.
3. Settings & Language switch.
4. Distance to campus.
5. Offline booking and sync.
6. Real-time notifications.
7. Landlord and admin flows.

---

<p align="center"><sub>© 2026 Mpilonhle Memela · Mansuet Dev · Built for the OPSC6312 Portfolio of Evidence, Rosebank College (The IIE)</sub></p>
