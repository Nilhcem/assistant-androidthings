package com.nilhcem.assistant.androidthings.ui.main

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.OnLifecycleEvent
import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import com.felhr.usbserial.UsbSerialDevice
import com.felhr.usbserial.UsbSerialInterface
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import com.nilhcem.assistant.androidthings.pubsub.Action
import com.nilhcem.assistant.androidthings.ui.main.eyes.Emotion
import com.nilhcem.assistant.androidthings.ui.main.eyes.Eyes
import javax.inject.Inject

class MainBoardComponents @Inject constructor(private val eyes: Eyes, private val context: Context) : LifecycleObserver {

    companion object {
        private val GPIO_LED = "BCM25"
        private val GPIO_BUTTON = "BCM23"

        private val BUTTON_DEBOUNCE_DELAY_MS = 20L
    }

    val buttonPressedLiveData: MutableLiveData<Boolean> = MutableLiveData()

    private lateinit var led: Gpio
    private lateinit var button: Button
    private var arduino: UsbSerialDevice? = null

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

        val usbManager = context.getSystemService(UsbManager::class.java)
        val connectedDevices = usbManager.deviceList
        for (usbDevice in connectedDevices.values) {
            if (usbDevice.vendorId == 0x2341 && usbDevice.productId == 0x0001) {
                Log.i("MainBoardComponents", "Arduino found!")
                val connection = usbManager.openDevice(usbDevice)
                arduino = UsbSerialDevice.createUsbSerialDevice(usbDevice, connection)
                if (arduino?.open() ?: false) {
                    arduino?.setBaudRate(115200)
                    arduino?.setDataBits(UsbSerialInterface.DATA_BITS_8)
                    arduino?.setStopBits(UsbSerialInterface.STOP_BITS_1)
                    arduino?.setParity(UsbSerialInterface.PARITY_NONE)
                    arduino?.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF)
                }
            }
        }

        eyes.onCreate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        led.value = false
        led.close()
        button.setOnButtonEventListener(null)
        button.close()

        arduino?.close()

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
        arduino?.write("!".toByteArray(Charsets.UTF_8))
    }
}
