package com.nilhcem.assistant.androidthings.core.dagger

import com.nilhcem.assistant.androidthings.core.dagger.scope.ActivityScope
import com.nilhcem.assistant.androidthings.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AndroidBindingModule {

    @ActivityScope
    @ContributesAndroidInjector(modules = arrayOf(MainModule::class))
    abstract fun contributeMainActivity(): MainActivity
}
