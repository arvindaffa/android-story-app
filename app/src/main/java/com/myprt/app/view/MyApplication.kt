package com.myprt.app.view

import android.app.Application
import com.myprt.app.data.Injection

class MyApplication : Application() {

    lateinit var injection: Injection

    override fun onCreate() {
        super.onCreate()
        injection = Injection(applicationContext)
    }
}