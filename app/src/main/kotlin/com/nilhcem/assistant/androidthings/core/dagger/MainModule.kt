package com.nilhcem.assistant.androidthings.core.dagger

import android.content.Context
import com.nilhcem.assistant.androidthings.core.dagger.scope.ActivityScope
import com.nilhcem.assistant.androidthings.googleassistant.AssistantHelper
import com.nilhcem.assistant.androidthings.pubsub.EventLiveData
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides fun provideEventLiveData(context: Context): EventLiveData {
        return EventLiveData(context)
    }

    @ActivityScope @Provides fun provideAssistantHelper(context: Context): AssistantHelper {
        return AssistantHelper(context)
    }
}
