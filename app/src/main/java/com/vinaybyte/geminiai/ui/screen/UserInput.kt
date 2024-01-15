package com.vinaybyte.geminiai.ui.screen

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.vinaybyte.geminiai.R
import com.vinaybyte.geminiai.di.UriCustomSaver

enum class InputSelector {
    NONE,
    PICTURE
}

@Preview
@Composable
fun UserInputPreview() {
    UserInput(onMessageSent = { it, it1 -> })
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserInput(
    onMessageSent: (String, MutableList<Uri>) -> Unit,
    modifier: Modifier = Modifier,
    resetScroll: () -> Unit = {},
) {
    val imageUris = rememberSaveable(saver = UriCustomSaver()) {
        mutableStateListOf()
    }

    val pickMediaLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { imageUri ->
            imageUri?.let {
                imageUris.clear() //added for single image
                imageUris.add(it)
            }
        }

    val focusRequester = remember { FocusRequester() }
    var isFirstTimeFocus by rememberSaveable {
        mutableStateOf(true)
    }
    var currentInputSelector by rememberSaveable { mutableStateOf(InputSelector.NONE) }
    val dismissKeyboard = { currentInputSelector = InputSelector.NONE }
    val focusManager = LocalFocusManager.current

    if (currentInputSelector != InputSelector.NONE) {
        BackHandler(onBack = dismissKeyboard)
    }

    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue())
    }

    var textFieldFocusState by remember { mutableStateOf(false) }

    var isExpanded by remember {
        mutableStateOf(false)
    }

    var isSendEnable by remember {
        mutableStateOf(false)
    }

    Surface(tonalElevation = 2.dp, contentColor = MaterialTheme.colorScheme.secondary) {
        Column {
            AnimatedVisibility(visible = !isExpanded) {
                Row(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserInputSelector(
                        onClick = {
                            focusManager.clearFocus()
                            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    )
//
                    UserInputText(
                        textFieldValue = textState,
                        onTextChanged = { textState = it },
                        // Only show the keyboard if there's no input selector and text field has focus
                        keyboardShown = currentInputSelector == InputSelector.NONE && textFieldFocusState,
                        // Close extended selector if text field receives focus
                        onTextFieldFocused = { focused ->
                            if (focused) {
                                currentInputSelector = InputSelector.NONE
                                resetScroll()
                            }
                            textFieldFocusState = focused
                        },
                        onMessageSent = {
                            onMessageSent(textState.text, imageUris)
                            textState = TextFieldValue()
                        },

                        isFirstTimeFocus = isFirstTimeFocus,
                        isFirstTimeFocusUpdate = { value ->
                            isFirstTimeFocus = value
                        },
                        modifier = modifier
                            .weight(1f),
                        focusRequester = focusRequester,
                        onSendEnable = { isEnable ->
                            isSendEnable = isEnable
                        }
                    )
                    SendMessage(
                        onMessageSent = {
                            onMessageSent(textState.text, imageUris)
                            textState = TextFieldValue()
                        }
                    )
                }
            }
            SelectorExpanded(imageUris = imageUris)
        }
    }
}

@ExperimentalFoundationApi
@Composable
private fun UserInputText(
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    onTextChanged: (TextFieldValue) -> Unit,
    textFieldValue: TextFieldValue,
    onMessageSent: (String) -> Unit,
    onSendEnable: (Boolean) -> Unit,
    keyboardShown: Boolean,
    onTextFieldFocused: (Boolean) -> Unit,
    isFirstTimeFocus: Boolean,
    isFirstTimeFocusUpdate: (Boolean) -> Unit,
    focusRequester: FocusRequester
) {
    val a11ylabel = stringResource(id = R.string.textfield_desc)
    Box(modifier = modifier.fillMaxWidth()) {
        UserInputTextField(
            textFieldValue,
            onTextChanged,
            onTextFieldFocused,
            onSendEnable,
            onMessageSent,
            keyboardType,
            isFirstTimeFocus,
            isFirstTimeFocusUpdate,
            Modifier.semantics {
                contentDescription = a11ylabel
                keyboardShownProperty = keyboardShown
            },
            focusRequester = focusRequester
        )
    }
}

@Composable
private fun UserInputTextField(
    textFieldValue: TextFieldValue,
    onTextChanged: (TextFieldValue) -> Unit,
    onTextFieldFocused: (Boolean) -> Unit,
    onSendEnable: (Boolean) -> Unit,
    onMessageSent: (String) -> Unit,
    keyboardType: KeyboardType,
    isFirstTimeFocus: Boolean,
    onFirstTimeFocusUpdate: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester
) {
    var lastFocusState by remember { mutableStateOf(false) }
    var textFieldLoaded by remember { mutableStateOf(false) }

    if (textFieldValue.text.isEmpty() || textFieldValue.text.isBlank()) onSendEnable(false)

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .height(40.dp)
    ) {
        Box(contentAlignment = Alignment.CenterStart, modifier = modifier.weight(1f)) {
            BasicTextField(
                value = textFieldValue,
                onValueChange = {
                    onTextChanged(it)
                    onSendEnable(it.text.isNotEmpty() && it.text.isNotBlank())
                },
                modifier = modifier
                    .padding(start = 12.dp, end = 8.dp)
                    .onFocusChanged { state ->
                        if (lastFocusState != state.isFocused) {
                            onTextFieldFocused(state.isFocused)
                        }
                        lastFocusState = state.isFocused
                    }
                    .focusRequester(focusRequester)
                    .onGloballyPositioned {
                        if (isFirstTimeFocus) {
                            if (!textFieldLoaded) {
                                focusRequester.requestFocus()
                                textFieldLoaded = true
                            }
                            onFirstTimeFocusUpdate(false)
                        }
                    },
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    imeAction = ImeAction.Send,
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        onMessageSent(textFieldValue.text)
                    }
                ),
                maxLines = 1,
                textStyle = TextStyle(color = Color.Black)
            )
            Row(modifier = modifier.fillMaxSize()) {
                Box(modifier = modifier
                    .fillMaxSize()
                    .clickable {
                        focusRequester.requestFocus()
                    }
                )
            }
            if (textFieldValue.text.isEmpty()) {
                Text(
                    modifier = Modifier
                        .padding(start = 12.dp),
                    text = "Aa",
                )
            }
        }
    }
}

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

@Composable
fun UserInputSelector(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        SelectorButton(
            onClick = onClick,
            icon = Icons.Filled.Image,
            description = stringResource(id = R.string.attach_photo_desc)
        )
    }
}

@Composable
fun SelectorButton(
    onClick: () -> Unit,
    icon: ImageVector,
    description: String,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            icon,
            tint = LocalContentColor.current,
            modifier = modifier
                .padding(8.dp)
                .size(56.dp),
            contentDescription = description
        )
    }
}

@Composable
fun SendMessage(
    onMessageSent: () -> Unit
) {
    IconButton(
        onClick = onMessageSent
    ) {
        Icon(
            imageVector = Icons.Filled.Send,
            tint = Color.Blue,
            modifier = Modifier
                .padding(8.dp)
                .size(56.dp),
            contentDescription = "fast emoji"
        )
    }
}

@Composable
private fun SelectorExpanded(
    modifier: Modifier = Modifier,
    imageUris: MutableList<Uri>
) {
    Surface(tonalElevation = 8.dp) {
        AnimatedVisibility(
            visible = imageUris.size > 0
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                LazyRow(modifier = Modifier.padding(8.dp)) {
                    items(imageUris) { imageUri ->
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "",
                                modifier = Modifier
                                    .padding(4.dp)
                                    .requiredSize(50.dp)
                            )
                            TextButton(onClick = { imageUris.remove(imageUri) }) {
                                Text(text = "X")
                            }
                        }
                    }
                }
            }
        }
    }
}

