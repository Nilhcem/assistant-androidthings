package com.nilhcem.assistant.androidthings.core.dagger

import android.content.Context
import com.nilhcem.assistant.androidthings.core.dagger.scope.AppScope
import com.nilhcem.assistant.androidthings.core.dagger.viewmodel.ViewModelModule
import dagger.Module
import dagger.Provides

@Module(includes = arrayOf(ViewModelModule::class))
class AppModule(private val context: Context) {

    @AppScope @Provides fun provideContext(): Context {
        return context
    }
}
