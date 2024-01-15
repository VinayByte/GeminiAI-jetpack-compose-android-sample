package com.vinaybyte.geminiai.di

import android.app.Application
import android.content.Context
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * @Author: Vinay
 * @Date: 15-01-2024
 */
@[Module InstallIn(SingletonComponent::class)]
object AppModule {
    private val harassment = SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH)
    private val hateSpeech =
        SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE)

    private val config = generationConfig {
        temperature = 0.99f
        topK = 50
        topP = 0.99f
    }

    @[Provides Singleton GeminiPro]
    fun provideGemini(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-pro",
            apiKey = API_KEY,
            safetySettings = listOf(
                harassment, hateSpeech
            ),
            generationConfig = config
        )
    }

    @[Provides Singleton GeminiProVision]
    fun provideGeminiVision(): GenerativeModel {
        return GenerativeModel(
            modelName = "gemini-pro-vision",
            apiKey = API_KEY,
            safetySettings = listOf(
                harassment,
                hateSpeech,
                SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.ONLY_HIGH),
                SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE)
            ),
        )
    }

    private const val API_KEY = "Enter API Key Here"

    @Provides
    fun provideApplicationContext(application: Application): Context {
        return application.applicationContext
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiPro

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiProVision