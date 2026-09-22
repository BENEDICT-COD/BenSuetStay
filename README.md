# BenSuet Stay 
**VIDEO PRESENTATION**
https://drive.google.com/file/d/1ikP7i9yvL_1ChDlEHmDNDV4_kkpdz1Up/view?usp=sharing

**BenSuet Stay** is an Android application that helps South African students find safe, verified accommodation near their institutions — with funding-aware filtering that shows NSFAS-funded students only NSFAS-accredited accommodation near their campus, and self-funded/bursary students only self-funded options near theirs.

Built as the Portfolio of Evidence (PoE) project for **PROG6212** at Rosebank College (The IIE).

---

## About the App

Students across South Africa struggle to find safe, reliable accommodation close to their institutions — especially when trying to match housing to how their studies are funded. BenSuet Stay solves this with a three-role platform:

- **Students** register with their institution and funding type, then browse a homepage that automatically filters accommodation to what's actually relevant to them — right funding type, right campus.
- **Landlords** manage bookings and applications for the properties assigned to them, view student documents, and approve or decline applications — without needing to handle listing creation themselves.
- **Admins** have full control of the platform: creating, editing, and deleting accommodation listings (with amenities, per-room pricing, and photo/video uploads), and managing landlord accounts.

---

##  Features

### Student
- Register with full name, address, institution/school, email, password, and funding type (NSFAS or Self-Funded/Bursary)
- Real Firebase Authentication for register/login, with proper success/failure feedback
- Homepage automatically filtered by the student's funding type **and** institution/campus on login
- Guest browsing mode (no account required) shows all listings, unfiltered
- Search accommodations by name, location, or room type
- Full listing detail view: image carousel, amenities, contact landlord, room type selection with live pricing and availability
- View own bookings and profile

### Landlord
- Dashboard with at-a-glance stats (active listings, pending applications, upcoming viewings)
- View assigned accommodations (read-only — listings are created by admin)
- Manage viewing bookings: confirm or decline requests from students
- Manage applications: view student-submitted documents (ID copy, proof of registration, or NSFAS confirmation details), approve or reject

### Admin
- Platform-wide dashboard: total listings, landlords, students, applications, and bookings
- Full listing management (create/edit/delete): name, location, campus served, amenities, per-room-type funding + pricing + availability, and real device photo/video picker
- Landlord account management: create new accounts with a temporary password, delete existing ones
- All listing data syncs live to every device via Firestore's real-time listeners

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Android XML layouts, ViewBinding, Material Components, ViewPager2 |
| Backend | Firebase Authentication, Cloud Firestore, Firebase Cloud Storage |
| Architecture | Activity-based navigation, RecyclerView + adapters, repository pattern with Firestore snapshot listeners |
| Image loading | Coil |
| Media selection | Android Storage Access Framework / Photo Picker (`ActivityResultContracts`) |
| Min SDK | Android 8.0 (API 26) |

---

##  APIs & Services Used (and how they're implemented)

BenSuet Stay is built entirely on **Firebase** (Google's backend-as-a-service platform), wired up via `google-services.json` and the Firebase BoM (`33.5.1`), plus one small third-party library for image loading. No custom backend server or REST API was written for this project — Firebase's client SDKs act as the backend.

### 1. Firebase Authentication (`firebase-auth-ktx`)
Used for real student account creation and login (not mock data).
- **Register/Login:** `RegisterActivity` and `LoginActivity` call `FirebaseAuth.getInstance().createUserWithEmailAndPassword(...)` / `signInWithEmailAndPassword(...)`, with `addOnSuccessListener` / `addOnFailureListener` callbacks driving the UI feedback (success navigation vs. inline error messages).
- **Password recovery:** `ForgotPasswordActivity` uses `sendPasswordResetEmail(...)` to trigger Firebase's built-in reset flow.
- **Session-aware screens:** `StudentProfileActivity`, `EditStudentProfileActivity`, and `LandlordProfileActivity` read `auth.currentUser?.uid` to know which Firestore document to fetch/update, so profile data always belongs to the signed-in account.
- Landlord and Admin accounts are currently handled as local/mock data rather than Firebase Auth accounts (see Known Limitations) — only the Student role uses real authentication end to end.

### 2. Cloud Firestore (`firebase-firestore-ktx`)
The app's live database, used as a real-time NoSQL data store rather than a one-off REST call.
- **Repository pattern:** each domain area has its own repository (`ListingRepository`, `StudentRepository`, `BookingRepository`, `ApplicationRepository`, `LandlordRepository`, `AdminRepository`) that wraps `FirebaseFirestore.getInstance()` and exposes clean functions to the Activities — Activities never call Firestore directly.
- **Real-time sync:** listings use `collection("accommodations").addSnapshotListener { ... }`, so any change an Admin makes (add/edit/delete a listing) is pushed to every connected Student/Landlord device immediately, with no manual refresh or polling.
- **Writes:** `document(id).set(...)` / `.update(...)` for creating and editing listings, bookings, and applications; `.delete()` for admin listing/landlord removal.
- **Reads:** `.get().addOnSuccessListener { snapshot -> ... }` for one-off fetches (e.g. loading a student's profile), and query filtering (`whereEqualTo(...)`) combined with in-app case-insensitive substring matching to implement the funding-type + institution filtering on the student homepage.
- **Collections:** `students` (written at registration, read at login/homepage filtering) and `accommodations` (written by Admin, read by Homepage, Listing Detail, and Landlord Dashboard) — see the table further down.

### 3. Firebase Cloud Storage (`firebase-storage-ktx`)
Used specifically for **profile picture uploads** for Students and Landlords.
- `StudentProfileActivity` and `LandlordProfileActivity` each hold a `FirebaseStorage.getInstance()` reference.
- The flow: the user picks an image via `ActivityResultContracts.GetContent()` → the resulting `Uri` is uploaded with `storageRef.putFile(uri)` → on success, `ref.downloadUrl.addOnSuccessListener { downloadUri -> ... }` retrieves the public URL → that URL string is saved onto the user's Firestore document (`profileImageUri`) so it can be re-loaded on any device/session.
- Listing photos/videos (Admin side) are currently picked via the system picker and counted, but **not yet uploaded to Storage** — this is an explicit known limitation, not an oversight (see below).

### 4. Coil (`io.coil-kt:coil:2.7.0`)
A lightweight, Kotlin-first image-loading library (not a Firebase product).
- Used via the `coil.load` extension function (e.g. `binding.ivProfilePicture.load(imageUrl) { transformations(CircleCropTransformation()) }`) to asynchronously download, cache, and render profile pictures and listing images from their Firebase Storage / Firestore-stored URLs directly into `ImageView`s, with a circular crop transformation applied for profile photos.

### 5. Android Storage Access Framework / Photo Picker (`androidx.activity.result.contract.ActivityResultContracts`)
Not a network API, but a core system integration used throughout:
- `GetContent()` — single image selection (profile pictures).
- `OpenDocument()` / `OpenMultipleDocuments()` — Admin's listing photo/video picker in `AddEditListingActivity`, which lets Admins select one or more media files straight from device storage without the app needing broad storage permissions (the framework hands back scoped, temporary URI access).

### Why this stack, not a custom REST API
For an academic PoE with a hard deadline and three interconnected roles, Firebase's client SDKs remove the need to design, host, and secure a separate backend server, while still providing exactly what the brief needed: real authentication, a live multi-user database (so Admin's changes appear instantly for Students and Landlords), and file storage — all callable directly and securely from the Android client.

---

##  Project Structure

```
app/src/main/java/com/mansuetdev/bensuetstay/
├── RegisterActivity.kt / LoginActivity.kt        # Student authentication
├── HomeActivity.kt / ListingDetailActivity.kt    # Student browsing experience
├── LandlordDashboardActivity.kt                  # Landlord: overview + assigned listings
├── LandlordBookingsActivity.kt                   # Landlord: confirm/decline viewings
├── LandlordApplicationsActivity.kt               # Landlord: review + decide applications
├── ApplicationDetailActivity.kt                  # Full application view with documents
├── AdminDashboardActivity.kt                     # Admin: platform-wide overview
├── AdminListingsActivity.kt / AddEditListingActivity.kt   # Admin: full listing CRUD
├── AdminLandlordsActivity.kt / AddLandlordActivity.kt     # Admin: landlord account management
├── Accommodation.kt, AdminListing.kt, RoomConfig.kt       # Core data models
├── StudentProfile.kt, Booking.kt, StudentApplication.kt  # Supporting data models
├── ListingRepository.kt                          # Firestore-backed, real-time listing data source
└── ...adapters (AccommodationAdapter, BookingAdapter, ApplicationAdapter, LandlordAdapter)
```

---

## How BenSuet Stay Is Different

South African students already have accommodation options like **DigsConnect**, **Amber Student**, and **Property24**. BenSuet Stay isn't trying to out-list them — it targets a specific gap none of the three are built to solve.

| | **BenSuet Stay** | **DigsConnect** | **Amber Student** | **Property24** |
|---|---|---|---|---|
| **Core focus** | SA student accommodation, funding-type aware | SA student accommodation marketplace | Global student accommodation booking platform | General-purpose SA property portal (all property types, not student-specific) |
| **NSFAS vs. self-funded filtering** | Built into the login flow — a student only ever sees accommodation accredited for *their* funding type | Not funding-type specific; students filter listings manually by price/area themselves | Not applicable — global platform, no NSFAS concept | Not applicable — no student funding concept at all |
| **Who creates listings** | Admin only, centrally — with a defined verification/accreditation step (NSFAS-accredited vs. self-funded) before a listing can appear to students | Landlords/agents self-list directly, marketplace-style | Property Management Groups / Purpose-Built Student Accommodation operators list directly | Any landlord or agent self-lists directly |
| **Landlord's role** | Manage bookings and applications for listings *assigned to them* by Admin — no listing creation needed, so a landlord can't misrepresent NSFAS accreditation | Landlords manage their own full listings and tenant communication | Property partners manage inventory and pricing; amber's team handles booking/support | Landlords/agents manage their own listings independently |
| **In-app application review** | Landlords review student-submitted documents (ID, proof of registration, NSFAS confirmation) and approve/decline *inside the app*, tied to a live Firestore record | Contact and viewing arranged through the platform, but document vetting is off-platform | Booking-led (like an OTA); heavier emphasis on payments/contracts than document-based application review | No student application or vetting workflow at all — plain rental listings |
| **Data model** | Real-time (Firestore snapshot listeners) — an Admin edit is visible to every Student/Landlord instantly | Standard listings marketplace | Standard booking platform with agent support | Standard listings portal |
| **Geographic/market scope** | Purpose-built for South African institutions and NSFAS specifically | South Africa-focused, expanding internationally | Global (250+ cities, primarily serving international students moving abroad) | South Africa-wide, general property (residential, commercial, student) |

**In short:** DigsConnect and Amber Student are both *marketplaces* — they connect students to landlords and largely leave verification and funding-matching to the student. Property24 isn't student-specific at all. BenSuet Stay's differentiator is that **funding type is a first-class part of the data model**, not a filter option: the accreditation check happens before a listing is even shown, listing creation is centralized under Admin so accreditation claims can't be misrepresented by an individual landlord, and the student-to-landlord application (with document upload and approval/decline) happens natively in the app rather than being handed off to email, WhatsApp, or an external process.

---

##  Getting Started

### Prerequisites
- Android Studio (latest stable)
- A Firebase project with **Authentication** (Email/Password provider enabled) and **Cloud Firestore** set up

### Setup
1. Clone this repository.
2. Create a Firebase project at [console.firebase.google.com](https://console.firebase.google.com) and register an Android app with package name `com.mansuetdev.bensuetstay`.
3. Download the generated `google-services.json` and place it in the `app/` directory (same level as `app/build.gradle.kts`).
4. Enable **Email/Password** sign-in under Authentication → Sign-in method.
5. Create a Cloud Firestore database (test-mode rules are fine for development; production rules should be configured before any real deployment).
6. Open the project in Android Studio, let Gradle sync, then build and run.

### Firestore Collections
| Collection | Written by | Read by |
|---|---|---|
| `students` | Student registration | Student login, Homepage filtering |
| `accommodations` | Admin (Add/Edit Listing) | Homepage, Listing Detail, Landlord Dashboard |

---

##  Known Limitations / Roadmap

This is an actively developed academic project. Current known gaps:

- Offline mode with local sync (Room DB) not yet implemented
- Push notifications (Firebase Cloud Messaging) not yet implemented
- Multi-language support (English/isiZulu) not yet implemented
- Google Sign-In (SSO) not yet implemented
- Selected photos/videos are picked via the device gallery but not yet uploaded to Firebase Storage — media counts are tracked, not the files themselves
- Landlord and Admin accounts are currently managed as local/mock data rather than real Firebase Authentication accounts
- Campus/institution matching uses simple case-insensitive substring matching rather than a structured campus database

---

## Author

**Mpilonhle Memela**
Final-year Diploma in IT (Software Development), Rosebank College (The IIE)
[Mansuet Dev](https://mansuetdev.tech) — freelance web & mobile development
[GitHub](https://github.com/BENEDICT-COD) · [LinkedIn](https://linkedin.com/in/mpilonhle-memela-a71130293)

---

## License

This project was developed for academic purposes as part of the PROG6212 Portfolio of Evidence at Rosebank College (The IIE).
