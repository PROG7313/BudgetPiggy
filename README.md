
# 🐷 BUDGET PIGGY  
### PROG7313 POE 🎓 – Personal Budgeting Reinvented

![BudgetPiggy Logo](app/src/main/res/drawable-xxxhdpi/pic_piggylogo.png)

---

## 👥 Team Members
- **Markus Fourie** (ST10269396)  
- **Derik Korf** (ST10268524)  
- **Kyle Nel** (ST10298302)  

---

## 🎬 Video Demo
▶️ [Watch our Demo](https://youtu.be/96Y0Juvut9k)

---

## 🌟 Purpose of the App
BudgetPiggy was developed to make **budgeting fun, interactive, and visually engaging**. It targets individuals looking to:
- Understand spending habits
- Stay consistent with expense logging
- Develop financial discipline through gamification
- Manage their money smartly using intuitive UI and real-time feedback

By transforming mundane financial tasks into a gamified journey, we promote **financial literacy** in a rewarding and engaging way.

---

## ✨ Key Features
| Category | Feature |
|---------|---------|
| 🔐 **Security** | Encrypted login & registration with future support for OAuth (Google, Facebook) |
| 💰 **Transaction Management** | Add, categorize, edit, delete transactions with receipt attachments |
| 📊 **Visualization** | Dynamic graphs and pie charts show category-wise expenses |
| 🎯 **Budgeting Goals** | Monthly budget tracking with visual indicators and warnings |
| 🏆 **Gamification** | Badges and rewards for financial milestones (e.g., streaks, savings) |
| 🔔 **Smart Notifications** | Contextual reminders and goal alerts |
| 🔄 **Transfers** | Move funds between accounts and categories |

> 📍 Backed by `Firebase` for a full experience, with  cloud DB sync.

## 🐷 Own Features
| Category | Feature |
| 📈 **Streak Tracker** | Encourages daily check-ins and logging |
| 🌍 **Currency Conversion** | Live conversion for multiple currencies |

---

## 🖌️ Design & User Experience

📎 **Figma Design**  
- [🔗 Full Design](https://www.figma.com/design/HWWY7d3LVORQhSZKvHSVYF/BudgetPiggy?m=auto&t=pVesay7ycyU4uyCx-1)

---

## 📷 UI Screenshots

### 🐽 Welcome Page
![Welcome Page](Screenshots/welcome.png)

### 🔐 Sign Up Page
![Sign Up Page](Screenshots/signup.png)

### 🏠 Home Dashboard
![Home Dashboard](Screenshots/home.png)

### 👛 Wallet Page
![Wallet Page](Screenshots/wallet.png)

### 💱 Currency Settings
![Currency Page](Sscreenshots/currency.png)

### 🔁 Transaction History
![Transaction History](Screenshots/transactionhistory.png)

### 📊 Reports and Insights
![Reports Page](Screenshots/reports.png)

### 🔔 Notifications Center
![Notifications](Screenshots/notifications.png)

### ⚙️ Account Settings
![Account Settings](Screenshots/account.png)

---

## 📅 Project Planning & Methodology

### 📌 Planning Highlights
- Defined MVP and user requirements
- Built UI/UX prototypes in Figma
- Developed backend logic and integrated APIs
- Implemented testing & documentation phases


### ✅ GitHub & GitHub Actions
- 🔄 Version control using GitHub
- ⚙️ Automated builds/tests with GitHub Actions:
  - Run tests on push
  - Validate builds across SDK versions

```yaml
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v2
    - name: Set up JDK
      uses: actions/setup-java@v2
      with:
        java-version: '17'
    - name: Build with Gradle
      run: ./gradlew build
```

---

## 🛠️ Installation & Running

1. 📥 Download this repository as a ZIP  
2. 📂 Unzip and open in **Android Studio Meerkat**  
3. ▶️ Build & run on an emulator or Android device (API 21+)  

> Ensure developer mode is enabled for physical devices.

---

## 🗂️ References

Ambitions, C., 2025. Kotlin Full Course | Kotlin For Beginners | Kotlin For Android KMP | Kotlin Tutorial 2025. [Online]  
Available at: https://www.youtube.com/watch?v=8uEYI6lTGps&ab_channel=CodingAmbitions  
[Accessed 5 April 2025].

Android, 2025. Android Platform. [Online]  
Available at: https://developer.android.com/reference/android/package-summary?hl=en  
[Accessed 5 April 2025].

CodingStuff, 2024. The Complete Beginner Guide for Room in Android 2024 | Local Database Tutorial for Android. [Online]  
Available at: https://www.youtube.com/watch?v=r_UfOz3yaLg&ab_channel=CodingSTUFF  
[Accessed 10 April 2025].

Developers, 2025. Develop a UI with Views. [Online]  
Available at: https://developer.android.com/studio/write/layout-editor  
[Accessed 7 April 2025].

Developers, 2025. Use Kotlin coroutines with lifecycle-aware components. [Online]  
Available at: https://developer.android.com/topic/libraries/architecture/coroutines  
[Accessed 6 April 2025].

Gaur, H., 2025. Master Encrypted Room Database in Android | Secure Your App's Data. [Online]  
Available at: https://www.youtube.com/watch?v=PmxiYt1dR5s&ab_channel=HimanshuGaur  
[Accessed 6 April 2025].

GeeksforGeeks, 2022. Dynamic Spinner in Android. [Online]  
Available at: https://www.geeksforgeeks.org/dynamic-spinner-in-android/  
[Accessed 10 April 2025].

tech, l., 2023. Android Studio User Interface - Layout and XML. [Online]  
Available at: https://www.youtube.com/watch?v=kn_7H8v4Emo&ab_channel=larntech  
[Accessed 5 April 2025].

---

## 🚀 Future Enhancements

- AI-driven budget suggestions
- Shared budgeting across users
- Kotlin Multiplatform support

---

## 💬 Feedback & Contributions

We welcome stars ⭐, forks 🍴, and feedback 💬!  
Please open an issue or contact a team member for support.

---
