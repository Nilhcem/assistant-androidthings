package com.nilhcem.assistant.androidthings.googleassistant;

import android.arch.lifecycle.MutableLiveData;

public class AssistantResponseLiveData extends MutableLiveData<AssistantResponseLiveData.Status> {

    public enum Status {
        ON_AUDIO_OUT,
        ON_COMPLETED
    }
}
