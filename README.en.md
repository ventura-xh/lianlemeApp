# Lianleme - Fitness Assistant (LianlemeApp)

Lianleme is a modern Android fitness assistant application built based on Kotlin + Jetpack Compose.

## 🏋️ Core Features

### 1. Training Plan Management
*   **Plan Creation**: Create custom training plans, support adding exercises, setting sets, weight, and reps.
*   **Exercise Library**: Built-in rich preset exercise library covering various muscle groups and training categories. Supports filtering by muscle or category.
*   **Exercise Editing**: Support adding new custom exercises, or resetting the preset exercise library.

### 2. Training Execution
*   **Real-time Execution**: Provides a clear interface when executing plans, tracking the completion status of each set (weight, reps).
*   **Timers**:
    *   **Workout Timer**: Automatically records total workout duration.
    *   **Rest Timer**: Countdown timer for rest between sets, supports voice prompts and vibration reminders.
    *   **Floating Window**: A timer floating window can be summoned during training without affecting other operations.
*   **Resume Training**: If unexpectedly exited or switched to background, upon restart the app will prompt to resume the last training progress.

### 3. Training Records and Statistics
*   **Calendar View**: Intuitively displays daily training records.
*   **Statistical Reports**: Generates monthly training reports, statistics on training frequency, total duration, exercise distribution, muscle group coverage, etc.
*   **Session Details**: View detailed completion status for each training session (weight and reps per set).

### 4. Personal Data Management
*   **Body Data**: Record metrics such as weight, height, BMI, body fat percentage, muscle mass, as well as body circumferences (chest, waist, hips, arms, legs).
*   **Body Fat Calculator**: Built-in US Military Three-Site and Seven-Site methods for body fat calculation formulas, assisting in assessing body composition.
*   **RM Calculator**: Input weight and reps to quickly estimate 1RM (One Repetition Maximum), and generate common rep count comparison tables.

### 5. Other Features
*   **Data Persistence**: Uses Room database to store all training data, local exercise library, and user information.
*   **Dark Mode**: Supports following system settings or manually switching between dark/light themes.
*   **Result Sharing**: After training ends, a training result image can be generated and saved to the gallery.

## 🛠 Tech Stack

*   **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) - Declarative UI building.
*   **Architecture Pattern**: MVVM (Model-View-ViewModel).
*   **Database**: [Room](https://developer.android.com/training/data-storage/room) - SQLite abstraction layer, type-safe data persistence.
*   **Navigation**: [Jetpack Navigation](https://developer.android.com/guide/navigation) - Navigation component support.
*   **Asynchronous Processing**: Kotlin Coroutines & Flow.
*   **Dependency Injection**: Manual (ViewModel requires DAO/Repository instances).

## 📁 Project Structure Overview

*   `dao/`: Data Access Objects (DAO), defining Room database operations.
*   `entity/`: Database entity classes (Entity).
*   `repository/`: Repository layer, encapsulating data logic.
*   `viewmodel/`: ViewModels, managing UI state.
*   `ui/`:
    *   `actionlib/`: Exercise library pages.
    *   `calendar/`: Calendar and statistical report pages.
    *   `exercise/`: Training execution related pages (execution, preparation, results).
    *   `plan/`: Plan management pages (create, edit, list).
    *   `profile/`: Personal center, body data, calculator, and other pages.
    *   `common/`: Common UI components.
*   `service/`: Foreground service, used for background timing and floating windows.
*   `utils/`: Utility classes (body fat calculation, RM calculation, Bitmap saving, etc.).

## 🚀 Quick Start

### Environment Requirements
*   Android Studio Hedgehog (2023.1.1) or higher.
*   JDK 17.
*   Android SDK (minSdk 26, targetSdk 34).

### Build Steps
1.  Clone the repository locally.
2.  Open the `build.gradle.kts` in the project root directory in Android Studio.
3.  Wait for Gradle synchronization to complete.
4.  Connect a device or start the emulator, click the Run button to execute.

## 📄 License

This project follows the Apache License 2.0.

---

*Lianleme, helping you train scientifically and enjoy fitness!*