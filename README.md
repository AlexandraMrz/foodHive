# 🐝 FoodHive
- A cross-platform mobile app built to help people prevent food waste, save money, and eat smarter.
Built as my **bachelor thesis project**, FoodHive combines AI, computer vision, and a friendly UX to tackle one of today's most pressing challenges: food waste.
---
# 🌱 Why FoodHive?
- Each year, millions of tons of food are wasted - costing money, hurting the planet, and leaving resources unused.
FoodHive empowers users to track their groceries, get notified before food expires, and discover recipes based they already have.
---
## 🚀 Features
- ✅ Add products manually or via:
- Barcode scanning (OpenFoodFacts API integration)
- Image recognition (Google Vision API)
- ✅ Automatically detect product names & estimate expiration dates
- ✅ Color-coded product cards (green/yellow/red) based on expiry
- ✅ AI-powered recipe suggestions (Spoonacular & Gemini APIs) based on what's in your fridge
- ✅ Add missing recipe ingredients to shopping list with one tap
- ✅ Smart notifications whhen products are close to expiring
- ✅ Shopping list management with category grouping
- ✅ Dark & light theme support
- ✅ Responsive & intuitive UI built with Jetpack Compose

---
## 📚 About the Project
This app was developed as part of my **bachelor thesis in Computer Science (2025) at Transilvanya University of Brasov.
I combined my passion for programming, UX, and sustainability to create a tool that can genuinely help reduce food waste in households.
---
## 🔮 Planned Improvments
- ✨Advanced analytics dashboard (weekly/monthly waste stats)
- ✨More AI-powered suggestions for diet & meal planning
- ✨Cloud sync & multi-deevice support
- ✨Public beta release on Google Play & App Store

---
## 🛠️ Tech Stack
- 📱**Jetpack Compose (Android)**
- ☁️**Firebase (Auth, Firestore, Storage, Notifications)**
- 🤖**Google Vision API (OCR & label detection)**
- 📦**OpenFoodFacts API (barcodes)**
- 🧑‍🍳**Spoonacular API + Gemini API (recipe suggestions)**
- 🧰**Kotlin, Android Studio, Material Design 3**

---
## 🧑‍💻Getting Started
- If you'd like to try out FoodHive yourself or contribute to its development here's a step-by-step guide to get in running on your machine.
- Don't worry - you don't need to be an Android wizard to follow this. 😉

- ## 🔷 Step 1: Prerequisites
- Before you start, make sure you have these tools installed:
- - ✅ Android Studio (latest stable version)
  - ✅ Java JDK (I used version 17 but you can use a newer one)
  - ✅ A Google account (for Firebbase & Google Vision API)
  - ✅ Internet connection (to fetch dependencies & APIs)
  - Optional but recommended:
  - ✅ An Android device with USB debugging enabled OR use the Android Emulator from Android Studio
 
  ## 🔷 Step 2: Fork the Repository
  1. Go to this repository
  2. Click the `fork` button in the top right corner to create your own copy of the repo
  3. Once forked, clone it to your machine:
  ```
  git clone https://github.com/your-username/foodhive.git
  cd foodhive
  ```
  ## 🔷 Step 3: Open in Android Studio
  1. Open Android Studio
  2. Click on ``Open an existing project`` and select ``foodhive`` folder you just cloned.
  3. Let Android Studio finish syncing the Gradle files (this may take a few minutes on first run).
 
  ## 🔷 Step 4: Set up Firebase
  FoodHive uses Firebase for authentication, Firestoore database, and notifications.
  You'll need to set up your own Firebase project:
  1. Go to Firebase Console and create a new project
  2. Add an Android app to your project (package name> you can keep ``com.example.foodhive`` or change it.
  3. Download the ``google-services.json`` file from Firebase and place it in the ``/app`` folder of the project.
  4. Enable these Firebase services in the console:
  - Authentication (Email/Password)
  - Firestore Database
  - Cloud Messaging (optional for notifications)
    
  ## 🔷 Step 5: Set up API Keys
  FoodHive uses additional APIs:
  - Google Cloud Visioon (for image recognition)
  - Spoonacular API (for recipes)
  - (optional) Gemini API for conversational AI
 Create accounts & generate your API keys for each service
Then, open the appropiate configuration files in the projects and replace the placeholders with your actual keys.

## 🔷 Step 6: Run the App
- ✅Plug in your Android device (or start an emulator)
- ✅Click the green ``Run`` nutton in Android Studio
- ✅That's all - you should see FoodHive running! 🐝

🌟 Tips for New Contribuitors
- Always create a new branch for your changes
- Follow the existing code style & naming conventions
- Test your changes before submitting a pull request
- Feel free to open an issue if you encounter bugs or have feature suggestions!

---
## 🤝 Acknowledgements
🙏 Thanks to:
- My thesis advisor & faculty mentor
- The open-sorce community behind OpenFoodFacts, Google Vision, Spoonacular, and Gemini
- Everyone how gave me feedback & tested early versions

---
## 📫Contact
- 👤 Alexandra
- 📧 mrz.alexandra17@gmail.com
- 🌐 LinkedIn:  https://www.linkedin.com/in/alexandra-valentina-m-aa2a45259/

