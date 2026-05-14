Kutira Kone
Giving fabrics a second life.
Kutira Kone is a sustainable community-driven platform designed to reduce textile waste. It connects tailors, designers, and craft enthusiasts, allowing them to upload, sell, or swap fabric scraps instead of discarding them.

Key Features
•User Authentication: Secure login and account creation using Firebase.
•Fabric Marketplace: Browse fabric scraps with details on material type, size, price, and color.
•Proximity Filtering: Find scraps near you using an adjustable search radius (1km to 100km).
•Smart Swap System: Send swap requests to trade fabrics and manage incoming requests (Accept/Reject).
•Interactive Map: Visualize the location of nearby scraps using integrated maps.
•Direct Contact: One-tap buttons to call the owner or send a pre-filled SMS for purchasing.
•Fabric Upcycling Ideas: A dedicated section to discover creative project ideas for fabric scraps.
•Offline Support: Local database caching using Room for a smooth experience even without internet.

Tech Stack
•Language: Kotlin
•UI Framework: Jetpack Compose (Modern Declarative UI)
•Architecture: MVVM (Model-View-ViewModel)
•Backend: Firebase (Auth, Firestore NoSQL, and Cloud Storage)
•Local Database: Room persistence library
•Maps & Location: Google Maps SDK and Play Services Location
•Image Loading: Coil

Installation and Setup
1.Configure Firebase:
◦Register your app in the Firebase Console.
◦Download the google-services.json file and place it in the app folder.
◦Enable Email/Password authentication, Firestore Database, and Firebase Storage.

2.Google Maps API:
◦Enable the Maps SDK for Android in the Google Cloud Console.
◦Add your API Key to the AndroidManifest.xml file under the com.google.android.geo.API_KEY meta-data tag.

3.Build the Project:
◦Open the project in Android Studio.
◦Perform a Gradle Sync.
◦Run the app on a device with Google Play Services.

Project Structure
•model: Data classes for Fabric Scraps, User Profiles, and Swap Requests.
•ui/screens: Composable functions for every screen (Home, Map, Upload, Detail, Profile).
•ui/theme: Custom color schemes (Silk, Cotton, Denim) and typography.
•viewmodel: MainViewModel managing the app state and data flow.
•repository: Handling data operations between the local Room DB and Firebase.
•utils: Location helpers, distance calculation logic, and constants.

Future Enhancements
•In-app Chat: Real-time messaging between buyers and sellers.
•AI Material Detection: Auto-detect fabric type from uploaded photos.
•Community Feed: Share finished projects made from upcycled scraps.
