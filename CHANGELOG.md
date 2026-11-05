# Changelog

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
- Added unit tests: `ListingLogicTest` and `ValidatorsTest`.
