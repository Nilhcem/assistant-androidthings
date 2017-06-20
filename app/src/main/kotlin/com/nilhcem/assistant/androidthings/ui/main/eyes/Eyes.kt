package com.nilhcem.assistant.androidthings.ui.main.eyes

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import java.io.IOException
import javax.inject.Inject

class Eyes @Inject constructor(private val context: Context) {

    companion object {
        private val TAG = Eyes::class.java.simpleName!!
        private val HANDLER_MSG_SHOW = 1
        private val HANDLER_MSG_STOP = 2
    }

    private lateinit var ledControl: LedControl

    private var curFrameIdx = 0
    private var emotion = Emotion.DEFAULT

    private var handler: Handler? = null
    private var handlerThread: HandlerThread? = null

    fun onCreate() {
        ledControl = LedControl("SPI0.0", 4)
        for (i in 0..ledControl.deviceCount - 1) {
            ledControl.setIntensity(i, 2)
            ledControl.shutdown(i, false)
            ledControl.clearDisplay(i)
        }

        handlerThread = HandlerThread("FrameThread").apply {
            start()

            handler = object : Handler(looper) {
                override fun handleMessage(msg: Message) {
                    super.handleMessage(msg)
                    if (msg.what != HANDLER_MSG_SHOW) {
                        return
                    }

                    try {
                        val frame = emotion.frames[curFrameIdx]
                        val bmp = BitmapFactory.decodeResource(context.resources, frame.drawableId)
                        ledControl.draw(bmp)
                        curFrameIdx = (curFrameIdx + 1) % emotion.frames.size

                        if (curFrameIdx == 0 && !emotion.loop) {
                            emotion = Emotion.DEFAULT
                        }
                        sendEmptyMessageDelayed(HANDLER_MSG_SHOW, frame.durationMillis)
                    } catch (e: IOException) {
                        Log.e(TAG, "Error displaying frame", e)
                    }
                }
            }
        }

        handler?.sendEmptyMessage(HANDLER_MSG_SHOW)
    }

    fun setEmotion(emotion: Emotion) {
        handler?.removeMessages(HANDLER_MSG_SHOW)
        this@Eyes.emotion = emotion
        curFrameIdx = 0
        handler?.sendEmptyMessage(HANDLER_MSG_SHOW)
    }

    fun onDestroy() {
        handler?.sendEmptyMessage(HANDLER_MSG_STOP)

        try {
            handlerThread?.quitSafely()
            ledControl.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error closing LED matrix", e)
        } finally {
            handler = null
            handlerThread = null
        }
    }
}
