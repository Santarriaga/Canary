package com.grumpy.canary

import android.app.Application
import com.grumpy.canary.database.AppDatabase

class ContactsApplication : Application() {
    val database : AppDatabase by lazy { AppDatabase.getDatabase(this) }
}