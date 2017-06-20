package com.nilhcem.assistant.androidthings.core.dagger

import com.nilhcem.assistant.androidthings.App

object AppInjector {

    fun init(app: App) {
        DaggerAppComponent.builder()
                .appModule(AppModule(app))
                .build()
                .inject(app)
    }
}
