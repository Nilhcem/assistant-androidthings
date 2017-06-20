package com.nilhcem.assistant.androidthings.ui.main

import android.arch.lifecycle.ViewModel
import com.nilhcem.assistant.androidthings.pubsub.EventLiveData
import javax.inject.Inject

class MainViewModel @Inject constructor(val eventLiveData: EventLiveData) : ViewModel()
