package com.vinaybyte.geminiai.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.vinaybyte.geminiai.di.GeminiPro
import com.vinaybyte.geminiai.di.GeminiProVision
import com.vinaybyte.geminiai.di.Response
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

/**
 * @Author: Vinay
 * @Date: 15-01-2024
 */
@HiltViewModel
class ChatViewModel @Inject constructor(
    @GeminiProVision private val geminiProVision: GenerativeModel,
    @GeminiPro private val geminiPro: GenerativeModel,
    val application: Application
) : ViewModel() {
    val imageRequestBuilder = ImageRequest.Builder(context = application)
    val imageLoader = ImageLoader.Builder(application).build()

    private val _promptResponse = MutableStateFlow<Response<Pair<String?, Bitmap?>>?>(null)
    val promptResponse: StateFlow<Response<Pair<String?, Bitmap?>>?>
        get() = _promptResponse

    fun sendPrompt(message: String?, pickUri: MutableList<Uri>) {
        _promptResponse.value = Response.Loading
        viewModelScope.launch {

            val bitmaps = pickUri.mapNotNull {
                val imageRequest = imageRequestBuilder
                    .data(it)
                    .size(size = 768)
                    .build()

                val imageResult = imageLoader.execute(imageRequest)
                if (imageResult is SuccessResult) {
                    return@mapNotNull (imageResult.drawable as BitmapDrawable).bitmap
                } else {
                    return@mapNotNull null
                }
            }
            if (!message.isNullOrBlank() && bitmaps.isEmpty()) {
                val contents = geminiPro.startChat().sendMessage(message)
                contents.text?.let {
                    _promptResponse.value = Response.Success(Pair(it, null))
                }
            } else if (bitmaps.isNotEmpty()) {
                var bitmap: Bitmap? = null
                val inputContent = content {
                    bitmaps.forEach {
                        bitmap = compressBitmap(it)
                        image(bitmap ?: compressBitmap(it))
                    }
                    text(if (message.isNullOrBlank()) "describe image" else message)
                }

                var output = ""
                geminiProVision.generateContentStream(inputContent).collect {
                    output += it.text
                }
                _promptResponse.value = Response.Success(Pair(output, bitmap))
            } else {
                _promptResponse.value = Response.Failure("Error")
            }
        }
    }
}

private fun compressBitmap(bitmap: Bitmap): Bitmap {
    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 1024, 1024, true)
    val byteArrayOutputStream = ByteArrayOutputStream()
    scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
    return BitmapFactory.decodeByteArray(
        byteArrayOutputStream.toByteArray(),
        0,
        byteArrayOutputStream.size()
    )
}