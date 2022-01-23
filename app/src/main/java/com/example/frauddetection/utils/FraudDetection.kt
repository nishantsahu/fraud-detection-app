package com.example.frauddetection.utils

import android.util.Log
import java.util.*

object FraudDetection {
    val keywordLibrary: List<String> = listOf(
        "banking", "bank", "account", "amount", "otp"
    )

    fun detectFromSenderAndMessage(sender: String, message: String): String {
        var phone: String? = sender
        if (sender.contains("+91")) {
            phone = sender.removePrefix("+91")
        }
        if (phone?.length == 10) {
            var detectionCount = 0
            val msg = message.split("\\P{L}+".toRegex())
            msg.forEach { it ->
                keywordLibrary.forEach { it1 ->
                    Log.d("msg", "${it.lowercase(Locale.getDefault()).trim()} and $it1")
                    if (it.lowercase(Locale.getDefault()).trim() == it1.trim()) {
                        detectionCount++
                    }
                }
            }
            if (detectionCount > 0) {
                return "$detectionCount level Fraud"
            } else {
                return "0% Fraud"
            }
        } else {
            return "0% Fraud"
        }
    }
}