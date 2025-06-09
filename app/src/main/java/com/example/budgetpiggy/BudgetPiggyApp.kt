package com.example.budgetpiggy

import android.app.Application
import com.google.firebase.FirebaseApp

class BudgetPiggyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this) // Initializes Firebase
    }
}