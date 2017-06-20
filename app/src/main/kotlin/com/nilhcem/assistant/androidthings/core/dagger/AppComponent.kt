package com.nilhcem.assistant.androidthings.core.dagger

import com.nilhcem.assistant.androidthings.App
import com.nilhcem.assistant.androidthings.core.dagger.scope.AppScope
import dagger.Component
import dagger.android.AndroidInjectionModule

@AppScope
@Component(modules = arrayOf(
        AndroidInjectionModule::class,
        AppModule::class,
        AndroidBindingModule::class)
)
interface AppComponent {
    fun inject(app: App)
}
