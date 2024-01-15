package com.vinaybyte.geminiai.ui.screen

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vinaybyte.geminiai.di.Response
import com.vinaybyte.geminiai.ui.ChatViewModel

/**
 * @Author: Vinay
 * @Date: 15-01-2024
 */
@Composable
fun AppContent(viewModel: ChatViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val messages = viewModel.promptResponse.collectAsState()

    MainScreen(messages.value) { inputText, selectedItems ->
        viewModel.sendPrompt(message = inputText, pickUri = selectedItems)
        selectedItems.clear()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    promptResponse: Response<Pair<String?, Bitmap?>>? = Response.Loading,
    onMessageSent: (String, MutableList<Uri>) -> Unit
) {
    Scaffold(
        topBar = {
            AppBars()
        },
        bottomBar = {
            UserInput(onMessageSent = { it, it1 ->
                onMessageSent(it, it1)
            })
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            when (promptResponse) {
                is Response.Loading -> {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is Response.Success -> {
                    val pair = promptResponse.data as Pair<String?, Bitmap?>
                    Card(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(), shape = MaterialTheme.shapes.large
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            pair.second?.let {
                                AsyncImage(
                                    model = pair.second,
                                    contentDescription = "",
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .requiredSize(300.dp)
                                )
                            }
                            Text(text = pair.first ?: "", modifier = Modifier.padding(16.dp))
                        }
                    }
                }

                is Response.Failure -> {
                    Card(
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(text = promptResponse.message)
                    }
                }

                else -> {}
            }
        }
    }
}


@Preview
@Composable
fun AppContentPreview() {
    AppContent()
}