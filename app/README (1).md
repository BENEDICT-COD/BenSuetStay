# BenSuet Stay 🏠

**BenSuet Stay** is an Android application that helps South African students find safe, verified accommodation near their institutions — with funding-aware filtering that shows NSFAS-funded students only NSFAS-accredited accommodation near their campus, and self-funded/bursary students only self-funded options near theirs.

Built as the Portfolio of Evidence (PoE) project for **PROG6212** and **APPR6312** at Rosebank College (The IIE).

---

## 📱 About the App

Students across South Africa struggle to find safe, reliable accommodation close to their institutions — especially when trying to match housing to how their studies are funded. BenSuet Stay solves this with a three-role platform:

- **Students** register with their institution and funding type, then browse a homepage that automatically filters accommodation to what's actually relevant to them — right funding type, right campus.
- **Landlords** manage bookings and applications for the properties assigned to them, view student documents, and approve or decline applications — without needing to handle listing creation themselves.
- **Admins** have full control of the platform: creating, editing, and deleting accommodation listings (with amenities, per-room pricing, and photo/video uploads), and managing landlord accounts.

---

## ✨ Features

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

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Android XML layouts, ViewBinding, Material Components |
| Backend | Firebase Authentication, Cloud Firestore |
| Architecture | Activity-based navigation, RecyclerView + adapters, repository pattern with Firestore snapshot listeners |
| Media | Android Storage Access Framework (system photo/video picker) |
| Min SDK | Android 8.0 (API 26) |

---

## 📂 Project Structure

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

## 🚀 Getting Started

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

## 📋 Known Limitations / Roadmap

This is an actively developed academic project. Current known gaps:

- Offline mode with local sync (Room DB) not yet implemented
- Push notifications (Firebase Cloud Messaging) not yet implemented
- Multi-language support (English/isiZulu) not yet implemented
- Google Sign-In (SSO) not yet implemented
- Selected photos/videos are picked via the device gallery but not yet uploaded to Firebase Storage — media counts are tracked, not the files themselves
- Landlord and Admin accounts are currently managed as local/mock data rather than real Firebase Authentication accounts
- Campus/institution matching uses simple case-insensitive substring matching rather than a structured campus database

---

## 👤 Author

**Mpilonhle Memela**
Final-year Diploma in IT (Software Development), Rosebank College (The IIE)
[Mansuet Dev](https://mansuetdev.tech) — freelance web & mobile development
[GitHub](https://github.com/BENEDICT-COD) · [LinkedIn](https://linkedin.com/in/mpilonhle-memela-a71130293)

---

## 📄 License

This project was developed for academic purposes as part of the PROG6212/APPR6312 Portfolio of Evidence at Rosebank College (The IIE).
