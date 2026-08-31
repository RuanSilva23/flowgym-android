package com.ruan.apexlift.ui.util

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class TimerAlertHelper(private val context: Context) {

    fun dispararAlertaFimDescanso() {
        tocarBipe()
        acionarVibracao()
    }

    private fun tocarBipe() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun acionarVibracao() {
        try {
            // Padrão de pulso duplo: Espera 0ms, Vibra 400ms, Pausa 150ms, Vibra 400ms
            val timing = longArrayOf(0, 400, 150, 400)
            val amplitudes = intArrayOf(0, 255, 0, 255)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ (API 31+)
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator

                if (vibrator.hasVibrator()) {
                    val vibrationEffect = VibrationEffect.createWaveform(timing, amplitudes, -1)
                    val attributes = VibrationAttributes.Builder()
                        .setUsage(VibrationAttributes.USAGE_ALARM)
                        .build()
                    vibrator.vibrate(vibrationEffect, attributes)
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0 até Android 11 (API 26 a 30)
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                if (vibrator.hasVibrator()) {
                    val vibrationEffect = VibrationEffect.createWaveform(timing, amplitudes, -1)
                    val audioAttributes = AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                    vibrator.vibrate(vibrationEffect, audioAttributes)
                }
            } else {
                // Android legado
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                @Suppress("DEPRECATION")
                vibrator.vibrate(timing, -1)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}