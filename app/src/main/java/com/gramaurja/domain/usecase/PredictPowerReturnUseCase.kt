package com.gramaurja.domain.usecase

import com.google.ai.client.generativeai.GenerativeModel
import com.gramaurja.data.model.PowerHistory
import com.gramaurja.data.model.PowerStatus
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * AI Prediction use case.
 *
 * Strategy:
 * 1. Try Gemini AI with history context
 * 2. Fall back to algorithmic prediction (average outage duration)
 */
class PredictPowerReturnUseCase @Inject constructor(
    private val geminiModel: GenerativeModel?
) {
    /**
     * Returns a human-readable prediction string.
     * e.g. "Power may return in ~2 hours based on past patterns"
     */
    suspend operator fun invoke(
        history: List<PowerHistory>,
        currentStatus: PowerStatus,
        zoneName: String
    ): String {
        if (history.size < 3) return "Not enough data for prediction yet"
        if (currentStatus == PowerStatus.ON) return "Power is currently ON ✓"

        return try {
            geminiModel?.let { model ->
                val prompt = buildGeminiPrompt(history, zoneName)
                val response = model.generateContent(prompt)
                response.text?.trim()?.take(120) ?: algorithmicPrediction(history)
            } ?: algorithmicPrediction(history)
        } catch (e: Exception) {
            Timber.w(e, "Gemini prediction failed, using algorithmic fallback")
            algorithmicPrediction(history)
        }
    }

    private fun buildGeminiPrompt(history: List<PowerHistory>, zoneName: String): String {
        val sdf = SimpleDateFormat("HH:mm dd-MMM", Locale.getDefault())
        val recent = history.takeLast(10)
        val historyText = recent.joinToString("\n") { entry ->
            "${sdf.format(Date(entry.timestamp))}: ${entry.status}"
        }
        return """
            You are a power availability predictor for a rural village in Karnataka, India.
            Zone: $zoneName
            Recent power history (oldest to newest):
            $historyText
            
            Based on this pattern, predict when power might return (if currently OFF).
            Reply in ONE short sentence (max 15 words), e.g. "Power may return around 6 PM based on past patterns."
        """.trimIndent()
    }

    private fun algorithmicPrediction(history: List<PowerHistory>): String {
        // Find average outage duration from history
        val offPeriods = mutableListOf<Long>()
        var lastOffTime: Long? = null

        history.sortedBy { it.timestamp }.forEach { entry ->
            when (entry.status) {
                "OFF" -> lastOffTime = entry.timestamp
                "ON" -> {
                    lastOffTime?.let { offStart ->
                        offPeriods.add(entry.timestamp - offStart)
                    }
                    lastOffTime = null
                }
            }
        }

        return if (offPeriods.isEmpty()) {
            "Insufficient outage history for prediction"
        } else {
            val avgMinutes = (offPeriods.average() / 60_000).toInt()
            val hours = avgMinutes / 60
            val mins = avgMinutes % 60
            val duration = when {
                hours > 0 -> "~${hours}h ${mins}min"
                else -> "~${mins} minutes"
            }
            "Based on past patterns, power typically returns in $duration"
        }
    }
}
