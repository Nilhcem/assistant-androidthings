package com.nilhcem.assistant.androidthings.ui.main

import android.arch.lifecycle.LifecycleActivity
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import com.nilhcem.assistant.androidthings.googleassistant.AssistantHelper
import com.nilhcem.assistant.androidthings.googleassistant.AssistantResponseLiveData.Status.ON_AUDIO_OUT
import com.nilhcem.assistant.androidthings.googleassistant.AssistantResponseLiveData.Status.ON_COMPLETED
import dagger.android.AndroidInjection
import javax.inject.Inject

class MainActivity : LifecycleActivity() {

    @Inject lateinit var boardComponents: MainBoardComponents
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var assistantHelper: AssistantHelper
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(boardComponents)
        lifecycle.addObserver(assistantHelper)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainViewModel::class.java)

        viewModel.eventLiveData.observe(this, Observer { action ->
            action?.let {
                boardComponents.setAction(action)
            }
        })

        boardComponents.buttonPressedLiveData.observe(this, Observer { pressed ->
            assistantHelper.onButtonPressed(pressed ?: false)
        })

        assistantHelper.assistantResponseLiveData.observe(this, Observer { status ->
            when (status) {
                ON_AUDIO_OUT -> boardComponents.setLedsOn(!boardComponents.isLedsOn())
                ON_COMPLETED -> boardComponents.setLedsOn(false)
            }
        })
    }
}
