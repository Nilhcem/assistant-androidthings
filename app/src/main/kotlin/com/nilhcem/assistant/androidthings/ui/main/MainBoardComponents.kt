package com.nilhcem.assistant.androidthings.ui.main

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.OnLifecycleEvent
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.pwmservo.Servo
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import com.nilhcem.assistant.androidthings.pubsub.Action
import com.nilhcem.assistant.androidthings.ui.main.eyes.Emotion
import com.nilhcem.assistant.androidthings.ui.main.eyes.Eyes
import javax.inject.Inject

class MainBoardComponents @Inject constructor(private val eyes: Eyes) : LifecycleObserver {

    companion object {
        private val GPIO_LED = "BCM25"
        private val GPIO_BUTTON = "BCM23"
        private val GPIO_SERVO = "PWM0"

        private val BUTTON_DEBOUNCE_DELAY_MS = 20L
    }

    val buttonPressedLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private lateinit var led: Gpio
    private lateinit var button: Button
    private lateinit var servo: Servo

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        val peripheralManagerService = PeripheralManagerService()

        led = peripheralManagerService.openGpio(GPIO_LED)
        led.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        led.value = false

        button = Button(GPIO_BUTTON, Button.LogicState.PRESSED_WHEN_LOW)
        button.setDebounceDelay(BUTTON_DEBOUNCE_DELAY_MS)
        button.setOnButtonEventListener { _, pressed ->
            led.value = pressed
            buttonPressedLiveData.value = pressed
        }

        servo = Servo(GPIO_SERVO)
        servo.setPulseDurationRange(1.0, 2.0)
        servo.setAngleRange(0.0, 180.0)
        servo.setEnabled(true)
        servo.angle = servo.minimumAngle

        eyes.onCreate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        led.value = false
        led.close()
        button.setOnButtonEventListener(null)
        button.close()
        servo.close()

        eyes.onDestroy()
    }

    fun setAction(action: Action) {
        when (action) {
            Action.CHOCOLATE -> {
                eyes.setEmotion(Emotion.HEARTS)
                toggleServo()
            }
            Action.HEARTS -> {
                eyes.setEmotion(Emotion.HEARTS)
            }
            Action.JOKE -> {
                eyes.setEmotion(Emotion.JOKE)
            }
            Action.NOPE -> {
                eyes.setEmotion(Emotion.NO)
            }
            Action.SAD -> {
                eyes.setEmotion(Emotion.SAD)
            }
        }
    }

    fun isLedsOn(): Boolean {
        return led.value
    }

    fun setLedsOn(on: Boolean) {
        led.value = on
    }

    private fun toggleServo() {
        if (servo.angle < (0.1 + servo.minimumAngle)) {
            servo.angle = servo.maximumAngle
        } else {
            servo.angle = servo.minimumAngle
        }
    }
}
