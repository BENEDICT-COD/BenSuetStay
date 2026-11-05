# BenSuet Stay 

**BenSuet Stay** is an Android app that helps South African students find safe, verified accommodation near their institutions, with funding-aware filtering. NSFAS-funded students see NSFAS-accredited rooms near their campus, and self-funded or bursary students see self-funded options near theirs.

Built as the Portfolio of Evidence (PoE) project for **OPSC6312** at Rosebank College (The IIE).

---

##  About the App

There are three roles. All of them sign in with Firebase Authentication.

- **Students** register with their institution and funding type. They browse listings filtered to them, book viewings, and apply for rooms with their documents. They can then track each application's outcome and the landlord's feedback.
- **Landlords** are created by an admin. They see only the listings assigned to them, and only the bookings and applications for those listings. They confirm or decline viewings and approve or reject applications. Approving takes one space off the room automatically.
- **Admins** create, edit and delete listings, including photos and a video. They also create and remove landlord accounts and send landlords password-reset emails.

##  Features

### Student
- Registration with name, address, institution, email, password and funding type (NSFAS or Self-Funded/Bursary).
- Stays signed in across app restarts.
- Homepage defaults to the student's funding type and their campus. The two filters work independently, and search applies on top of both.
- Guests can browse every listing without an account.
- Listing detail page with a photo carousel, amenities, landlord contact, live room availability and map link.
- Viewing bookings, which can't be made in the past.
- Room applications, where the student picks their documents (PDF or image) and the file names are recorded:
  - **NSFAS:** SA ID number, checked for a valid date of birth and Luhn check digit, plus student number, campus and NSFAS status letter.
  - **Self-funded:** next of kin, ID copy and proof of registration.
  - Duplicate applications to the same residence are blocked.
- **My Bookings** and **My Applications** screens show status and feedback live.

### Landlord
- Dashboard with live counts of active listings, pending applications and upcoming viewings.
- Must change the temporary password on first login.
- Confirm or decline viewing requests, with a reason and a suggested alternative time.
- Review applications and see which documents the student provided. Approving or declining includes a note for the student. Approval decrements room availability inside a Firestore transaction.

### Admin
- Platform stats using Firestore `count()` queries, so no documents are downloaded.
- Full listing management:
  - Fields: name, location, campuses served, amenities, transport, furnishing and electricity.
  - Rooms: per-room-type funding, rent and spaces.
  - Media: listing photos, room photos and a video, linked from the admin's phone.
- Landlord management: create accounts, send reset emails, delete accounts (their listings are unassigned).

##  PoE Requirements: where to find each one

| Requirement | How it's done | Code |
|---|---|---|
| Register & log in, password encrypted | Firebase Authentication stores passwords as salted **scrypt hashes**. The app and Firestore never see or store them. | `LoginActivity`, `RegisterActivity` |
| Single sign-on (SSO) | **Continue with Google** via Android Credential Manager → Firebase Auth. New Google users finish a short campus/funding step with no password. | `GoogleSignIn.kt` |
| Settings | Profile, photo, password, language, notification switches, home-screen defaults, offline sync status, FAQ, contact support, privacy policy, delete account | `StudentProfileActivity`, `LandlordProfileActivity` |
| REST API | **OpenStreetMap Nominatim** geocoding (`GET /search`). Listing coordinates are stored when the admin saves, and students see **"≈ 2.4 km from your campus"**. Responses are cached and rate-limited to 1 request per second, per the API's policy. | `GeocodingApi.kt` |
| Offline mode with sync | **SQLite outbox.** Viewing bookings and applications made offline are stored locally and sent automatically when the connection returns (Android network callback). Listings still browse offline from Firestore's on-device cache. | `OfflineStore.kt`, `SyncManager.kt` |
| Real-time notifications | **FCM push** to topics (`all`, `students`, `landlords`, `admins`), plus **real-time event alerts** via a `notifications` collection: booking requested/confirmed/declined, application submitted/approved/declined | `NotificationCenter.kt`, `BenSuetMessagingService.kt`, `Notifier.kt` |
| Multi-language | All 11 written SA languages | `res/values-*`, `LanguagePicker.kt` |
| NoSQL store/read | Cloud Firestore | `*Repository.kt` |

##  Demo-video checklist

1. **Register and log in.** Then open Firebase → Authentication and point out that passwords aren't visible anywhere.
2. **Continue with Google** on the login screen.
3. **Settings:** edit your profile, change your password, and switch the language (for example, to isiZulu). Every screen changes language.
4. **REST API:**
   1. As admin, save a listing. The button shows "Finding location…".
   2. As a student, open that listing to see "≈ X km from <campus>".
5. **Offline sync:**
   1. Turn on airplane mode, then book a viewing. You'll see the "saved offline" message, and My Bookings shows **Waiting to sync**.
   2. Turn airplane mode off. A "Back online" notification appears and the booking syncs.
6. **Real-time notifications.** Use two emulators/phones (student and landlord). The landlord confirms the viewing and the student's phone shows a notification immediately.
7. **Push notifications.** Send one from the Firebase console to the `students` topic with the app closed (setup step 7 below).

##  Languages

The whole app is available in all 11 written official languages of South Africa: English, Afrikaans, isiZulu, isiXhosa, Sepedi, Setswana, Sesotho, Xitsonga, siSwati, Tshivenḓa and isiNdebele.

- **Where to switch:** the globe button on the Welcome screen, or **Settings → App Language** (every role). On Android 13+ it also appears under the phone's *Settings → Apps → BenSuet Stay → Language*.
- **How it works:** AppCompat's per-app language API. The choice is remembered and the screen redraws in the new language.
- **Where the text lives:** `res/values-<code>/strings.xml`, with Sepedi in `values-b+nso`.
- **What stays the same:** values stored in Firestore (room types, amenities, funding) stay in English, so filtering and approvals work the same in every language. Only what's displayed is translated.
- **Native review recommended:** translations were machine-assisted and checked for completeness and formatting. Before release, have a native speaker review them, especially Tshivenḓa, isiNdebele, siSwati and Xitsonga.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | XML layouts, ViewBinding, Material Components, edge-to-edge (Android 15+ ready) |
| Auth | Firebase Authentication (email/password) for all roles |
| Data | Cloud Firestore with scoped real-time listeners and security rules |
| Files | Listing media linked from the admin's device; Cloud Storage for profile pictures only (optional, needs Blaze) |
| Images | Coil |
| Min / target SDK | 26 (Android 8.0) / 37 |

##  Project Structure

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

### Firestore collections

| Collection | Doc id | Written by | Read by |
|---|---|---|---|
| `students` | Auth uid | the student | that student, admins |
| `landlords` | Auth uid | admins (+ own photo / password flag) | that landlord, admins |
| `admins` | Auth uid | Firebase console | that admin |
| `accommodations` | auto | admins (+ assigned landlord: room spaces only) | everyone, including guests |
| `bookings` | auto | the student (create), the landlord (status) | that student, that landlord, admins |
| `applications` | auto | the student (create), the landlord (status/feedback) | that student, that landlord, admins |

##  Getting Started

### Prerequisites
- Android Studio (latest stable)
- A Firebase project. The free **Spark** plan is enough. Only profile pictures need Cloud Storage, which requires the Blaze plan.

### First-time Firebase setup

1. **Authentication:** Authentication → Sign-in method → enable **Email/Password**.
2. **Firestore:** create the database if you haven't already. Then go to Firestore → **Rules**, paste in [`firestore.rules`](firestore.rules) and click **Publish**.
3. **Storage (optional, Blaze plan only):** only needed for profile pictures. Storage → Get started. Then go to **Rules**, paste in [`storage.rules`](storage.rules) and click **Publish**. When asked, allow Storage to access Firestore.
4. **Create the first admin.** The app never creates admins.
   1. Authentication → Users → **Add user**, using an email and password.
   2. Copy the new user's **UID**.
   3. Firestore → start a collection called `admins`. Use the UID as the **document ID** and add the fields `name` (string) and `email` (string).
   4. Log in to the app with that email. You'll land on the Admin dashboard.
5. As admin, create landlords (Manage Landlords → +) and listings (Manage Listings → +). Assign each listing a landlord, otherwise it can't take bookings or applications.
6. **Google sign-in (SSO)**
   1. Authentication → Sign-in method → **Google** → Enable → choose a support email → Save.
   2. Get your debug **SHA-1**:
      - In Android Studio: Gradle panel → *app → Tasks → android → signingReport*.
      - Or in a terminal: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`
   3. Project settings → Your apps → Android app → **Add fingerprint** → paste the SHA-1.
   4. **Download `google-services.json` again** and replace `app/google-services.json`. It now contains the OAuth client the app needs, and the client ID is picked up automatically.
   5. Rebuild.
7. **Push notifications (FCM, free on Spark)**
   1. Open the app once and log in, which subscribes the phone to its topics.
   2. Go to Firebase console → **Messaging** → *Create your first campaign* → **Firebase Notification messages**.
   3. Add a title and text.
   4. Under Target, choose **Topic** and enter `students` (or `landlords`, `admins`, `all`), then publish.
   5. A brand-new topic can take a few minutes to become available.
8. **Republish `firestore.rules`** whenever this file changes. The latest version adds the `notifications` collection.

### Upgrading from the earlier build
Earlier versions stored landlord and admin passwords in Firestore and linked bookings and applications by name. After this update:
- Delete the old `landlords` and `admins` documents. Create the admin as in step 4 and recreate landlords in the app.
- Delete old test documents in `bookings` and `applications`. They have no `studentId`/`landlordId`, so nobody but an admin can see them.
- Reassign every existing listing to its new landlord (edit the listing → choose the landlord → Save). The old landlord IDs no longer exist, so bookings and applications on unassigned listings would reach nobody.
- Existing **student** accounts keep working as they are.

### Run
Open in Android Studio, let Gradle sync, then run on a device or emulator. Test on **Android 15 or newer** too, because edge-to-edge drawing is enforced there.

### Tests
```
./gradlew testDebugUnitTest
```
`ListingLogicTest` covers filtering, campus matching, rent parsing and sorting. `ValidatorsTest` covers the SA ID, phone and email checks.

##  Known Limitations / Roadmap
- **Listing photos and video only display on the phone that picked them** (the admin's). Other phones show a placeholder. Sharing them needs Cloud Storage (Blaze plan) or a free image host.
- **Application documents aren't uploaded.** Only their file names are saved, so landlords ask students to bring the documents to the viewing. Uploading needs Cloud Storage (Blaze plan).
- Profile pictures need Cloud Storage, so they won't upload on the Spark plan.
- Automatic event notifications (e.g. "viewing confirmed") arrive instantly while the app is running or recently used. Delivering them while the app is fully closed needs a server to send FCM messages (Cloud Functions require the Blaze plan). Console/topic pushes do arrive when the app is closed.
- Deleting a landlord in the app removes their profile and blocks login. The Auth account itself has to be deleted in the console, or by a Cloud Function.
- Campus matching is text-based ("Wits" ≠ "University of the Witwatersrand"). A fixed list of institutions would be more reliable.

##  Author

**Mpilonhle Memela**
Final-year Diploma in IT (Software Development), Rosebank College (The IIE)
[Mansuet Dev](https://mansuetdev.tech): freelance web & mobile development
[GitHub](https://github.com/BENEDICT-COD) · [LinkedIn](https://linkedin.com/in/mpilonhle-memela-a71130293)

## 📄 License

Developed for academic purposes as part of the OPSC Portfolio of Evidence at Rosebank College (The IIE).
