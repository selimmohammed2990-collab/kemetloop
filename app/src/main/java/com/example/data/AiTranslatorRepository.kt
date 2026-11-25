package com.example.data

import android.graphics.Bitmap
import android.util.Log
import com.example.model.ImageTranslationResult
import com.example.model.TranslationResult
import com.example.model.TranslatorLanguage
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

class AiTranslatorRepository {

    private val translationSchema = Schema.obj(
        properties = mapOf(
            "translatedText" to Schema.string(
                description = "Accurate, natural translation tailored for travelers and merchants."
            ),
            "pronunciationGuide" to Schema.string(
                description = "Phonetic pronunciation guide written clearly for an Arabic or English speaker."
            ),
            "culturalTip" to Schema.string(
                description = "Short, helpful travel or commerce etiquette tip in Arabic."
            )
        )
    )

    private val imageOcrSchema = Schema.obj(
        properties = mapOf(
            "extractedText" to Schema.string(
                description = "Original text extracted cleanly from the restaurant menu, sign, or document."
            ),
            "translatedText" to Schema.string(
                description = "Accurate, easy-to-read translation of the extracted menu items or sign text."
            ),
            "pronunciationGuide" to Schema.string(
                description = "Helpful phonetic pronunciation guide for key phrases or dishes."
            ),
            "culturalTip" to Schema.string(
                description = "Practical Arabic travel/menu tip (prices, ingredients, or directions)."
            )
        )
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = translationSchema
            }
        )
    }

    private val visionModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = imageOcrSchema
            }
        )
    }

    suspend fun translate(
        inputText: String,
        sourceLanguage: TranslatorLanguage,
        targetLanguage: TranslatorLanguage
    ): Result<TranslationResult> {
        val cleanInput = inputText.trim()
        if (cleanInput.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى إدخال نص للترجمة."))
        }
        return try {
            val prompt = """
                You are KemetLoop Smart Travel & Commerce Translator.
                Translate the following text from ${sourceLanguage.nameAr} (${sourceLanguage.code})
                to ${targetLanguage.nameAr} (${targetLanguage.code}).
                Keep the tone natural, polite, and practical for travelers and merchants.
                Text: "$cleanInput"
            """.trimIndent()

            val response = withTimeout(45_000L) {
                model.generateContent(prompt)
            }
            val rawJson = response.text?.takeIf { it.isNotBlank() }
                ?: return Result.failure(IllegalStateException("لم يُعد المترجم الذكي استجابة صالحة."))

            val json = JSONObject(rawJson)
            Result.success(
                TranslationResult(
                    translatedText = json.optString("translatedText", rawJson),
                    pronunciationGuide = json.optString("pronunciationGuide", ""),
                    culturalTip = json.optString("culturalTip", "")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Translation timed out", e)
            Result.failure(Exception("انتهت مهلة الاتصال بالمترجم الذكي. حاول مجدداً."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI translation failed", e)
            Result.failure(e)
        }
    }

    suspend fun translateFromImage(
        bitmap: Bitmap,
        targetLanguage: TranslatorLanguage
    ): Result<ImageTranslationResult> {
        return try {
            val scaledBitmap = scaleBitmapForAi(bitmap, maxDim = 1280)
            val prompt = """
                You are KemetLoop Camera & Menu OCR Translator for travelers and merchants.
                1. Read and extract all visible text from this photo (restaurant menu, street sign, price tag, or label).
                2. Translate the extracted text accurately into ${targetLanguage.nameAr} (${targetLanguage.code}).
                3. Provide a clear pronunciation guide and a short helpful Arabic tip if relevant (e.g., dishes, prices, or directions).
            """.trimIndent()

            val response = withTimeout(60_000L) {
                visionModel.generateContent(
                    content {
                        image(scaledBitmap)
                        text(prompt)
                    }
                )
            }
            val rawJson = response.text?.takeIf { it.isNotBlank() }
                ?: return Result.failure(IllegalStateException("لم يتم التعرف على نص واضح في الصورة."))

            val json = JSONObject(rawJson)
            Result.success(
                ImageTranslationResult(
                    extractedText = json.optString("extractedText", ""),
                    translatedText = json.optString("translatedText", rawJson),
                    pronunciationGuide = json.optString("pronunciationGuide", ""),
                    culturalTip = json.optString("culturalTip", "")
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Camera OCR translation timed out", e)
            Result.failure(Exception("انتهت مهلة تحليل الصورة. تأكد من الاتصال بالإنترنت وحاول مجدداً."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Camera OCR translation failed", e)
            Result.failure(e)
        }
    }

    private fun scaleBitmapForAi(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDim && height <= maxDim) return bitmap
        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width >= height) {
            newWidth = maxDim
            newHeight = (maxDim / ratio).toInt().coerceAtLeast(1)
        } else {
            newHeight = maxDim
            newWidth = (maxDim * ratio).toInt().coerceAtLeast(1)
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    private val voiceAssistantModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    suspend fun askVoiceAssistant(query: String, userPlanLabel: String): Result<String> {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) {
            return Result.failure(IllegalArgumentException("يرجى كتابة سؤالك أو التحدث أولاً."))
        }
        return try {
            val prompt = """
                You are KemetLoop AI Voice Concierge, an elite Egyptian smart-tourism, luxury yacht,
                marketplace, and multi-language travel assistant.
                The user's current plan is: $userPlanLabel.
                Respond naturally in clear, concise, warm Arabic (unless the user explicitly wrote in another language).
                Keep answers practical and ready to be read aloud (2-4 concise sentences, no markdown tables or asterisks).
                User question: "$cleanQuery"
            """.trimIndent()
            val response = withTimeout(45_000L) {
                voiceAssistantModel.generateContent(prompt)
            }
            val text = response.text?.trim()?.takeIf { it.isNotBlank() }
                ?: return Result.failure(IllegalStateException("لم يتم استلام رد من المساعد الذكي."))
            Result.success(text)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Voice assistant timed out", e)
            Result.failure(Exception("انتهت مهلة الاتصال بالمساعد الذكي. حاول مجدداً."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Voice assistant failed", e)
            Result.failure(e)
        }
    }

    private companion object {
        const val TAG = "AiTranslatorRepo"
    }
}
